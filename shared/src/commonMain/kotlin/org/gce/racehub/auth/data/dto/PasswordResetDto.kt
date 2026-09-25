package org.gce.racehub.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequestDto(
    @SerialName("email") val email: String
)

@Serializable
data class PasswordResetConfirmDto(
    @SerialName("email") val email: String,
    @SerialName("otp") val otp: String,
    @SerialName("newPassword") val newPassword: String
)

@Serializable
data class PasswordResetResponseDto(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String
)
