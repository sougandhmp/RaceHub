package org.gce.racehub.auth.presentation

/** Pure: the only place [LoginState] changes. */
internal object LoginReducer {
    fun reduce(state: LoginState, mutation: LoginMutation): LoginState = when (mutation) {
        is LoginMutation.EmailChanged -> state.copy(email = mutation.email, error = null)
        is LoginMutation.PasswordChanged -> state.copy(password = mutation.password, error = null)
        LoginMutation.PasswordVisibilityToggled -> state.copy(isPasswordVisible = !state.isPasswordVisible)
        LoginMutation.Submitted -> state.copy(isLoading = true, error = null)
        is LoginMutation.Failed -> state.copy(isLoading = false, error = mutation.failure)
        LoginMutation.Succeeded -> LoginState()
    }
}
