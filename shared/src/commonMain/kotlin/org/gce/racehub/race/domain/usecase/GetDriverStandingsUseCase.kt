package org.gce.racehub.race.domain.usecase

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.repository.RaceRepository

/**
 * Retrieves the current Drivers' Championship standings from [RaceRepository].
 *
 * Kept as a dedicated use case so sorting, filtering by team, or pagination
 * logic can be added here later without touching the repository or the ViewModel.
 *
 * Shared between Android and iOS via the KMP `shared` module.
 */
internal class GetDriverStandingsUseCase(private val repository: RaceRepository) {

    /**
     * @return The [DriverStanding] list sorted by championship position
     *         (position 1 at index 0).
     */
    @Throws(Exception::class)
    suspend operator fun invoke(): DataResult<List<DriverStanding>> = repository.getDriverStandings()
}
