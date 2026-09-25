package org.gce.racehub.race.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.repository.RaceRepository

/** Emits the cached rows now and again after every refresh. Use from Kotlin (Android). */
class ObserveRaceScheduleUseCase(private val repository: RaceRepository) {
    operator fun invoke(): Flow<List<Race>> = repository.observeRaceSchedule()
}
