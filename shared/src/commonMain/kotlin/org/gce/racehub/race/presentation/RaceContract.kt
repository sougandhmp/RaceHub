package org.gce.racehub.race.presentation

import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.TrendingThread

/**
 * Immutable snapshot of the Race tab and the screens that hang off it
 * (schedule, standings, race detail). Produced by [RaceViewModel]; rendered
 * as-is by both the Compose and SwiftUI views.
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
    /** Display-ready sessions for [nextRace]: real ones once [nextRaceDetail] loads, estimated before. */
    val nextRaceSessions: List<WeekendSession> = emptyList(),
    val isLoadingDetail: Boolean = false,
    /** Race the user opened via [RaceIntent.SelectRace]. */
    val selectedRace: Race? = null,
    val selectedRaceDetail: RaceDetail? = null,
    /** Display-ready sessions for [selectedRace]. */
    val selectedRaceSessions: List<WeekendSession> = emptyList(),
    val isLoadingSelectedDetail: Boolean = false,
    val isLoading: Boolean = false,
    /** Set when the last load failed; each platform maps it to localized text. */
    val error: RaceError? = null
)

enum class RaceError { LoadFailed }

/** Every user interaction on the Race tab. [RaceViewModel] is the sole handler. */
sealed class RaceIntent {
    /** Pull-to-refresh or retry. Cancels a load already in flight. */
    data object Refresh : RaceIntent()

    /** Opens the race identified by [slug]; loads its detail. */
    data class SelectRace(val slug: String) : RaceIntent()

    data object DismissError : RaceIntent()
}
