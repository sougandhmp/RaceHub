package org.gce.racehub.forgotpassword

sealed class ForgotPasswordIntent {
    data class EmailChanged(val email: String) : ForgotPasswordIntent()
    data class OtpChanged(val otp: String) : ForgotPasswordIntent()
    data class NewPasswordChanged(val password: String) : ForgotPasswordIntent()
    data class ConfirmPasswordChanged(val password: String) : ForgotPasswordIntent()
    data object TogglePasswordVisibility : ForgotPasswordIntent()
    data object RequestReset : ForgotPasswordIntent()
    data object ConfirmReset : ForgotPasswordIntent()
    data object DismissError : ForgotPasswordIntent()
}
