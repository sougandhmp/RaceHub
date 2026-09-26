package org.gce.racehub.auth.presentation

// MVI contract for the email-verification (OTP) screen.

/** Immutable snapshot of the verification screen. Only [EmailVerificationReducer] produces new values. */
data class EmailVerificationState(
    /** Address the code was sent to; set by [EmailVerificationIntent.Open]. */
    val email: String = "",
    val otp: String = "",
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    /** True after a resend succeeded; the view shows its own localized confirmation. */
    val codeResent: Boolean = false,
    /** Inline error from the server; cleared by the next edit or action. */
    val errorMessage: String? = null
)

sealed class EmailVerificationIntent {
    /** The screen opened for [email]; resets state left over from another address. */
    data class Open(val email: String) : EmailVerificationIntent()
    data class OtpChanged(val otp: String) : EmailVerificationIntent()
    data object Verify : EmailVerificationIntent()
    data object ResendCode : EmailVerificationIntent()
}

sealed class EmailVerificationEffect {
    /** Verified; the account still needs a normal sign-in to start a session. */
    data object EmailVerified : EmailVerificationEffect()
}

internal sealed interface EmailVerificationMutation {
    data class Opened(val email: String) : EmailVerificationMutation
    data class OtpChanged(val otp: String) : EmailVerificationMutation
    data object VerifyStarted : EmailVerificationMutation
    data object Verified : EmailVerificationMutation
    data object ResendStarted : EmailVerificationMutation
    data object Resent : EmailVerificationMutation
    data class Failed(val message: String) : EmailVerificationMutation
}
