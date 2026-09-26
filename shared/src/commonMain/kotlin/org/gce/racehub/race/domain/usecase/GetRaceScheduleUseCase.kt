package org.gce.racehub.race.domain.usecase

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.core.domain.map
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.repository.RaceRepository

/**
 * Retrieves the full race calendar from [RaceRepository].
 *
 * Kept as a dedicated use case so filtering, sorting, or caching logic
 * can be added here later without touching the repository or the ViewModel.
 *
 * Shared between Android and iOS via the KMP `shared` module.
 */
internal class GetRaceScheduleUseCase(private val repository: RaceRepository) {

    /** The season's races ordered by round, or why they could not be loaded. */
    @Throws(Exception::class)
    suspend operator fun invoke(): DataResult<List<Race>> =
        repository.getRaceSchedule().map { races -> races.sortedBy { it.round } }
}
