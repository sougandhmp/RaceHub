package org.gce.racehub.login

/**
 * Every user interaction on the Login screen expressed as an explicit event.
 *
 * The View dispatches intents; the ViewModel is the sole handler. This
 * one-way data flow makes state changes auditable and testable.
 */
sealed class LoginIntent {

    /** Fired on every keystroke in the email field. */
    data class EmailChanged(val email: String) : LoginIntent()

    /** Fired on every keystroke in the password field. */
    data class PasswordChanged(val password: String) : LoginIntent()

    /** Toggles the password field between masked (dots) and plain text. */
    data object TogglePasswordVisibility : LoginIntent()

    /** Submits the current credentials to the login use case. */
    data object Login : LoginIntent()

    /** Clears the active error message without changing any other state. */
    data object DismissError : LoginIntent()
}
