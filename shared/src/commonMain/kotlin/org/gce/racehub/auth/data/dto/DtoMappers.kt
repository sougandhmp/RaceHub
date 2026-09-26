package org.gce.racehub.auth.data.dto

import org.gce.racehub.core.domain.model.User

/**
 * Converts a [UserResponseDto] to a domain [User] model.
 *
 * @param token The authentication token returned by the server
 * @return A [User] instance with all fields populated
 */
internal fun UserResponseDto.toDomainModel(token: String): User {
    return User(
        id = this.id,
        email = this.email,
        name = this.username,
        token = token,
        username = this.username,
        country = this.country,
        avatar = this.avatar,
        role = this.role,
        joinedAt = this.joinedAt,
        postsCount = this.postsCount,
        isEmailVerified = this.emailVerified
    )
}

/**
 * Converts a [LoginResponseDto] to a domain [User] model.
 *
 * Extracts user data and token from the response payload.
 * Returns null if the response data is null or invalid.
 *
 * @return A [User] instance, or null if the response data is unavailable
 */
internal fun LoginResponseDto.toDomainModel(): User? {
    return data?.let { loginData ->
        loginData.user.toDomainModel(loginData.token)
    }
}

