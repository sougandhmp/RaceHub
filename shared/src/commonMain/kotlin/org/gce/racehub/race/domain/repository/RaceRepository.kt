package org.gce.racehub.race.domain.repository

import kotlinx.coroutines.flow.Flow
import org.gce.racehub.core.DataResult
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.TrendingThread

/**
 * Race calendar, standings and trending threads, cached in the local database.
 *
 * The local database is the single source of truth:
 * - `observe…()` emits the cached rows and emits again whenever a refresh saves new ones,
 *   so a screen that observes them updates on its own after a refresh.
 * - `refresh…()` fetches from the network and saves to the database. Concurrent calls
 *   share one request, and failures come back as a [DataResult] error instead of throwing.
 * - `getCached…()` returns the current rows once, for callers that can't collect a Flow (Swift).
 */
interface RaceRepository {

    fun observeRaceSchedule(): Flow<List<Race>>
    fun observeDriverStandings(): Flow<List<DriverStanding>>
    fun observeConstructorStandings(): Flow<List<ConstructorStanding>>
    fun observeTrendingThreads(): Flow<List<TrendingThread>>

    suspend fun getCachedRaceSchedule(): List<Race>
    suspend fun getCachedDriverStandings(): List<DriverStanding>
    suspend fun getCachedConstructorStandings(): List<ConstructorStanding>
    suspend fun getCachedTrendingThreads(): List<TrendingThread>

    /** Fetches the full race calendar and saves it. */
    suspend fun refreshRaceSchedule(): DataResult<Unit>

    /** Fetches driver standings, constructor standings and trending threads (one request) and saves them. */
    suspend fun refreshDashboard(): DataResult<Unit>

    /** Fetches the detail of one race. Not cached. */
    suspend fun getRaceDetail(slug: String): DataResult<RaceDetail>
}
