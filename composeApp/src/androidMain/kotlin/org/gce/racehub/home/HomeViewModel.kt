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
import org.gce.racehub.race.data.repository.HomeRepositoryImpl
import org.gce.racehub.race.domain.usecase.GetDriverStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetRaceScheduleUseCase

/**
 * ViewModel for the Home screen following the MVI pattern.
 *
 * - Exposes [state] as an immutable [StateFlow] the View observes.
 * - Accepts user actions via [onIntent] and processes them into state mutations.
 * - Emits one-time navigation events through [effect].
 *
 * Data is loaded eagerly in [init] so the screen is populated immediately
 * when first displayed.
 */
class HomeViewModel : ViewModel() {

    private val repository = HomeRepositoryImpl()

    /** Use case that returns the race calendar from the repository. */
    private val getRaceScheduleUseCase = GetRaceScheduleUseCase(repository)

    /** Use case that returns the championship standings from the repository. */
    private val getDriverStandingsUseCase = GetDriverStandingsUseCase(repository)

    private val _state = MutableStateFlow(HomeState())

    /** Observable UI state. Collected by the View via [collectAsStateWithLifecycle]. */
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // Channel is used instead of SharedFlow so each effect is consumed exactly once,
    // even if the collector is briefly inactive during recomposition.
    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)

    /** Stream of one-time side effects (navigation, etc.). */
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
    }

    /**
     * Entry point for all View interactions.
     * Maps each [HomeIntent] to a state mutation or a command.
     */
    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.TabSelected ->
                _state.update { it.copy(selectedTab = intent.tab) }

            is HomeIntent.Refresh ->
                loadData()
        }
    }

    /**
     * Fetches race schedule and driver standings concurrently, then updates state.
     * Runs inside [viewModelScope] to automatically cancel on ViewModel destruction.
     */
    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val schedule = getRaceScheduleUseCase.execute()
            val standings = getDriverStandingsUseCase.execute()

            _state.update {
                it.copy(
                    isLoading = false,
                    raceSchedule = schedule,
                    driverStandings = standings
                )
            }
        }
    }
}
