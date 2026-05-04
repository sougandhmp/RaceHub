package org.gce.racehub.home

/**
 * One-time side effects produced by [HomeViewModel].
 *
 * Effects represent navigation or system events that must not be replayed on
 * recomposition. They are delivered via a [kotlinx.coroutines.channels.Channel]
 * so they survive config changes and are consumed exactly once.
 *
 * Detail-screen navigation is wired here even though the detail screens do not
 * exist yet — the ViewModel can emit these effects and [App] can route them
 * once the destination screens are ready.
 */
sealed class HomeEffect {

    /**
     * Navigate to the detail view for a specific race.
     * @param raceId The unique ID of the race that was tapped.
     */
    data class NavigateToRaceDetail(val raceId: String) : HomeEffect()

    /**
     * Navigate to the detailed profile of a driver.
     * @param driverName The full name of the driver that was tapped.
     */
    data class NavigateToDriverDetail(val driverName: String) : HomeEffect()
}
