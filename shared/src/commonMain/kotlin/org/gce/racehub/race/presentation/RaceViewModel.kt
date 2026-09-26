package org.gce.racehub.race.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.usecase.GetConstructorStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetDriverStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetRaceDetailUseCase
import org.gce.racehub.race.domain.usecase.GetRaceScheduleUseCase
import org.gce.racehub.race.domain.usecase.GetTrendingThreadsUseCase

/**
 * Shared MVI ViewModel for the Race tab, used by both Android (Compose) and
 * iOS (SwiftUI via KMP-NativeCoroutines).
 *
 * Handles [RaceIntent]s by running use cases and emitting [RaceMutation]s,
 * which [RaceReducer] folds into [state]. One-off events go to [effects].
 * Loads once on creation; [RaceIntent.Refresh] reloads.
 */
class RaceViewModel internal constructor(
    private val getRaceSchedule: GetRaceScheduleUseCase,
    private val getDriverStandings: GetDriverStandingsUseCase,
    private val getConstructorStandings: GetConstructorStandingsUseCase,
    private val getTrendingThreads: GetTrendingThreadsUseCase,
    private val getRaceDetail: GetRaceDetailUseCase,
    /** Zone sessions are shown in; tests pass a fixed one. */
    timeZone: TimeZone
) : ViewModel() {

    /** Shows sessions in the device's time zone. (Internal ctor keeps kotlinx-datetime out of the Swift API.) */
    constructor(
        getRaceSchedule: GetRaceScheduleUseCase,
        getDriverStandings: GetDriverStandingsUseCase,
        getConstructorStandings: GetConstructorStandingsUseCase,
        getTrendingThreads: GetTrendingThreadsUseCase,
        getRaceDetail: GetRaceDetailUseCase
    ) : this(
        getRaceSchedule, getDriverStandings, getConstructorStandings, getTrendingThreads, getRaceDetail,
        TimeZone.currentSystemDefault()
    )

    private val reducer = RaceReducer(timeZone)

    private val _state = MutableStateFlow(RaceState())

    /** Swift sees `state` (current value) and `stateFlow` (for asyncSequence) via KMP-NativeCoroutines. */
    @NativeCoroutinesState
    val state: StateFlow<RaceState> = _state.asStateFlow()

    private val _effects = Channel<RaceEffect>(Channel.BUFFERED)

    /** One-off events; collect while the screen is visible. Swift sees `effects` as a native flow. */
    @NativeCoroutines
    val effects: Flow<RaceEffect> = _effects.receiveAsFlow()

    private var loadJob: Job? = null
    private var selectedDetailJob: Job? = null

    init {
        load()
    }

    fun onIntent(intent: RaceIntent) {
        when (intent) {
            RaceIntent.Refresh -> load()
            is RaceIntent.SelectRace -> selectRace(intent.slug)
        }
    }

    private fun mutate(mutation: RaceMutation) = _state.update { reducer.reduce(it, mutation) }

    private fun load() {
        // A refresh supersedes a load in flight rather than racing it.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            mutate(RaceMutation.LoadStarted)
            // Sequential on purpose: on a cold cache the first call populates the
            // dashboard that the standings/trending calls then read.
            val schedule = getRaceSchedule()
            val drivers = getDriverStandings()
            val constructors = getConstructorStandings()
            val trending = getTrendingThreads()

            val failure = listOf(schedule, drivers, constructors, trending)
                .firstNotNullOfOrNull { it as? DataResult.Failure }
            if (failure != null) {
                mutate(RaceMutation.LoadFailed)
                _effects.send(RaceEffect.ShowLoadError(failure.error))
                return@launch
            }
            mutate(
                RaceMutation.Loaded(
                    schedule = schedule.dataOrEmpty(),
                    drivers = drivers.dataOrEmpty(),
                    constructors = constructors.dataOrEmpty(),
                    trending = trending.dataOrEmpty()
                )
            )
            _state.value.nextRace?.let { loadNextRaceDetail(it) }
        }
    }

    private suspend fun loadNextRaceDetail(race: Race) {
        mutate(RaceMutation.NextRaceDetailStarted)
        mutate(RaceMutation.NextRaceDetailLoaded(getRaceDetail(race.id).dataOrNull()))
    }

    private fun selectRace(slug: String) {
        // Cancel the previous request so a slow response for an earlier race
        // can never overwrite the detail of the race now on screen.
        selectedDetailJob?.cancel()
        mutate(RaceMutation.RaceSelected(_state.value.raceSchedule.firstOrNull { it.id == slug }))
        selectedDetailJob = viewModelScope.launch {
            mutate(RaceMutation.SelectedRaceDetailLoaded(getRaceDetail(slug).dataOrNull()))
        }
    }

    private fun <T> DataResult<T>.dataOrNull(): T? = (this as? DataResult.Success)?.data

    private fun <T> DataResult<List<T>>.dataOrEmpty(): List<T> = dataOrNull().orEmpty()
}
