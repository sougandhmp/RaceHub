package org.gce.racehub.signup

/**
 * Every user interaction on the Sign-Up screen expressed as an explicit event.
 *
 * The View dispatches intents; the ViewModel is the sole handler. This
 * one-way data flow makes state changes auditable and testable.
 */
sealed class SignUpIntent {

    /** Fired on every keystroke in the full-name field. */
    data class NameChanged(val name: String) : SignUpIntent()

    /** Fired on every keystroke in the email field. */
    data class EmailChanged(val email: String) : SignUpIntent()

    /** Fired on every keystroke in the password field. */
    data class PasswordChanged(val password: String) : SignUpIntent()

    /** Fired on every keystroke in the confirm-password field. */
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpIntent()

    /** Toggles the password field between masked (dots) and plain text. */
    data object TogglePasswordVisibility : SignUpIntent()

    /** Toggles the confirm-password field between masked (dots) and plain text. */
    data object ToggleConfirmPasswordVisibility : SignUpIntent()

    /** Submits the form to the sign-up use case. */
    data object SignUp : SignUpIntent()

    /** Clears the active error message without changing any other state. */
    data object DismissError : SignUpIntent()
}
