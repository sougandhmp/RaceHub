package org.gce.racehub.auth.presentation

/** Pure: the only place [EmailVerificationState] changes. */
internal object EmailVerificationReducer {
    fun reduce(state: EmailVerificationState, mutation: EmailVerificationMutation): EmailVerificationState =
        when (mutation) {
            is EmailVerificationMutation.Opened ->
                if (mutation.email == state.email) state else EmailVerificationState(email = mutation.email)
            is EmailVerificationMutation.OtpChanged -> state.copy(otp = mutation.otp, error = null)
            EmailVerificationMutation.VerifyStarted -> state.copy(isVerifying = true, error = null, codeResent = false)
            EmailVerificationMutation.Verified -> EmailVerificationState()
            EmailVerificationMutation.ResendStarted -> state.copy(isResending = true, error = null, codeResent = false)
            EmailVerificationMutation.Resent -> state.copy(isResending = false, codeResent = true)
            is EmailVerificationMutation.Failed ->
                state.copy(isVerifying = false, isResending = false, error = mutation.failure)
        }
}
