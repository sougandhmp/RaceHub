package org.gce.racehub.auth.presentation

/** Pure: the only place [ForgotPasswordState] changes. */
internal object ForgotPasswordReducer {
    fun reduce(state: ForgotPasswordState, mutation: ForgotPasswordMutation): ForgotPasswordState = when (mutation) {
        // Any edit clears the previous error.
        is ForgotPasswordMutation.EmailChanged -> state.copy(email = mutation.email, error = null)
        is ForgotPasswordMutation.OtpChanged -> state.copy(otp = mutation.otp, error = null)
        is ForgotPasswordMutation.NewPasswordChanged -> state.copy(newPassword = mutation.password, error = null)
        is ForgotPasswordMutation.ConfirmPasswordChanged -> state.copy(confirmPassword = mutation.password, error = null)
        ForgotPasswordMutation.PasswordVisibilityToggled -> state.copy(isPasswordVisible = !state.isPasswordVisible)
        ForgotPasswordMutation.Submitted -> state.copy(isLoading = true, error = null)
        is ForgotPasswordMutation.Failed -> state.copy(isLoading = false, error = mutation.failure)
        ForgotPasswordMutation.CodeSent -> state.copy(isLoading = false, step = ForgotPasswordStep.Confirm)
        // Leaving the flow: clear passwords and the code.
        ForgotPasswordMutation.ResetCompleted -> ForgotPasswordState()
    }
}
