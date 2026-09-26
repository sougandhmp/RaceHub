package org.gce.racehub.auth.domain.model

/**
 * The outcome of an email-verification operation (verify or resend step).
 *
 * Mirrors the plain-class pattern of [AuthResult] and [PasswordResetResult] so
 * both [isSuccess] and [error] are directly accessible from Swift without
 * sealed-class casts.
 */
class EmailVerificationResult private constructor(
    val isSuccess: Boolean,
    /** Why it failed; null on success. */
    val failure: AuthFailure? = null
) {
    companion object {
        fun success(): EmailVerificationResult = EmailVerificationResult(isSuccess = true)
        fun failure(reason: AuthError, serverMessage: String? = null): EmailVerificationResult =
            EmailVerificationResult(isSuccess = false, failure = AuthFailure(reason, serverMessage))
    }
}
