package org.gce.racehub.profile.data.dto

import kotlinx.serialization.Serializable

// Profile API payloads.

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
