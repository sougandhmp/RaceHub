package org.gce.racehub.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.usecase.ObserveConstructorStandingsUseCase
import org.gce.racehub.race.domain.usecase.ObserveDriverStandingsUseCase
import org.gce.racehub.race.domain.usecase.ObserveRaceScheduleUseCase

/** Full season calendar for the Schedule screen, read from the cache and sorted by round. */
class ScheduleViewModel(observeRaceSchedule: ObserveRaceScheduleUseCase) : ViewModel() {
    val schedule: StateFlow<List<Race>> = observeRaceSchedule()
        .map { races -> races.sortedBy { it.round } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

/** Full driver and constructor standings for the Standings screen, read from the cache. */
class StandingsViewModel(
    observeDriverStandings: ObserveDriverStandingsUseCase,
    observeConstructorStandings: ObserveConstructorStandingsUseCase
) : ViewModel() {
    val drivers: StateFlow<List<DriverStanding>> = observeDriverStandings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val constructors: StateFlow<List<ConstructorStanding>> = observeConstructorStandings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
