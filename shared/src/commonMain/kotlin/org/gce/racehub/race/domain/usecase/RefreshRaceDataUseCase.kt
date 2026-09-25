package org.gce.racehub.race.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.gce.racehub.core.DataResult
import org.gce.racehub.race.domain.repository.RaceRepository

/**
 * Refreshes the race calendar and the dashboard (standings and trending threads) in parallel.
 *
 * New rows are saved to the cache, so anything observing it updates automatically.
 * Returns the first failure, if any; data from the refresh that succeeded is still saved.
 */
class RefreshRaceDataUseCase(private val repository: RaceRepository) {
    @Throws(Exception::class)
    suspend operator fun invoke(): DataResult<Unit> = coroutineScope {
        val schedule = async { repository.refreshRaceSchedule() }
        val dashboard = async { repository.refreshDashboard() }
        listOf(schedule.await(), dashboard.await()).firstOrNull { !it.isSuccess } ?: DataResult.success(Unit)
    }
}
