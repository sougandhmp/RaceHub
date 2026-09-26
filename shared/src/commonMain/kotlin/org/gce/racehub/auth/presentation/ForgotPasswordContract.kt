package org.gce.racehub.auth.presentation

// MVI contract for the two-step password reset (request a code, then confirm it).

enum class ForgotPasswordStep { Request, Confirm }

/** Immutable snapshot of the password-reset flow. Only [ForgotPasswordReducer] produces new values. */
data class ForgotPasswordState(
    val step: ForgotPasswordStep = ForgotPasswordStep.Request,
    val email: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    /** Inline error from validation or the server; cleared by the next edit or submit. */
    val errorMessage: String? = null
)

sealed class ForgotPasswordIntent {
    data class EmailChanged(val email: String) : ForgotPasswordIntent()
    data class OtpChanged(val otp: String) : ForgotPasswordIntent()
    data class NewPasswordChanged(val password: String) : ForgotPasswordIntent()
    data class ConfirmPasswordChanged(val password: String) : ForgotPasswordIntent()
    data object TogglePasswordVisibility : ForgotPasswordIntent()
    data object RequestReset : ForgotPasswordIntent()
    data object ConfirmReset : ForgotPasswordIntent()
}

sealed class ForgotPasswordEffect {
    /** Password changed; the view should return to login. */
    data object PasswordResetSuccess : ForgotPasswordEffect()
}

internal sealed interface ForgotPasswordMutation {
    data class FieldsChanged(val transform: (ForgotPasswordState) -> ForgotPasswordState) : ForgotPasswordMutation
    data object Submitted : ForgotPasswordMutation
    data class Failed(val message: String) : ForgotPasswordMutation
    data object CodeSent : ForgotPasswordMutation
    data object ResetCompleted : ForgotPasswordMutation
}
