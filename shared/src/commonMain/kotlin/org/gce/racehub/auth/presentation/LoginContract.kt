package org.gce.racehub.auth.presentation

import org.gce.racehub.auth.domain.model.AuthFailure

// MVI contract for the login form. Form errors are state (shown inline until the
// next edit); navigation is an effect.

/** Immutable snapshot of the login form. Only [LoginReducer] produces new values. */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    /** Inline error; cleared by the next edit or submit. Each platform localizes it. */
    val error: AuthFailure? = null
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
    data class Failed(val failure: AuthFailure) : LoginMutation
    /** Leaving the screen: clear the form so the password doesn't linger. */
    data object Succeeded : LoginMutation
}
