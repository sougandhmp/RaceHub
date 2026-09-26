package org.gce.racehub.race.domain.usecase

import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.repository.RaceRepository

/**
 * Returns the cached rows once. For callers that can't collect a Flow (Swift):
 * run [RefreshRaceDataUseCase] first to get fresh data.
 */
class GetRaceScheduleUseCase(private val repository: RaceRepository) {
    @Throws(Exception::class)
    suspend operator fun invoke(): List<Race> = repository.getCachedRaceSchedule()
}
