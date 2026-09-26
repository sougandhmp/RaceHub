package org.gce.racehub.auth.domain.model

/**
 * The outcome of a password-reset operation (request or confirm step).
 *
 * Mirrors the plain-class pattern of [AuthResult] so both [isSuccess] and
 * [error] are directly accessible from Swift without sealed-class casts.
 */
class PasswordResetResult private constructor(
    val isSuccess: Boolean,
    /** Why it failed; null on success. */
    val failure: AuthFailure? = null
) {
    companion object {
        fun success(): PasswordResetResult = PasswordResetResult(isSuccess = true)
        fun failure(reason: AuthError, serverMessage: String? = null): PasswordResetResult =
            PasswordResetResult(isSuccess = false, failure = AuthFailure(reason, serverMessage))
    }
}
