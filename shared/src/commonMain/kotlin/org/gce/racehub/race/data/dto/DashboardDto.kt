package org.gce.racehub.race.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLResponse<T>(
    val data: T? = null,
    val errors: List<GraphQLError>? = null
)

@Serializable
data class GraphQLError(
    val message: String
)

@Serializable
data class DashboardData(
    val dashboard: DashboardContent
)

@Serializable
data class DashboardContent(
    val seasonYear: Int,
    val upcomingRace: UpcomingRaceDto? = null,
    val latestRace: LatestRaceDto? = null,
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
    val likes: Int,
    val createdAt: String
)

@Serializable
data class GraphQLRequest(
    val query: String
)

@Serializable
data class GraphQLRequestWithVariables(
    val query: String,
    val variables: ThreadsVariables
)

@Serializable
data class ThreadsVariables(
    val sort: String? = null,
    val category: String? = null,
    val userId: String? = null
)

@Serializable
data class ThreadsData(
    val threads: List<ThreadDto>
)

@Serializable
data class ThreadDto(
    val id: String,
    val title: String,
    val category: String,
    val author: ThreadAuthorDto,
    val excerpt: String? = null,
    val content: String,
    val createdAt: String,
    val likes: Int,
    val bookmarked: Boolean = false,
    val comments: List<ThreadCommentDto> = emptyList()
)

@Serializable
data class ThreadAuthorDto(
    val username: String,
    val avatar: String
)

@Serializable
data class ThreadCommentDto(
    val content: String,
    val author: ThreadCommentAuthorDto
)

@Serializable
data class ThreadCommentAuthorDto(
    val username: String
)

@Serializable
data class GraphQLCreateThreadRequest(
    val query: String,
    val variables: CreateThreadVariables
)

@Serializable
data class CreateThreadVariables(
    val userId: String,
    val input: CreateThreadInput
)

@Serializable
data class CreateThreadInput(
    val title: String,
    val category: String,
    val content: String
)

@Serializable
data class CreateThreadData(
    val createThread: CreatedThreadDto
)

@Serializable
data class CreatedThreadDto(
    val id: String,
    val title: String,
    val createdAt: String
)

@Serializable
data class GraphQLAddCommentRequest(
    val query: String,
    val variables: AddCommentVariables
)

@Serializable
data class AddCommentVariables(
    val userId: String,
    val threadId: String,
    val content: String
)

@Serializable
data class AddCommentData(
    val addComment: AddedCommentDto
)

@Serializable
data class AddedCommentDto(
    val id: String,
    val content: String,
    val createdAt: String
)

@Serializable
data class GraphQLProfileRequest(
    val query: String,
    val variables: ProfileVariables
)

@Serializable
data class ProfileVariables(
    val userId: String
)

@Serializable
data class ProfileData(
    val me: ProfileDto
)

@Serializable
data class ProfileDto(
    val username: String,
    val email: String,
    val avatar: String,
    val postsCount: Int,
    val savedCount: Int,
    val recentThreads: List<ProfileThreadDto>,
    val savedThreads: List<ProfileThreadDto>
)

@Serializable
data class ProfileThreadDto(
    val title: String
)

@Serializable
data class GraphQLLikeThreadRequest(
    val query: String,
    val variables: LikeThreadVariables
)

@Serializable
data class LikeThreadVariables(
    val id: String
)

@Serializable
data class LikeThreadData(
    val likeThread: LikedThreadDto
)

@Serializable
data class LikedThreadDto(
    val id: String,
    val likes: Int
)

@Serializable
data class RacesData(
    val races: List<RaceScheduleDto>
)

@Serializable
data class RaceScheduleDto(
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
data class GraphQLRaceDetailRequest(
    val query: String,
    val variables: RaceDetailVariables
)

@Serializable
data class RaceDetailVariables(
    val slug: String
)

@Serializable
data class RaceDetailData(
    val race: RaceDetailDto
)

@Serializable
data class RaceDetailDto(
    val grandPrix: String,
    val circuit: String,
    val overview: String? = null,
    val trackFacts: TrackFactsDto? = null,
    val sessions: List<SessionDto> = emptyList(),
    val results: List<RaceResultDetailDto> = emptyList(),
    val fastestLap: FastestLapDto? = null
)

@Serializable
data class TrackFactsDto(
    val laps: Int,
    val lapRecord: String? = null,
    val distanceKm: Double,
    val corners: Int
)

@Serializable
data class SessionDto(
    val label: String,
    val dateTime: String
)

@Serializable
data class RaceResultDetailDto(
    val position: Int,
    val driver: String,
    val team: String,
    val points: Int,
    val time: String? = null
)

@Serializable
data class FastestLapDto(
    val driver: String,
    val time: String
)
