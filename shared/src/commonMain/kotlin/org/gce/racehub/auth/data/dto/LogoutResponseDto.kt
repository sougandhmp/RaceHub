package org.gce.racehub.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LogoutResponseDto(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String
)
