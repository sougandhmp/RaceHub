package org.gce.racehub.race.presentation

import org.gce.racehub.core.domain.DataError
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.TrendingThread

// MVI contract for the Race tab:
//   View ──RaceIntent──▶ RaceViewModel ──RaceMutation──▶ RaceReducer ──RaceState──▶ View
//                              └──────────RaceEffect (one-off)──────────────────▶ View

/**
 * Immutable snapshot of the Race tab and the screens that hang off it
 * (schedule, standings, race detail). Only [RaceReducer] produces new values;
 * both the Compose and SwiftUI views render it as-is.
 */
data class RaceState(
    /** Season calendar ordered by round. Empty until loaded. */
    val raceSchedule: List<Race> = emptyList(),
    val driverStandings: List<DriverStanding> = emptyList(),
    val constructorStandings: List<ConstructorStanding> = emptyList(),
    val trendingThreads: List<TrendingThread> = emptyList(),
    /** First race not yet completed; null when the season is over or not loaded. */
    val nextRace: Race? = null,
    /** API detail for [nextRace]. Null until loaded. */
    val nextRaceDetail: RaceDetail? = null,
    /** Display-ready sessions for [nextRace]: real once [nextRaceDetail] loads, estimated before. */
    val nextRaceSessions: List<WeekendSession> = emptyList(),
    val isLoadingDetail: Boolean = false,
    /** Race the user opened via [RaceIntent.SelectRace]. */
    val selectedRace: Race? = null,
    val selectedRaceDetail: RaceDetail? = null,
    /** Display-ready sessions for [selectedRace]. */
    val selectedRaceSessions: List<WeekendSession> = emptyList(),
    val isLoadingSelectedDetail: Boolean = false,
    val isLoading: Boolean = false
)

/** Everything the user can do on the Race tab. [RaceViewModel] is the sole handler. */
sealed class RaceIntent {
    /** Pull-to-refresh or retry. Supersedes a load already in flight. */
    data object Refresh : RaceIntent()

    /** Opens the race identified by [slug]; loads its detail. */
    data class SelectRace(val slug: String) : RaceIntent()
}

/** One-off events the view shows once (e.g. a snackbar/alert), never replayed on re-render. */
sealed class RaceEffect {
    /** Loading the tab failed; [error] tells the view which message to show. */
    data class ShowLoadError(val error: DataError) : RaceEffect()
}

/**
 * State changes, each the result of an intent or of async work finishing.
 * Internal: only the ViewModel emits them and only the reducer consumes them.
 */
internal sealed interface RaceMutation {
    data object LoadStarted : RaceMutation
    data class Loaded(
        val schedule: List<Race>,
        val drivers: List<DriverStanding>,
        val constructors: List<ConstructorStanding>,
        val trending: List<TrendingThread>
    ) : RaceMutation
    data object LoadFailed : RaceMutation
    data object NextRaceDetailStarted : RaceMutation
    data class NextRaceDetailLoaded(val detail: RaceDetail?) : RaceMutation
    data class RaceSelected(val race: Race?) : RaceMutation
    data class SelectedRaceDetailLoaded(val detail: RaceDetail?) : RaceMutation
}
