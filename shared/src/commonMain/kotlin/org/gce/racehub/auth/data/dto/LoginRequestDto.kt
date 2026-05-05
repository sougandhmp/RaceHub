package org.gce.racehub.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for login API endpoint.
 *
 * Sent to POST /api/v1/auth/login
 */
@Serializable
data class LoginRequestDto(
    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String
)

