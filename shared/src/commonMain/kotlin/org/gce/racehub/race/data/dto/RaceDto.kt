package org.gce.racehub.race.data.dto

import kotlinx.serialization.Serializable

// Race API payloads (schedule, dashboard, race detail).

@Serializable
internal data class DashboardData(
    val dashboard: DashboardContent
)

@Serializable
internal data class DashboardContent(
    val seasonYear: Int,
    val upcomingRace: UpcomingRaceDto? = null,
    val latestRace: LatestRaceDto? = null,
    val driverStandings: List<DriverStandingDto>,
    val constructorStandings: List<ConstructorStandingDto>,
    val trendingThreads: List<TrendingThreadDto>
)

@Serializable
internal data class UpcomingRaceDto(
    val grandPrix: String,
    val city: String,
    val dateTime: String,
    val status: String
)

@Serializable
internal data class LatestRaceDto(
    val grandPrix: String,
    val results: List<RaceResultDto>? = null
)

@Serializable
internal data class RaceResultDto(
    val position: Int,
    val driverName: String,
    val teamName: String
)

@Serializable
internal data class DriverStandingDto(
    val position: Int,
    val name: String,
    val team: String? = null,
    val points: Int,
    val wins: Int = 0
)

@Serializable
internal data class ConstructorStandingDto(
    val position: Int,
    val name: String,
    val points: Int,
    val wins: Int = 0
)

@Serializable
internal data class TrendingThreadDto(
    val id: String,
    val title: String,
    val likes: Int,
    val createdAt: String
)

@Serializable
internal data class RacesData(
    val races: List<RaceScheduleDto>
)

@Serializable
internal data class RaceScheduleDto(
    val slug: String,
    val grandPrix: String,
    val circuit: String,
    val country: String,
    val city: String,
    val dateTime: String,
    val round: Int,
    val status: String,
    val weather: String? = null
)

@Serializable
internal data class GraphQLRaceDetailRequest(
    val query: String,
    val variables: RaceDetailVariables
)

@Serializable
internal data class RaceDetailVariables(
    val slug: String
)

@Serializable
internal data class RaceDetailData(
    val race: RaceDetailDto
)

@Serializable
internal data class RaceDetailDto(
    val grandPrix: String,
    val circuit: String,
    val overview: String? = null,
    val trackFacts: TrackFactsDto? = null,
    val sessions: List<SessionDto> = emptyList(),
    val results: List<RaceResultDetailDto> = emptyList(),
    val fastestLap: FastestLapDto? = null
)

@Serializable
internal data class TrackFactsDto(
    val laps: Int,
    val lapRecord: String? = null,
    val distanceKm: Double,
    val corners: Int
)

@Serializable
internal data class SessionDto(
    val label: String,
    val dateTime: String
)

@Serializable
internal data class RaceResultDetailDto(
    val position: Int,
    val driver: String,
    val team: String,
    val points: Int,
    val time: String? = null
)

@Serializable
internal data class FastestLapDto(
    val driver: String,
    val time: String
)
