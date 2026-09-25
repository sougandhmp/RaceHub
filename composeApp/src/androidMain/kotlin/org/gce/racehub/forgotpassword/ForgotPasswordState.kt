package org.gce.racehub.forgotpassword

enum class ForgotPasswordStep { REQUEST, CONFIRM }

data class ForgotPasswordState(
    val step: ForgotPasswordStep = ForgotPasswordStep.REQUEST,
    val email: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
