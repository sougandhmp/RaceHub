package org.gce.racehub.race.presentation

import kotlinx.datetime.TimeZone
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.nextRace
import org.gce.racehub.race.domain.model.weekendSchedule

/**
 * The only place [RaceState] changes: a pure function of the current state and
 * a [RaceMutation]. No I/O, no coroutines — [timeZone] is injected so tests are
 * deterministic.
 */
internal class RaceReducer(private val timeZone: TimeZone) {

    fun reduce(state: RaceState, mutation: RaceMutation): RaceState = when (mutation) {
        RaceMutation.LoadStarted -> state.copy(isLoading = true)

        is RaceMutation.Loaded -> {
            val next = mutation.schedule.nextRace()
            // Keep the detail only if it still belongs to the next race.
            val detail = state.nextRaceDetail.takeIf { state.nextRace?.id == next?.id }
            state.copy(
                isLoading = false,
                raceSchedule = mutation.schedule,
                driverStandings = mutation.drivers,
                constructorStandings = mutation.constructors,
                trendingThreads = mutation.trending,
                nextRace = next,
                nextRaceDetail = detail,
                nextRaceSessions = sessions(next, detail)
            )
        }

        RaceMutation.LoadFailed -> state.copy(isLoading = false)

        RaceMutation.NextRaceDetailStarted -> state.copy(isLoadingDetail = true)

        is RaceMutation.NextRaceDetailLoaded -> {
            // A failed fetch (null) keeps whatever detail was already shown.
            val detail = mutation.detail ?: state.nextRaceDetail
            state.copy(
                isLoadingDetail = false,
                nextRaceDetail = detail,
                nextRaceSessions = sessions(state.nextRace, detail)
            )
        }

        is RaceMutation.RaceSelected -> state.copy(
            selectedRace = mutation.race,
            selectedRaceDetail = null,
            selectedRaceSessions = sessions(mutation.race, null),
            isLoadingSelectedDetail = true
        )

        is RaceMutation.SelectedRaceDetailLoaded -> state.copy(
            isLoadingSelectedDetail = false,
            selectedRaceDetail = mutation.detail,
            selectedRaceSessions = sessions(state.selectedRace, mutation.detail)
        )
    }

    private fun sessions(race: Race?, detail: RaceDetail?): List<WeekendSession> =
        if (race == null && detail == null) emptyList()
        else formatSessions(weekendSchedule(race, detail), timeZone)
}
