package org.gce.racehub.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Body for `POST /api/v1/auth/otp/send` and `POST /api/v1/auth/otp/resend`. */
@Serializable
data class OtpSendRequestDto(
    @SerialName("email") val email: String,
    @SerialName("subject") val subject: String
)

/** Body for `POST /api/v1/auth/otp/verify`. */
@Serializable
data class OtpVerifyRequestDto(
    @SerialName("email") val email: String,
    @SerialName("otp") val otp: String
)

@Serializable
data class OtpResponseDto(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String
)
