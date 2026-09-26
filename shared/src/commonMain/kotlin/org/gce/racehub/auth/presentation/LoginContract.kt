package org.gce.racehub.auth.presentation

// MVI contract for the login form. Form errors are state (shown inline until the
// next edit); navigation is an effect.

/** Immutable snapshot of the login form. Only [LoginReducer] produces new values. */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    /** Inline error from validation or the server; cleared by the next edit or submit. */
    val errorMessage: String? = null
)

sealed class LoginIntent {
    data class EmailChanged(val email: String) : LoginIntent()
    data class PasswordChanged(val password: String) : LoginIntent()
    data object TogglePasswordVisibility : LoginIntent()
    data object Submit : LoginIntent()
}

sealed class LoginEffect {
    /** Signed in with a verified account; the session is set. */
    data object NavigateToHome : LoginEffect()
    /** Credentials were right but the email is unverified; a fresh code was sent. */
    data class NavigateToEmailVerification(val email: String) : LoginEffect()
}

internal sealed interface LoginMutation {
    data class EmailChanged(val email: String) : LoginMutation
    data class PasswordChanged(val password: String) : LoginMutation
    data object PasswordVisibilityToggled : LoginMutation
    data object Submitted : LoginMutation
    data class Failed(val message: String) : LoginMutation
    /** Leaving the screen: clear the form so the password doesn't linger. */
    data object Succeeded : LoginMutation
}
