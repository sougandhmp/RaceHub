package org.gce.racehub.race.data.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.gce.racehub.db.LocalDataSource
import org.gce.racehub.race.data.dto.*
import org.gce.racehub.race.domain.model.*
import org.gce.racehub.race.domain.repository.HomeRepository

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
    private val localDataSource: LocalDataSource
) : HomeRepository {

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
     * Syncs all dashboard data from the network to the local database.
     * Runs silently without blocking; errors are logged but not thrown.
     */
    private suspend fun syncDashboard() {
        try {
            val response: GraphQLResponse<DashboardData> = httpClient.post("$baseUrl/graphql") {
                contentType(ContentType.Application.Json)
                setBody(GraphQLRequest(dashboardQuery))
            }.body()

            val dashboard = response.data?.dashboard
            if (dashboard == null) {
                println("Failed to sync dashboard: ${response.errors?.joinToString { it.message } ?: "no data"}")
                return
            }
            saveDashboardData(dashboard)
        } catch (e: Exception) {
            println("Failed to sync dashboard: ${e.message}")
        }
    }

    /**
     * Processes and saves all dashboard data from the GraphQL response.
     */
    private fun saveDashboardData(dashboard: DashboardContent) {
        // Save races
        val races = buildRacesList(dashboard.upcomingRace)
        if (races.isNotEmpty()) {
            localDataSource.saveRaces(races)
        }

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
     * Builds a list of races from upcoming and latest race data.
     */
    private fun buildRacesList(
        upcomingRace: UpcomingRaceDto?
    ): List<Race> {
        val races = mutableListOf<Race>()

        upcomingRace?.let {
            races.add(
                Race(
                    id = "upcoming",
                    name = it.grandPrix,
                    circuit = "",
                    country = it.city,
                    countryFlag = "",
                    date = it.dateTime,
                    round = 0,
                    isCompleted = false,
                    daysRemaining = null
                )
            )
        }

        return races
    }

    /**
     * Returns the race schedule, refreshing from the network if empty.
     * Subsequent calls trigger background syncs without blocking.
     */
    override suspend fun getRaceSchedule(): List<Race> {
        val localRaces = localDataSource.getAllRaces()
        if (localRaces.isEmpty()) {
            syncDashboard()
        } else {
            // Trigger background refresh
            backgroundSync()
        }
        return localDataSource.getAllRaces()
    }

    /**
     * Returns driver standings, refreshing from the network if empty.
     * Subsequent calls trigger background syncs without blocking.
     */
    override suspend fun getDriverStandings(): List<DriverStanding> {
        val localStandings = localDataSource.getAllDriverStandings()
        if (localStandings.isEmpty()) {
            syncDashboard()
        } else {
            backgroundSync()
        }
        return localDataSource.getAllDriverStandings()
    }

    /**
     * Returns constructor standings, refreshing from the network if empty.
     * Subsequent calls trigger background syncs without blocking.
     */
    override suspend fun getConstructorStandings(): List<ConstructorStanding> {
        val localStandings = localDataSource.getAllConstructorStandings()
        if (localStandings.isEmpty()) {
            syncDashboard()
        } else {
            backgroundSync()
        }
        return localStandings
    }

    /**
     * Returns trending threads, refreshing from the network if empty.
     * Subsequent calls trigger background syncs without blocking.
     */
    override suspend fun getTrendingThreads(): List<TrendingThread> {
        val localThreads = localDataSource.getAllTrendingThreads()
        if (localThreads.isEmpty()) {
            syncDashboard()
        } else {
            backgroundSync()
        }
        return localThreads
    }

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
                ?: error(response.errors?.joinToString { it.message } ?: "Empty GraphQL response")

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
            println("Failed to fetch threads: ${e.message}")
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
            ?: error(response.errors?.joinToString { it.message } ?: "Failed to create thread")
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
     * Triggers a background sync without blocking the caller.
     * Used to refresh data asynchronously.
     */
    private suspend fun backgroundSync() {
        try {
            withTimeoutOrNull(5000) {
                syncDashboard()
            }
        } catch (_: CancellationException) {
            // Timeout or cancellation - gracefully ignore
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
