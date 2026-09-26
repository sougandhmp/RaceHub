package org.gce.racehub.auth.presentation

/** Pure: the only place [SignUpState] changes. */
internal object SignUpReducer {
    fun reduce(state: SignUpState, mutation: SignUpMutation): SignUpState = when (mutation) {
        // Any edit clears the previous error.
        is SignUpMutation.UsernameChanged -> state.copy(username = mutation.username, error = null)
        is SignUpMutation.EmailChanged -> state.copy(email = mutation.email, error = null)
        is SignUpMutation.PasswordChanged -> state.copy(password = mutation.password, error = null)
        is SignUpMutation.ConfirmPasswordChanged -> state.copy(confirmPassword = mutation.confirmPassword, error = null)
        is SignUpMutation.CountryChanged -> state.copy(country = mutation.country, error = null)
        SignUpMutation.PasswordVisibilityToggled -> state.copy(isPasswordVisible = !state.isPasswordVisible)
        SignUpMutation.ConfirmPasswordVisibilityToggled ->
            state.copy(isConfirmPasswordVisible = !state.isConfirmPasswordVisible)
        SignUpMutation.Submitted -> state.copy(isLoading = true, error = null)
        is SignUpMutation.Failed -> state.copy(isLoading = false, error = mutation.failure)
        SignUpMutation.Succeeded -> SignUpState()
    }
}
