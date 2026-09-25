package org.gce.racehub.race.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.gce.racehub.core.DataResult
import org.gce.racehub.core.SingleFlight
import org.gce.racehub.core.safeCall
import org.gce.racehub.db.LocalDataSource
import org.gce.racehub.race.data.dto.*
import org.gce.racehub.race.data.network.GraphQLClient
import org.gce.racehub.race.domain.model.*
import org.gce.racehub.race.domain.repository.RaceRepository
import org.gce.racehub.util.platformIoDispatcher

/**
 * [RaceRepository] backed by the GraphQL API and the local SQLDelight cache.
 *
 * Refreshes run through [SingleFlight], so the Race tab asking for standings and
 * trending threads at the same time sends one dashboard request, not three. They run
 * in [refreshScope] rather than the caller's coroutine, so a caller leaving the screen
 * doesn't cancel a refresh another caller is waiting for.
 */
class RaceRepositoryImpl(
    private val graphQL: GraphQLClient,
    private val localDataSource: LocalDataSource,
    private val ioDispatcher: CoroutineDispatcher = platformIoDispatcher,
    refreshScope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)
) : RaceRepository {

    private companion object {
        const val TAG = "RaceRepository"
    }

    private val scheduleRefresh = SingleFlight<DataResult<Unit>>(refreshScope)
    private val dashboardRefresh = SingleFlight<DataResult<Unit>>(refreshScope)

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

    override fun observeRaceSchedule(): Flow<List<Race>> = localDataSource.observeRaces()
    override fun observeDriverStandings(): Flow<List<DriverStanding>> = localDataSource.observeDriverStandings()
    override fun observeConstructorStandings(): Flow<List<ConstructorStanding>> = localDataSource.observeConstructorStandings()
    override fun observeTrendingThreads(): Flow<List<TrendingThread>> = localDataSource.observeTrendingThreads()

    override suspend fun getCachedRaceSchedule(): List<Race> =
        withContext(ioDispatcher) { localDataSource.getAllRaces() }

    override suspend fun getCachedDriverStandings(): List<DriverStanding> =
        withContext(ioDispatcher) { localDataSource.getAllDriverStandings() }

    override suspend fun getCachedConstructorStandings(): List<ConstructorStanding> =
        withContext(ioDispatcher) { localDataSource.getAllConstructorStandings() }

    override suspend fun getCachedTrendingThreads(): List<TrendingThread> =
        withContext(ioDispatcher) { localDataSource.getAllTrendingThreads() }

    override suspend fun refreshRaceSchedule(): DataResult<Unit> = scheduleRefresh.run {
        safeCall(TAG) {
            val data: RacesData = graphQL.execute(GraphQLRequest(racesQuery), "Couldn't load the race calendar.")
            localDataSource.saveRaces(data.races.map { dto ->
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
        }
    }

    override suspend fun refreshDashboard(): DataResult<Unit> = dashboardRefresh.run {
        safeCall(TAG) {
            val data: DashboardData = graphQL.execute(GraphQLRequest(dashboardQuery), "Couldn't load standings.")
            saveDashboard(data.dashboard)
        }
    }

    override suspend fun getRaceDetail(slug: String): DataResult<RaceDetail> = safeCall(TAG) {
        val data: RaceDetailData = graphQL.execute(
            GraphQLRaceDetailRequest(query = raceDetailQuery, variables = RaceDetailVariables(slug)),
            "Couldn't load race detail."
        )
        val dto = data.race
        RaceDetail(
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
     * Saves standings and trending threads. An empty list from the server leaves the cached
     * rows in place. The races table is owned by [refreshRaceSchedule]: the dashboard only
     * carries one summary race, and saving it would wipe the calendar down to one row.
     */
    private fun saveDashboard(dashboard: DashboardContent) {
        val driverStandings = dashboard.driverStandings.map { dto ->
            DriverStanding(
                position = dto.position,
                driverName = dto.name,
                team = dto.team.orEmpty(),
                points = dto.points,
                wins = dto.wins
            )
        }
        if (driverStandings.isNotEmpty()) localDataSource.saveDriverStandings(driverStandings)

        val constructorStandings = dashboard.constructorStandings.map { dto ->
            ConstructorStanding(position = dto.position, name = dto.name, points = dto.points, wins = dto.wins)
        }
        if (constructorStandings.isNotEmpty()) localDataSource.saveConstructorStandings(constructorStandings)

        val trendingThreads = dashboard.trendingThreads.map { dto ->
            TrendingThread(id = dto.id, title = dto.title, likes = dto.likes, createdAt = dto.createdAt)
        }
        if (trendingThreads.isNotEmpty()) localDataSource.saveTrendingThreads(trendingThreads)
    }
}
