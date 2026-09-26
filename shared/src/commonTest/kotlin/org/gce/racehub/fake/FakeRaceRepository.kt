package org.gce.racehub.fake

import org.gce.racehub.core.domain.DataError
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.TrendingThread
import org.gce.racehub.race.domain.repository.RaceRepository

internal class FakeRaceRepository : RaceRepository {
    var raceSchedule: List<Race> = emptyList()
    var driverStandings: List<DriverStanding> = emptyList()
    var constructorStandings: List<ConstructorStanding> = emptyList()
    var trendingThreads: List<TrendingThread> = emptyList()
    var raceDetailResult: RaceDetail = RaceDetail("GP", "Circuit", null, null, emptyList(), emptyList(), null)

    /** Returned as a Failure by [getRaceSchedule] / [getRaceDetail] when set. */
    var raceScheduleError: DataError? = null
    var raceDetailError: DataError? = null
    var raceScheduleCalls = 0
    /** Per-slug detail and artificial latency, overriding [raceDetailResult]. */
    var raceDetailBySlug: Map<String, RaceDetail> = emptyMap()
    var raceDetailDelayMs: Map<String, Long> = emptyMap()

    override suspend fun getRaceSchedule(): DataResult<List<Race>> {
        raceScheduleCalls++
        raceScheduleError?.let { return DataResult.Failure(it) }
        return DataResult.Success(raceSchedule)
    }
    override suspend fun getDriverStandings(): DataResult<List<DriverStanding>> = DataResult.Success(driverStandings)
    override suspend fun getConstructorStandings(): DataResult<List<ConstructorStanding>> = DataResult.Success(constructorStandings)
    override suspend fun getTrendingThreads(): DataResult<List<TrendingThread>> = DataResult.Success(trendingThreads)
    override suspend fun getRaceDetail(slug: String): DataResult<RaceDetail> {
        raceDetailDelayMs[slug]?.let { kotlinx.coroutines.delay(it) }
        raceDetailError?.let { return DataResult.Failure(it) }
        return DataResult.Success(raceDetailBySlug[slug] ?: raceDetailResult)
    }
}
