package org.gce.racehub.race.domain.usecase

import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.repository.HomeRepository

/**
 * Retrieves the current Drivers' Championship standings from [HomeRepository].
 *
 * Kept as a dedicated use case so sorting, filtering by team, or pagination
 * logic can be added here later without touching the repository or the ViewModel.
 *
 * Shared between Android and iOS via the KMP `shared` module.
 */
class GetDriverStandingsUseCase(private val repository: HomeRepository) {

    /**
     * @return The [DriverStanding] list sorted by championship position
     *         (position 1 at index 0).
     */
    @Throws(Exception::class)
    suspend operator fun invoke(): List<DriverStanding> = repository.getDriverStandings()
}
