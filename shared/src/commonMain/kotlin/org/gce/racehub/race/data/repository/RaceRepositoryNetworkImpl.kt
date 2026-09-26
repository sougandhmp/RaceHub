package org.gce.racehub.race.data.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.gce.racehub.core.data.GraphQLRequest
import org.gce.racehub.core.data.GraphQLResponse
import org.gce.racehub.core.data.safeCall
import org.gce.racehub.core.data.toDataError
import org.gce.racehub.core.data.toErrorMessage
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.db.LocalDataSource
import org.gce.racehub.race.data.dto.*
import org.gce.racehub.race.domain.model.*
import org.gce.racehub.race.domain.repository.RaceRepository
import org.gce.racehub.util.logError
import org.gce.racehub.util.platformIoDispatcher
import kotlin.concurrent.Volatile
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Network-based implementation of [RaceRepository].
 *
 * Fetches F1 racing data (races, standings, trending threads) from a GraphQL backend,
 * caches results in a local database, and syncs in the background.
 *
 * Architecture:
 * - Primary data source: Local database (fast UI updates)
 * - Secondary: Network (background refresh)
 * - Eventual consistency: Data updates asynchronously without blocking UI
 */
internal class RaceRepositoryNetworkImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val localDataSource: LocalDataSource,
    private val ioDispatcher: CoroutineDispatcher = platformIoDispatcher
) : RaceRepository {

    companion object {
        private const val TAG = "RaceRepository"
        private const val SYNC_TIMEOUT_MS = 5_000L

        /** A successful dashboard sync is reused for this long before a background refresh. */
        internal val DASHBOARD_FRESH_FOR = 30.seconds
    }

    /**
     * Long-lived scope for fire-and-forget background refreshes. Survives the
     * suspend call that triggered it so cached data can be returned immediately
     * while the network sync completes off the caller's critical path.
     */
    private val syncScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // One dashboard request serves drivers, constructors and trending threads, so
    // those three reads share it: a running sync is joined, and a recent
    // successful one is reused instead of firing again.
    private val dashboardSyncLock = Mutex()
    private var dashboardSyncInFlight: Deferred<DataError?>? = null
    @Volatile private var dashboardFreshUntil: TimeMark? = null

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
            dashboardFreshUntil = TimeSource.Monotonic.markNow() + DASHBOARD_FRESH_FOR
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
    override suspend fun getRaceSchedule(): DataResult<List<Race>, DataError> = withContext(ioDispatcher) {
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
    override suspend fun getDriverStandings(): DataResult<List<DriverStanding>, DataError> =
        dashboardRead { localDataSource.getAllDriverStandings() }

    /**
     * Returns constructor standings, refreshing from the network if empty.
     * Subsequent calls trigger background syncs without blocking.
     */
    override suspend fun getConstructorStandings(): DataResult<List<ConstructorStanding>, DataError> =
        dashboardRead { localDataSource.getAllConstructorStandings() }

    /**
     * Returns trending threads, refreshing from the network if empty.
     * Subsequent calls trigger background syncs without blocking.
     */
    override suspend fun getTrendingThreads(): DataResult<List<TrendingThread>, DataError> =
        dashboardRead { localDataSource.getAllTrendingThreads() }

    /**
     * Reads dashboard-backed rows: on a cold cache waits for a dashboard sync,
     * otherwise returns the cache and refreshes in the background.
     */
    private suspend fun <T> dashboardRead(read: () -> List<T>): DataResult<List<T>, DataError> =
        withContext(ioDispatcher) {
            val syncError = if (read().isEmpty()) {
                dashboardSync().await()
            } else {
                // Warm cache: return it now; refresh in the background unless recently synced.
                if (dashboardFreshUntil?.hasNotPassedNow() != true) dashboardSync()
                null
            }
            // Re-read after a cold-cache sync so freshly-saved rows are returned.
            cachedOrFailure(read(), syncError)
        }

    /** Cached rows win over a sync error; only an empty cache surfaces the failure. */
    private fun <T> cachedOrFailure(rows: List<T>, syncError: DataError?): DataResult<List<T>, DataError> =
        if (rows.isEmpty() && syncError != null) DataResult.Failure(syncError) else DataResult.Success(rows)

    /**
     * Fetches full race detail for the given [slug] via GraphQL query.
     */
    override suspend fun getRaceDetail(slug: String): DataResult<RaceDetail, DataError> =
        safeCall(TAG, "load race detail for $slug") { fetchRaceDetail(slug) }

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
     * Starts a dashboard sync on [syncScope], or returns the one already running.
     * Runs on [syncScope] so a caller that doesn't await it (background refresh)
     * can return immediately while the sync still lands in the cache.
     */
    private suspend fun dashboardSync(): Deferred<DataError?> = dashboardSyncLock.withLock {
        dashboardSyncInFlight?.takeIf { it.isActive }
            ?: syncScope.async { syncDashboard() }.also { dashboardSyncInFlight = it }
    }
}
