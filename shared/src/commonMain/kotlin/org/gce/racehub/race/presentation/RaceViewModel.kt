package org.gce.racehub.race.presentation

import androidx.lifecycle.ViewModel
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.usecase.GetConstructorStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetDriverStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetRaceDetailUseCase
import org.gce.racehub.race.domain.usecase.GetRaceScheduleUseCase
import org.gce.racehub.race.domain.usecase.GetTrendingThreadsUseCase

/**
 * Shared MVI ViewModel for the Race tab, used by both Android (Compose) and
 * iOS (SwiftUI via KMP-NativeCoroutines). Loads once on creation; [RaceIntent.Refresh] reloads.
 */
class RaceViewModel(
    private val getRaceSchedule: GetRaceScheduleUseCase,
    private val getDriverStandings: GetDriverStandingsUseCase,
    private val getConstructorStandings: GetConstructorStandingsUseCase,
    private val getTrendingThreads: GetTrendingThreadsUseCase,
    private val getRaceDetail: GetRaceDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RaceState())
    /** Swift sees `state` (current value) and `stateFlow` (for asyncSequence) via KMP-NativeCoroutines. */
    @NativeCoroutinesState
    val state: StateFlow<RaceState> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var selectedDetailJob: Job? = null

    init {
        load()
    }

    fun onIntent(intent: RaceIntent) {
        when (intent) {
            RaceIntent.Refresh -> load()
            is RaceIntent.SelectRace -> selectRace(intent.slug)
            RaceIntent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun load() {
        // A refresh supersedes a load in flight rather than racing it.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Sequential on purpose: on a cold cache the first call populates the
                // dashboard that the standings/trending calls then read.
                val schedule = getRaceSchedule().sortedBy { it.round }
                val drivers = getDriverStandings()
                val constructors = getConstructorStandings()
                val trending = getTrendingThreads()
                val next = schedule.firstOrNull { !it.isCompleted }
                _state.update {
                    // Keep the detail only if it still belongs to the next race.
                    val detail = it.nextRaceDetail.takeIf { _ -> it.nextRace?.id == next?.id }
                    it.copy(
                        isLoading = false,
                        raceSchedule = schedule,
                        driverStandings = drivers,
                        constructorStandings = constructors,
                        trendingThreads = trending,
                        nextRace = next,
                        nextRaceDetail = detail,
                        nextRaceSessions = sessionsOrEmpty(next, detail)
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _state.update { it.copy(isLoading = false, error = RaceError.LoadFailed) }
                return@launch
            }
            _state.value.nextRace?.let { loadNextRaceDetail(it) }
        }
    }

    private suspend fun loadNextRaceDetail(race: Race) {
        _state.update { it.copy(isLoadingDetail = true) }
        val detail = fetchDetail(race.id)
        _state.update {
            it.copy(
                isLoadingDetail = false,
                nextRaceDetail = detail ?: it.nextRaceDetail,
                nextRaceSessions = sessionsOrEmpty(race, detail ?: it.nextRaceDetail)
            )
        }
    }

    private fun selectRace(slug: String) {
        // Cancel the previous request so a slow response for an earlier race
        // can never overwrite the detail of the race now on screen.
        selectedDetailJob?.cancel()
        val race = _state.value.raceSchedule.firstOrNull { it.id == slug }
        _state.update {
            it.copy(
                selectedRace = race,
                selectedRaceDetail = null,
                selectedRaceSessions = sessionsOrEmpty(race, null),
                isLoadingSelectedDetail = true
            )
        }
        selectedDetailJob = viewModelScope.launch {
            val detail = fetchDetail(slug)
            _state.update {
                it.copy(
                    isLoadingSelectedDetail = false,
                    selectedRaceDetail = detail,
                    selectedRaceSessions = sessionsOrEmpty(race, detail)
                )
            }
        }
    }

    /** Detail for [slug], or null if the request fails. Cancellation still propagates. */
    private suspend fun fetchDetail(slug: String): RaceDetail? = try {
        getRaceDetail(slug)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private fun sessionsOrEmpty(race: Race?, detail: RaceDetail?) =
        if (race == null && detail == null) emptyList() else weekendSessions(race, detail)
}
