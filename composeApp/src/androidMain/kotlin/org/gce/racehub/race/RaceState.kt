package org.gce.racehub.race

import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.TrendingThread

/**
 * Immutable snapshot of the Race tab and the screens that hang off of it
 * (Schedule, Standings). Produced by [RaceViewModel] on every state change;
 * the View never mutates this object directly.
 */
data class RaceState(

    /** Ordered list of race events for the current season. Empty until loaded. */
    val raceSchedule: List<Race> = emptyList(),

    /** Driver championship standings sorted by position. Empty until loaded. */
    val driverStandings: List<DriverStanding> = emptyList(),

    /** Constructor standings sorted by position. Empty until loaded. */
    val constructorStandings: List<ConstructorStanding> = emptyList(),

    /** Trending forum threads displayed inside the Race tab. */
    val trendingThreads: List<TrendingThread> = emptyList(),

    /** Detailed race info for the next race fetched via GraphQL. Null until loaded. */
    val nextRaceDetail: RaceDetail? = null,

    /** True while the next-race detail is being fetched; drives shimmer animation. */
    val isLoadingDetail: Boolean = false,

    /** Detail for the race the user tapped through to. Null until loaded. */
    val selectedRaceDetail: RaceDetail? = null,

    /** True while the selected race's detail is being fetched. */
    val isLoadingSelectedDetail: Boolean = false,

    /** True while data is being fetched; drives the loading indicator. */
    val isLoading: Boolean = false,

    /** Non-null when a data-fetch error should be shown to the user. */
    val errorMessage: String? = null
)
