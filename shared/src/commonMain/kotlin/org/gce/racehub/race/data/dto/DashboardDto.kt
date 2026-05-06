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
