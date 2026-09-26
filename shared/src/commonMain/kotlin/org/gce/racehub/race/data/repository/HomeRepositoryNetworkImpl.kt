package org.gce.racehub.race.data.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.*
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.db.LocalDataSource
import org.gce.racehub.race.data.dto.*
import org.gce.racehub.race.domain.model.*
import org.gce.racehub.race.domain.repository.HomeRepository
import org.gce.racehub.util.logError
import org.gce.racehub.util.platformIoDispatcher
import kotlin.time.Duration.Companion.milliseconds

/**
 * Network-based implementation of [HomeRepository].
 *
 * Fetches F1 racing data (races, standings, trending threads) from a GraphQL backend,
 * caches results in a local database, and syncs in the background.
 *
 * Architecture:
 * - Primary data source: Local database (fast UI updates)
 * - Secondary: Network (background refresh)
 * - Eventual consistency: Data updates asynchronously without blocking UI
 */
class HomeRepositoryNetworkImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val localDataSource: LocalDataSource,
    private val ioDispatcher: CoroutineDispatcher = platformIoDispatcher
) : HomeRepository {

    companion object {
        private const val TAG = "HomeRepository"
        private const val SYNC_TIMEOUT_MS = 5_000L
    }

    /**
     * Long-lived scope for fire-and-forget background refreshes. Survives the
     * suspend call that triggered it so cached data can be returned immediately
     * while the network sync completes off the caller's critical path.
     */
    private val syncScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    /** Maps data-layer exceptions onto the domain's [DataError]. */
    private fun Exception.toDataError(): DataError = when (this) {
        is IOException -> DataError.Network // includes timeouts and connection failures
        is ResponseException, is SerializationException, is IllegalStateException -> DataError.Server
        else -> DataError.Unknown
    }

    private fun List<GraphQLError>?.toErrorMessage(fallback: String): String =
        this?.joinToString { it.message }?.takeIf { it.isNotBlank() } ?: fallback

    // GraphQL query to fetch the signed-in user's profile
    private val profileQuery = $$"""
        query GetMyProfile($userId: String) {
            me(userId: $userId) {
                username
                email
                avatar
                postsCount
                savedCount
                recentThreads { title }
                savedThreads { title }
            }
        }
    """.trimIndent()

    // GraphQL mutation to add a comment to a forum thread
    private val addCommentMutation = $$"""
        mutation AddComment($userId: ID!, $threadId: ID!, $content: String!) {
            addComment(userId: $userId, threadId: $threadId, content: $content) {
                id
                content
                createdAt
            }
        }
    """.trimIndent()

    // GraphQL mutation to toggle like on a forum thread
    private val likeThreadMutation = $$"""
        mutation UserInteractions($id: ID!) {
            likeThread(id: $id) {
                id
                likes
            }
        }
    """.trimIndent()

    // GraphQL mutation to create a new forum thread
    private val createThreadMutation = $$"""
        mutation CreateThread($userId: ID!, $input: CreateThreadInput!) {
            createThread(userId: $userId, input: $input) {
                id
                title
                createdAt
            }
        }
    """.trimIndent()

    // GraphQL query to fetch full forum threads with author and comments
    private val threadsQuery = $$"""
        query GetThreads($sort: String, $category: String, $userId: ID) {
            threads(sort: $sort, category: $category) {
                id
                title
                category
                author { username avatar }
                excerpt
                content
                createdAt
                likes
                bookmarked(userId: $userId)
                comments {
                    content
                    author { username }
                }
            }
        }
    """.trimIndent()

    // GraphQL query to fetch full race detail by slug
    private val raceDetailQuery = $$"""
        query GetRaceDetail($slug: String!) {
            race(slug: $slug) {
                grandPrix
                circuit
                overview
                trackFacts {
                    laps
                    lapRecord
                    distanceKm
                    corners
                }
                sessions {
                    label
                    dateTime
                }
                results {
                    position
                    driver
                    team
                    points
                    time
                }
                fastestLap {
                    driver
                    time
                }
            }
        }
    """.trimIndent()

    // GraphQL query to fetch the full race schedule
    private val racesQuery = """
        query GetRaces {
            races {
                round
                slug
                grandPrix
                circuit
                country
                city
                dateTime
                status
                weather
            }
        }
    """.trimIndent()

    // GraphQL query to fetch complete dashboard data
    private val dashboardQuery = """
        query GetDashboard {
            dashboard {
                seasonYear
                upcomingRace {
                    grandPrix
                    city
                    dateTime
                    status
                }
                latestRace {
                    grandPrix
                }
                driverStandings {
                    position
                    name
                    team
                    points
                    wins
                }
                constructorStandings {
                    position
                    name
                    points
                    wins
                }
                trendingThreads {
                    id
                    title
                    likes
                    createdAt
                }
            }
        }
    """.trimIndent()

    /**
     * Fetches the full race schedule from the network and saves it locally.
     * @return null on success, otherwise why the sync failed.
     */
    private suspend fun syncRaceSchedule(): DataError? {
        return try {
            val response: GraphQLResponse<RacesData> = httpClient.post("$baseUrl/graphql") {
                contentType(ContentType.Application.Json)
                setBody(GraphQLRequest(racesQuery))
            }.body()

            val races = response.data?.races
            if (races == null) {
                logError(TAG, "Failed to sync races: ${response.errors.toErrorMessage("no data")}")
                return DataError.Server
            }

            localDataSource.saveRaces(races.map { dto ->
                Race(
                    id = dto.slug,
                    name = dto.grandPrix,
                    circuit = dto.circuit,
                    country = dto.country,
                    city = dto.city,
                    dateTime = dto.dateTime,
                    round = dto.round,
                    status = dto.status,
                    weather = dto.weather
                )
            })
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logError(TAG, "Failed to sync races", e)
            e.toDataError()
        }
    }

    /**
     * Syncs all dashboard data from the network to the local database.
     * Errors are logged and returned, never thrown.
     * @return null on success, otherwise why the sync failed.
     */
    private suspend fun syncDashboard(): DataError? {
        return try {
            val response: GraphQLResponse<DashboardData> = httpClient.post("$baseUrl/graphql") {
                contentType(ContentType.Application.Json)
                setBody(GraphQLRequest(dashboardQuery))
            }.body()

            val dashboard = response.data?.dashboard
            if (dashboard == null) {
                logError(TAG, "Failed to sync dashboard: ${response.errors.toErrorMessage("no data")}")
                return DataError.Server
            }
            saveDashboardData(dashboard)
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logError(TAG, "Failed to sync dashboard", e)
            e.toDataError()
        }
    }

    /**
     * Processes and saves all dashboard data from the GraphQL response.
     */
    private fun saveDashboardData(dashboard: DashboardContent) {
        // NOTE: the races table is owned exclusively by syncRaceSchedule() (the full
        // GetRaces query). The dashboard only carries a single summary "upcoming" race,
        // and saveRaces() replaces the whole table — writing it here would wipe the full
        // calendar down to one row. The UI derives the upcoming race from the full
        // schedule, so dashboard race data is intentionally not persisted.

        // Save driver standings
        val driverStandings = dashboard.driverStandings.map { dto ->
            DriverStanding(
                position = dto.position,
                driverName = dto.name,
                team = dto.team.orEmpty(),
                points = dto.points,
                wins = dto.wins
            )
        }
        if (driverStandings.isNotEmpty()) {
            localDataSource.saveDriverStandings(driverStandings)
        }

        // Save constructor standings
        val constructorStandings = dashboard.constructorStandings.map { dto ->
            ConstructorStanding(
                position = dto.position,
                name = dto.name,
                points = dto.points,
                wins = dto.wins
            )
        }
        if (constructorStandings.isNotEmpty()) {
            localDataSource.saveConstructorStandings(constructorStandings)
        }

        // Save trending threads
        val trendingThreads = dashboard.trendingThreads.map { dto ->
            TrendingThread(
                id = dto.id,
                title = dto.title,
                likes = dto.likes,
                createdAt = dto.createdAt
            )
        }
        if (trendingThreads.isNotEmpty()) {
            localDataSource.saveTrendingThreads(trendingThreads)
        }
    }

    /**
     * Returns the full race schedule from the dedicated races query.
     *
     * Unlike the secondary dashboard data, the schedule is the primary content of
     * the calendar screen, so we await the sync (bounded by [SYNC_TIMEOUT_MS]) and
     * then re-read, returning the freshest data the network can provide within the
     * timeout and falling back to whatever is cached if the network is slow. A pure
     * background refresh would leave the screen showing a stale snapshot until the
     * next manual reload.
     */
    override suspend fun getRaceSchedule(): DataResult<List<Race>> = withContext(ioDispatcher) {
        val syncError = if (localDataSource.getAllRaces().isEmpty()) {
            // Cold cache: block until the first sync populates the DB.
            syncRaceSchedule()
        } else {
            // Warm cache: refresh now, capped by the timeout; a slow network just serves the cache.
            withTimeoutOrNull(SYNC_TIMEOUT_MS.milliseconds) { syncRaceSchedule() }
        }
        cachedOrFailure(localDataSource.getAllRaces(), syncError)
    }

    /**
     * Returns driver standings, refreshing from the network if empty.
     * Subsequent calls trigger background syncs without blocking.
     */
    override suspend fun getDriverStandings(): DataResult<List<DriverStanding>> =
        dashboardRead { localDataSource.getAllDriverStandings() }

    /**
     * Returns constructor standings, refreshing from the network if empty.
     * Subsequent calls trigger background syncs without blocking.
     */
    override suspend fun getConstructorStandings(): DataResult<List<ConstructorStanding>> =
        dashboardRead { localDataSource.getAllConstructorStandings() }

    /**
     * Returns trending threads, refreshing from the network if empty.
     * Subsequent calls trigger background syncs without blocking.
     */
    override suspend fun getTrendingThreads(): DataResult<List<TrendingThread>> =
        dashboardRead { localDataSource.getAllTrendingThreads() }

    /**
     * Reads dashboard-backed rows: on a cold cache waits for a dashboard sync,
     * otherwise returns the cache and refreshes in the background.
     */
    private suspend fun <T> dashboardRead(read: () -> List<T>): DataResult<List<T>> =
        withContext(ioDispatcher) {
            val syncError = if (read().isEmpty()) syncDashboard() else null.also { backgroundSync() }
            // Re-read after a cold-cache sync so freshly-saved rows are returned.
            cachedOrFailure(read(), syncError)
        }

    /** Cached rows win over a sync error; only an empty cache surfaces the failure. */
    private fun <T> cachedOrFailure(rows: List<T>, syncError: DataError?): DataResult<List<T>> =
        if (rows.isEmpty() && syncError != null) DataResult.Failure(syncError) else DataResult.Success(rows)

    /**
     * Fetches forum threads directly from the network. Not cached locally
     * because the schema (author, comments, bookmarks) doesn't fit the
     * existing trending-threads SQLDelight table.
     */
    override suspend fun getThreads(
        sort: String?,
        category: String?,
        userId: String?
    ): List<Thread> {
        return try {
            val response: GraphQLResponse<ThreadsData> = httpClient.post("$baseUrl/graphql") {
                contentType(ContentType.Application.Json)
                setBody(
                    GraphQLRequestWithVariables(
                        query = threadsQuery,
                        variables = ThreadsVariables(sort = sort, category = category, userId = userId)
                    )
                )
            }.body()

            val data = response.data
                ?: error(response.errors.toErrorMessage("Empty GraphQL response"))

            data.threads.map { dto ->
                Thread(
                    id = dto.id,
                    title = dto.title,
                    category = dto.category,
                    author = ThreadAuthor(dto.author.username, dto.author.avatar),
                    excerpt = dto.excerpt,
                    content = dto.content,
                    createdAt = dto.createdAt,
                    likes = dto.likes,
                    bookmarked = dto.bookmarked,
                    comments = dto.comments.map { c ->
                        ThreadComment(content = c.content, authorUsername = c.author.username)
                    }
                )
            }
        } catch (e: Exception) {
            logError(TAG, "Failed to fetch threads", e)
            emptyList()
        }
    }

    /**
     * Creates a new forum thread via GraphQL mutation.
     */
    override suspend fun createThread(
        userId: String,
        title: String,
        category: String,
        content: String
    ): Thread {
        val response: GraphQLResponse<CreateThreadData> = httpClient.post("$baseUrl/graphql") {
            contentType(ContentType.Application.Json)
            setBody(
                GraphQLCreateThreadRequest(
                    query = createThreadMutation,
                    variables = CreateThreadVariables(
                        userId = userId,
                        input = CreateThreadInput(title, category, content)
                    )
                )
            )
        }.body()

        val created = response.data?.createThread
            ?: error(response.errors.toErrorMessage("Failed to create thread"))
        return Thread(
            id = created.id,
            title = created.title,
            category = category,
            author = ThreadAuthor(username = "", avatar = ""),
            excerpt = null,
            content = content,
            createdAt = created.createdAt,
            likes = 0,
            bookmarked = false,
            comments = emptyList()
        )
    }

    /**
     * Fetches the signed-in user's profile via GraphQL query.
     * Sends the auth token as an `Authorization: Bearer` header.
     */
    override suspend fun getMyProfile(userId: String, token: String): UserProfile {
        val response: GraphQLResponse<ProfileData> = httpClient.post("$baseUrl/graphql") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(
                GraphQLProfileRequest(
                    query = profileQuery,
                    variables = ProfileVariables(userId = userId)
                )
            )
        }.body()

        val me = response.data?.me
            ?: error(response.errors.toErrorMessage("Failed to load profile"))
        return UserProfile(
            username = me.username,
            email = me.email,
            avatar = me.avatar,
            postsCount = me.postsCount,
            savedCount = me.savedCount,
            recentThreadTitles = me.recentThreads.map { it.title },
            savedThreadTitles = me.savedThreads.map { it.title }
        )
    }

    /**
     * Posts a comment via GraphQL mutation.
     */
    override suspend fun addComment(userId: String, threadId: String, content: String): ThreadComment {
        val response: GraphQLResponse<AddCommentData> = httpClient.post("$baseUrl/graphql") {
            contentType(ContentType.Application.Json)
            setBody(
                GraphQLAddCommentRequest(
                    query = addCommentMutation,
                    variables = AddCommentVariables(
                        userId = userId,
                        threadId = threadId,
                        content = content
                    )
                )
            )
        }.body()

        val added = response.data?.addComment
            ?: error(response.errors.toErrorMessage("Failed to add comment"))
        // The mutation does not return author info; leave the username blank so
        // callers don't mistake the raw user id for a display name.
        return ThreadComment(content = added.content, authorUsername = "")
    }

    /**
     * Fetches full race detail for the given [slug] via GraphQL query.
     */
    override suspend fun getRaceDetail(slug: String): DataResult<RaceDetail> = try {
        DataResult.Success(fetchRaceDetail(slug))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logError(TAG, "Failed to load race detail for $slug", e)
        DataResult.Failure(e.toDataError())
    }

    private suspend fun fetchRaceDetail(slug: String): RaceDetail {
        val response: GraphQLResponse<RaceDetailData> = httpClient.post("$baseUrl/graphql") {
            contentType(ContentType.Application.Json)
            setBody(GraphQLRaceDetailRequest(query = raceDetailQuery, variables = RaceDetailVariables(slug)))
        }.body()

        val dto = response.data?.race
            ?: error(response.errors.toErrorMessage("Failed to load race detail"))
        return RaceDetail(
            grandPrix = dto.grandPrix,
            circuit = dto.circuit,
            overview = dto.overview,
            trackFacts = dto.trackFacts?.let {
                TrackFacts(laps = it.laps, lapRecord = it.lapRecord, distanceKm = it.distanceKm, corners = it.corners)
            },
            sessions = dto.sessions.map { RaceSession(label = it.label, dateTime = it.dateTime) },
            results = dto.results.map { RaceResult(position = it.position, driver = it.driver, team = it.team, points = it.points, time = it.time) },
            fastestLap = dto.fastestLap?.let { FastestLap(driver = it.driver, time = it.time) }
        )
    }

    /**
     * Toggles the like on the thread identified by [id] via GraphQL mutation.
     *
     * @return The updated like count returned by the server.
     */
    override suspend fun likeThread(id: String): Int {
        val response: GraphQLResponse<LikeThreadData> = httpClient.post("$baseUrl/graphql") {
            contentType(ContentType.Application.Json)
            setBody(
                GraphQLLikeThreadRequest(
                    query = likeThreadMutation,
                    variables = LikeThreadVariables(id = id)
                )
            )
        }.body()

        val result = response.data?.likeThread
            ?: error(response.errors.toErrorMessage("Failed to like thread"))
        return result.likes
    }

    /**
     * Triggers a background sync without blocking the caller. Launched on
     * [syncScope] so it outlives the suspend call that requested it — the
     * caller returns cached data immediately and the refresh lands for next time.
     */
    private fun backgroundSync() {
        syncScope.launch {
            withTimeoutOrNull(SYNC_TIMEOUT_MS.milliseconds) {
                syncDashboard()
            }
        }
    }

    /**
     * Synchronous accessor for race count (for compatibility with the interface).
     *
     * WARNING: This blocks the current thread. Migrate callers to use
     * [getRaceSchedule] with suspend/async instead.
     *
     * @return The number of races in the local cache
     */
    override fun getRaceCount(): Int = localDataSource.getAllRaces().size

    /**
     * Synchronous accessor for a specific race (for compatibility with the interface).
     *
     * WARNING: This blocks the current thread. Migrate callers to use
     * [getRaceSchedule] with suspend/async instead.
     */
    override fun getRace(index: Int): Race = localDataSource.getAllRaces().getOrNull(index)
        ?: throw IndexOutOfBoundsException("Race at index $index not found")

    /**
     * Synchronous accessor for standing count (for compatibility with the interface).
     *
     * WARNING: This blocks the current thread. Migrate callers to use
     * [getDriverStandings] with suspend/async instead.
     */
    override fun getStandingCount(): Int = localDataSource.getAllDriverStandings().size

    /**
     * Synchronous accessor for a specific standing (for compatibility with the interface).
     *
     * WARNING: This blocks the current thread. Migrate callers to use
     * [getDriverStandings] with suspend/async instead.
     */
    override fun getStanding(index: Int): DriverStanding = localDataSource.getAllDriverStandings().getOrNull(index)
        ?: throw IndexOutOfBoundsException("Standing at index $index not found")
}
