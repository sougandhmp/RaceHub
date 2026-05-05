package org.gce.racehub.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gce.racehub.race.domain.usecase.GetDriverStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetRaceScheduleUseCase

class HomeViewModel(
    private val getRaceScheduleUseCase: GetRaceScheduleUseCase,
    private val getDriverStandingsUseCase: GetDriverStandingsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.TabSelected ->
                _state.update { it.copy(selectedTab = intent.tab) }

            is HomeIntent.Refresh ->
                loadData()

            is HomeIntent.DismissError ->
                _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val schedule = getRaceScheduleUseCase.execute()
                val standings = getDriverStandingsUseCase.execute()
                _state.update {
                    it.copy(
                        isLoading = false,
                        raceSchedule = schedule,
                        driverStandings = standings
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
