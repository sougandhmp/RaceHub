package org.gce.racehub.race.domain.repository

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.TrendingThread

/**
 * Contract for race data: schedule, standings, trending threads and race detail. Implementations are the
 * error boundary: failures come back as [DataResult.Failure], never as exceptions.
 */
internal interface RaceRepository {

    // ── Race reads: offline-first, errors as values ─────────────────────────
    // Each returns cached rows when a refresh fails, and a Failure only when
    // there is nothing to show.

    /** The full race calendar for the current season. */
    @Throws(Exception::class)
    suspend fun getRaceSchedule(): DataResult<List<Race>>

    /** The current Drivers' Championship standings table. */
    @Throws(Exception::class)
    suspend fun getDriverStandings(): DataResult<List<DriverStanding>>

    /** The current Constructors' Championship standings table. */
    @Throws(Exception::class)
    suspend fun getConstructorStandings(): DataResult<List<ConstructorStanding>>

    /** The trending threads from the forum. */
    @Throws(Exception::class)
    suspend fun getTrendingThreads(): DataResult<List<TrendingThread>>

    /** Full detail for the race identified by [slug]. Not cached. */
    @Throws(Exception::class)
    suspend fun getRaceDetail(slug: String): DataResult<RaceDetail>
}
