package org.gce.racehub.race

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gce.racehub.race.domain.usecase.GetConstructorStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetDriverStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetRaceScheduleUseCase
import org.gce.racehub.race.domain.usecase.GetTrendingThreadsUseCase

class RaceViewModel(
    private val getRaceScheduleUseCase: GetRaceScheduleUseCase,
    private val getDriverStandingsUseCase: GetDriverStandingsUseCase,
    private val getConstructorStandingsUseCase: GetConstructorStandingsUseCase,
    private val getTrendingThreadsUseCase: GetTrendingThreadsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RaceState())
    val state: StateFlow<RaceState> = _state.asStateFlow()

    private val _effect = Channel<RaceEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
    }

    fun onIntent(intent: RaceIntent) {
        when (intent) {
            is RaceIntent.Refresh ->
                loadData()

            is RaceIntent.DismissError ->
                _state.update { it.copy(errorMessage = null) }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val schedule = getRaceScheduleUseCase()
                val drivers = getDriverStandingsUseCase()
                val constructors = getConstructorStandingsUseCase()
                val trending = getTrendingThreadsUseCase()
                _state.update {
                    it.copy(
                        isLoading = false,
                        raceSchedule = schedule,
                        driverStandings = drivers,
                        constructorStandings = constructors,
                        trendingThreads = trending
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load data. Pull to refresh."
                    )
                }
            }
        }
    }
}
