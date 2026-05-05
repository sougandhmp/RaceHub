package org.gce.racehub.race.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLResponse<T>(
    val data: T
)

@Serializable
data class DashboardData(
    val dashboard: DashboardContent
)

@Serializable
data class DashboardContent(
    val seasonYear: Int,
    val upcomingRace: UpcomingRaceDto?,
    val latestRace: LatestRaceDto?,
    val driverStandings: List<DriverStandingDto>,
    val constructorStandings: List<ConstructorStandingDto>,
    val trendingThreads: List<TrendingThreadDto>
)

@Serializable
data class UpcomingRaceDto(
    val grandPrix: String,
    val city: String,
    val dateTime: String,
    val status: String
)

@Serializable
data class LatestRaceDto(
    val grandPrix: String,
    val results: List<RaceResultDto>? = null
)

@Serializable
data class RaceResultDto(
    val position: Int,
    val driverName: String,
    val teamName: String
)

@Serializable
data class DriverStandingDto(
    val position: Int,
    val name: String,
    val team: String? = null,
    val points: Int,
    val wins: Int = 0
)

@Serializable
data class ConstructorStandingDto(
    val position: Int,
    val name: String,
    val points: Int,
    val wins: Int = 0
)

@Serializable
data class TrendingThreadDto(
    val id: String,
    val title: String,
    val likes: Int
)

@Serializable
data class GraphQLRequest(
    val query: String
)
