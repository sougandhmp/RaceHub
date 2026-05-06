package org.gce.racehub.home

import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.race.domain.model.TrendingThread

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
 * Immutable snapshot of everything the Home screen needs to render itself.
 *
 * A new instance is produced by [HomeViewModel] on every state change;
 * the View never mutates this object directly.
 */
data class HomeState(

    /** Ordered list of race events for the current season. Empty until loaded. */
    val raceSchedule: List<Race> = emptyList(),

    /** Championship standings sorted by position. Empty until loaded. */
    val driverStandings: List<DriverStanding> = emptyList(),

    /** Constructor standings sorted by position. Empty until loaded. */
    val constructorStandings: List<ConstructorStanding> = emptyList(),

    /** Trending forum threads. Empty until loaded. */
    val trendingThreads: List<TrendingThread> = emptyList(),

    /** Full forum threads shown on the Forum tab. Empty until loaded. */
    val forumThreads: List<Thread> = emptyList(),

    /** Which tab the user is currently viewing. */
    val selectedTab: HomeTab = HomeTab.Race,

    /** True while data is being fetched; drives the loading indicator. */
    val isLoading: Boolean = false,

    /** Non-null when a data-fetch error should be shown to the user. */
    val errorMessage: String? = null
)
