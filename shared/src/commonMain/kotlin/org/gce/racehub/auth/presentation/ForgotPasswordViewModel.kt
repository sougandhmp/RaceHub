package org.gce.racehub.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gce.racehub.auth.domain.usecase.ConfirmPasswordResetUseCase
import org.gce.racehub.auth.domain.usecase.RequestPasswordResetUseCase

/** Shared MVI ViewModel for the password-reset flow (Android and iOS). */
class ForgotPasswordViewModel(
    private val requestPasswordReset: RequestPasswordResetUseCase,
    private val confirmPasswordReset: ConfirmPasswordResetUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())

    /** Swift sees `state` and `stateFlow` via KMP-NativeCoroutines. */
    @NativeCoroutinesState
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val _effects = Channel<ForgotPasswordEffect>(Channel.BUFFERED)

    /** One-off events. Swift sees `effects` as a native flow. */
    @NativeCoroutines
    val effects: Flow<ForgotPasswordEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: ForgotPasswordIntent) {
        when (intent) {
            is ForgotPasswordIntent.EmailChanged -> edit { it.copy(email = intent.email) }
            is ForgotPasswordIntent.OtpChanged -> edit { it.copy(otp = intent.otp) }
            is ForgotPasswordIntent.NewPasswordChanged -> edit { it.copy(newPassword = intent.password) }
            is ForgotPasswordIntent.ConfirmPasswordChanged -> edit { it.copy(confirmPassword = intent.password) }
            ForgotPasswordIntent.TogglePasswordVisibility -> edit { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            ForgotPasswordIntent.RequestReset -> requestReset()
            ForgotPasswordIntent.ConfirmReset -> confirmReset()
        }
    }

    private fun edit(transform: (ForgotPasswordState) -> ForgotPasswordState) =
        mutate(ForgotPasswordMutation.FieldsChanged(transform))

    private fun mutate(mutation: ForgotPasswordMutation) = _state.update { ForgotPasswordReducer.reduce(it, mutation) }

    private fun requestReset() {
        val form = _state.value
        if (form.isLoading) return
        mutate(ForgotPasswordMutation.Submitted)
        viewModelScope.launch {
            val result = requestPasswordReset(form.email)
            mutate(
                if (result.isSuccess) ForgotPasswordMutation.CodeSent
                else ForgotPasswordMutation.Failed(result.error ?: "Could not send the reset code.")
            )
        }
    }

    private fun confirmReset() {
        val form = _state.value
        if (form.isLoading) return
        mutate(ForgotPasswordMutation.Submitted)
        viewModelScope.launch {
            val result = confirmPasswordReset(form.email, form.otp, form.newPassword, form.confirmPassword)
            if (result.isSuccess) {
                mutate(ForgotPasswordMutation.ResetCompleted)
                _effects.send(ForgotPasswordEffect.PasswordResetSuccess)
            } else {
                mutate(ForgotPasswordMutation.Failed(result.error ?: "Could not reset the password."))
            }
        }
    }
}
