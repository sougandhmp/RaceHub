package org.gce.racehub.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User data included in the login response.
 */
@Serializable
data class UserResponseDto(
    @SerialName("id")
    val id: String,

    @SerialName("username")
    val username: String,

    @SerialName("email")
    val email: String,

    @SerialName("country")
    val country: String,

    @SerialName("avatar")
    val avatar: String,

    @SerialName("role")
    val role: String,

    @SerialName("joinedAt")
    val joinedAt: String,

    @SerialName("postsCount")
    val postsCount: Int
)

/**
 * Data payload in a successful login response.
 */
@Serializable
data class LoginDataDto(
    @SerialName("token")
    val token: String,

    @SerialName("user")
    val user: UserResponseDto
)

/**
 * Response body for login API endpoint.
 *
 * Received from POST /api/v1/auth/login
 */
@Serializable
data class LoginResponseDto(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: LoginDataDto?
)

