package org.gce.racehub.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.gce.racehub.core.DataResult
import org.gce.racehub.race.domain.model.*
import org.gce.racehub.race.domain.repository.RaceRepository

class FakeRaceRepository : RaceRepository {
    val raceSchedule = MutableStateFlow<List<Race>>(emptyList())
    val driverStandings = MutableStateFlow<List<DriverStanding>>(emptyList())
    val constructorStandings = MutableStateFlow<List<ConstructorStanding>>(emptyList())
    val trendingThreads = MutableStateFlow<List<TrendingThread>>(emptyList())

    var scheduleRefreshResult: DataResult<Unit> = DataResult.success(Unit)
    var dashboardRefreshResult: DataResult<Unit> = DataResult.success(Unit)
    var raceDetailResult: DataResult<RaceDetail> =
        DataResult.success(RaceDetail("GP", "Circuit", null, null, emptyList(), emptyList(), null))

    var scheduleRefreshCount = 0
    var dashboardRefreshCount = 0
    var lastRaceDetailSlug: String? = null

    override fun observeRaceSchedule(): Flow<List<Race>> = raceSchedule
    override fun observeDriverStandings(): Flow<List<DriverStanding>> = driverStandings
    override fun observeConstructorStandings(): Flow<List<ConstructorStanding>> = constructorStandings
    override fun observeTrendingThreads(): Flow<List<TrendingThread>> = trendingThreads

    override suspend fun getCachedRaceSchedule(): List<Race> = raceSchedule.value
    override suspend fun getCachedDriverStandings(): List<DriverStanding> = driverStandings.value
    override suspend fun getCachedConstructorStandings(): List<ConstructorStanding> = constructorStandings.value
    override suspend fun getCachedTrendingThreads(): List<TrendingThread> = trendingThreads.value

    override suspend fun refreshRaceSchedule(): DataResult<Unit> {
        scheduleRefreshCount++
        return scheduleRefreshResult
    }

    override suspend fun refreshDashboard(): DataResult<Unit> {
        dashboardRefreshCount++
        return dashboardRefreshResult
    }

    override suspend fun getRaceDetail(slug: String): DataResult<RaceDetail> {
        lastRaceDetailSlug = slug
        return raceDetailResult
    }
}
