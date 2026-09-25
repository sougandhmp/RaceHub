package org.gce.racehub.emailverification

sealed class EmailVerificationIntent {
    data class SetEmail(val email: String) : EmailVerificationIntent()
    data class OtpChanged(val otp: String) : EmailVerificationIntent()
    data object Verify : EmailVerificationIntent()
    data object ResendCode : EmailVerificationIntent()
    data object DismissError : EmailVerificationIntent()
}
