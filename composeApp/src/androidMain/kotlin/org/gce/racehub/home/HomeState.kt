package org.gce.racehub.home

/**
 * Identifies which content tab is active on the Home screen.
 * Stored inside [HomeState] and toggled via [HomeIntent.TabSelected].
 */
enum class HomeTab {
    Race,
    Forum,
    Profile
}

/**
 * Immutable snapshot of the Home shell. Owns only the currently selected tab;
 * each tab's content state lives in its own feature ViewModel.
 */
data class HomeState(

    /** Which tab the user is currently viewing. */
    val selectedTab: HomeTab = HomeTab.Race
)
