package org.gce.racehub.race.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class GraphQLResponse<T>(
    val data: T? = null,
    val errors: List<GraphQLError>? = null
)

@Serializable
internal data class GraphQLError(
    val message: String
)

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
internal data class GraphQLRequest(
    val query: String
)

@Serializable
internal data class GraphQLRequestWithVariables(
    val query: String,
    val variables: ThreadsVariables
)

@Serializable
internal data class ThreadsVariables(
    val sort: String? = null,
    val category: String? = null,
    val userId: String? = null
)

@Serializable
internal data class ThreadsData(
    val threads: List<ThreadDto>
)

@Serializable
internal data class ThreadDto(
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
internal data class ThreadAuthorDto(
    val username: String,
    val avatar: String
)

@Serializable
internal data class ThreadCommentDto(
    val content: String,
    val author: ThreadCommentAuthorDto
)

@Serializable
internal data class ThreadCommentAuthorDto(
    val username: String
)

@Serializable
internal data class GraphQLCreateThreadRequest(
    val query: String,
    val variables: CreateThreadVariables
)

@Serializable
internal data class CreateThreadVariables(
    val userId: String,
    val input: CreateThreadInput
)

@Serializable
internal data class CreateThreadInput(
    val title: String,
    val category: String,
    val content: String
)

@Serializable
internal data class CreateThreadData(
    val createThread: CreatedThreadDto
)

@Serializable
internal data class CreatedThreadDto(
    val id: String,
    val title: String,
    val createdAt: String
)

@Serializable
internal data class GraphQLAddCommentRequest(
    val query: String,
    val variables: AddCommentVariables
)

@Serializable
internal data class AddCommentVariables(
    val userId: String,
    val threadId: String,
    val content: String
)

@Serializable
internal data class AddCommentData(
    val addComment: AddedCommentDto
)

@Serializable
internal data class AddedCommentDto(
    val id: String,
    val content: String,
    val createdAt: String
)

@Serializable
internal data class GraphQLProfileRequest(
    val query: String,
    val variables: ProfileVariables
)

@Serializable
internal data class ProfileVariables(
    val userId: String
)

@Serializable
internal data class ProfileData(
    val me: ProfileDto
)

@Serializable
internal data class ProfileDto(
    val username: String,
    val email: String,
    val avatar: String,
    val postsCount: Int,
    val savedCount: Int,
    val recentThreads: List<ProfileThreadDto>,
    val savedThreads: List<ProfileThreadDto>
)

@Serializable
internal data class ProfileThreadDto(
    val title: String
)

@Serializable
internal data class GraphQLLikeThreadRequest(
    val query: String,
    val variables: LikeThreadVariables
)

@Serializable
internal data class LikeThreadVariables(
    val id: String
)

@Serializable
internal data class LikeThreadData(
    val likeThread: LikedThreadDto
)

@Serializable
internal data class LikedThreadDto(
    val id: String,
    val likes: Int
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
