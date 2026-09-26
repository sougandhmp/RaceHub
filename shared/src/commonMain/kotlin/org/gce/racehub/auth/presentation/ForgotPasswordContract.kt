package org.gce.racehub.auth.presentation

import org.gce.racehub.auth.domain.model.AuthFailure

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
    /** Inline error; cleared by the next edit or submit. Each platform localizes it. */
    val error: AuthFailure? = null
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
    data class EmailChanged(val email: String) : ForgotPasswordMutation
    data class OtpChanged(val otp: String) : ForgotPasswordMutation
    data class NewPasswordChanged(val password: String) : ForgotPasswordMutation
    data class ConfirmPasswordChanged(val password: String) : ForgotPasswordMutation
    data object PasswordVisibilityToggled : ForgotPasswordMutation
    data object Submitted : ForgotPasswordMutation
    data class Failed(val failure: AuthFailure) : ForgotPasswordMutation
    data object CodeSent : ForgotPasswordMutation
    data object ResetCompleted : ForgotPasswordMutation
}
