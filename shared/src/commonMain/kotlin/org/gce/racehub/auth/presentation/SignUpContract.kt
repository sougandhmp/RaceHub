package org.gce.racehub.auth.presentation

// MVI contract for the sign-up form.

/** Immutable snapshot of the sign-up form. Only [SignUpReducer] produces new values. */
data class SignUpState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    /** ISO country code chosen in the picker; empty until chosen. */
    val country: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    /** Inline error from validation or the server; cleared by the next edit or submit. */
    val errorMessage: String? = null
)

sealed class SignUpIntent {
    data class UsernameChanged(val username: String) : SignUpIntent()
    data class EmailChanged(val email: String) : SignUpIntent()
    data class PasswordChanged(val password: String) : SignUpIntent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpIntent()
    data class CountryChanged(val country: String) : SignUpIntent()
    data object TogglePasswordVisibility : SignUpIntent()
    data object ToggleConfirmPasswordVisibility : SignUpIntent()
    data object Submit : SignUpIntent()
}

sealed class SignUpEffect {
    /** Account created (not signed in); a verification code was sent to [email]. */
    data class NavigateToEmailVerification(val email: String) : SignUpEffect()
}

internal sealed interface SignUpMutation {
    data class FieldsChanged(val transform: (SignUpState) -> SignUpState) : SignUpMutation
    data object Submitted : SignUpMutation
    data class Failed(val message: String) : SignUpMutation
    data object Succeeded : SignUpMutation
}
