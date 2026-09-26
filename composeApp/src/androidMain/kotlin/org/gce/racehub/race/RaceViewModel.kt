package org.gce.racehub.race

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gce.racehub.race.domain.usecase.GetRaceDetailUseCase
import org.gce.racehub.race.domain.usecase.ObserveConstructorStandingsUseCase
import org.gce.racehub.race.domain.usecase.ObserveDriverStandingsUseCase
import org.gce.racehub.race.domain.usecase.ObserveRaceScheduleUseCase
import org.gce.racehub.race.domain.usecase.ObserveTrendingThreadsUseCase
import org.gce.racehub.race.domain.usecase.RefreshRaceDataUseCase

/**
 * MVI ViewModel for the Race tab.
 *
 * The screen shows whatever is in the local cache: the observe use cases emit the cached
 * rows straight away and again whenever a refresh saves new ones. [RefreshRaceDataUseCase]
 * fetches the calendar and the dashboard in parallel (one request each, however many
 * callers ask), so pull-to-refresh updates every section, not just the calendar.
 */
class RaceViewModel(
    observeRaceSchedule: ObserveRaceScheduleUseCase,
    observeDriverStandings: ObserveDriverStandingsUseCase,
    observeConstructorStandings: ObserveConstructorStandingsUseCase,
    observeTrendingThreads: ObserveTrendingThreadsUseCase,
    private val refreshRaceData: RefreshRaceDataUseCase,
    private val getRaceDetailUseCase: GetRaceDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RaceState())
    val state: StateFlow<RaceState> = _state.asStateFlow()

    private val _effect = Channel<RaceEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        combine(
            observeRaceSchedule(),
            observeDriverStandings(),
            observeConstructorStandings(),
            observeTrendingThreads()
        ) { schedule, drivers, constructors, trending ->
            _state.update {
                it.copy(
                    raceSchedule = schedule.sortedBy { race -> race.round },
                    driverStandings = drivers,
                    constructorStandings = constructors,
                    trendingThreads = trending
                )
            }
        }.launchIn(viewModelScope)

        // Load the next race's detail whenever the next race changes (first load, or after a refresh).
        _state.map { state -> state.raceSchedule.firstOrNull { !it.isCompleted }?.id }
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { slug -> fetchNextRaceDetail(slug) }
            .launchIn(viewModelScope)

        refresh()
    }

    fun onIntent(intent: RaceIntent) {
        when (intent) {
            is RaceIntent.Refresh ->
                refresh()
            is RaceIntent.DismissError ->
                _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = refreshRaceData()
            _state.update { it.copy(isLoading = false, errorMessage = result.error?.message) }
        }
    }

    private fun fetchNextRaceDetail(slug: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingDetail = true) }
            val result = getRaceDetailUseCase(slug)
            // Detail is optional on the Race tab: keep the card without it if the request fails.
            _state.update { it.copy(isLoadingDetail = false, nextRaceDetail = result.data ?: it.nextRaceDetail) }
        }
    }
}
