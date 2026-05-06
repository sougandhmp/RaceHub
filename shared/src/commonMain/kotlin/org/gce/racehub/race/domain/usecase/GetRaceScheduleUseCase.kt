package org.gce.racehub.race.domain.usecase

import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.repository.HomeRepository

/**
 * Retrieves the full race calendar from [HomeRepository].
 *
 * Kept as a dedicated use case so filtering, sorting, or caching logic
 * can be added here later without touching the repository or the ViewModel.
 *
 * Shared between Android and iOS via the KMP `shared` module.
 */
class GetRaceScheduleUseCase(private val repository: HomeRepository) {

    /**
     * @return The ordered list of [Race] events for the current season,
     *         completed events first, then upcoming in chronological order.
     */
    suspend operator fun invoke(): List<Race> = repository.getRaceSchedule()
}
