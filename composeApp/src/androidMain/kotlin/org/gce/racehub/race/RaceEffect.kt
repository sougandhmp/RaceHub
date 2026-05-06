package org.gce.racehub.race

/**
 * One-time side effects produced by [RaceViewModel] — typically navigation
 * events that must not be replayed on recomposition.
 */
sealed class RaceEffect {

    /** Navigate to the detail view for a specific race. */
    data class NavigateToRaceDetail(val raceId: String) : RaceEffect()

    /** Navigate to the detailed profile of a driver. */
    data class NavigateToDriverDetail(val driverName: String) : RaceEffect()
}
