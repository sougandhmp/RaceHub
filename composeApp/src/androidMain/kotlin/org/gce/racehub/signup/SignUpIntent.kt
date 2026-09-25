package org.gce.racehub.signup

/**
 * Every user interaction on the Sign-Up screen expressed as an explicit event.
 *
 * The View dispatches intents; the ViewModel is the sole handler. This
 * one-way data flow makes state changes auditable and testable.
 */
sealed class SignUpIntent {

    data class UsernameChanged(val username: String) : SignUpIntent()
    data class EmailChanged(val email: String) : SignUpIntent()
    data class PasswordChanged(val password: String) : SignUpIntent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpIntent()
    data class CountryChanged(val country: String) : SignUpIntent()
    data object TogglePasswordVisibility : SignUpIntent()
    data object ToggleConfirmPasswordVisibility : SignUpIntent()
    data object SignUp : SignUpIntent()
    data object DismissError : SignUpIntent()
}
