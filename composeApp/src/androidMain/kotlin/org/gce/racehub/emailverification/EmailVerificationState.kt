package org.gce.racehub.emailverification

data class EmailVerificationState(
    val email: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)
