package org.gce.racehub.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gce.racehub.auth.domain.usecase.ConfirmPasswordResetUseCase
import org.gce.racehub.auth.domain.usecase.RequestPasswordResetUseCase

class ForgotPasswordViewModel(
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val confirmPasswordResetUseCase: ConfirmPasswordResetUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val _effect = Channel<ForgotPasswordEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: ForgotPasswordIntent) {
        when (intent) {
            is ForgotPasswordIntent.EmailChanged -> _state.update { it.copy(email = intent.email) }
            is ForgotPasswordIntent.OtpChanged -> _state.update { it.copy(otp = intent.otp) }
            is ForgotPasswordIntent.NewPasswordChanged -> _state.update { it.copy(newPassword = intent.password) }
            is ForgotPasswordIntent.ConfirmPasswordChanged -> _state.update { it.copy(confirmPassword = intent.password) }
            is ForgotPasswordIntent.TogglePasswordVisibility -> _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is ForgotPasswordIntent.RequestReset -> requestReset()
            is ForgotPasswordIntent.ConfirmReset -> confirmReset()
            is ForgotPasswordIntent.DismissError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun requestReset() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = requestPasswordResetUseCase(_state.value.email)
            if (result.isSuccess) {
                _state.update { it.copy(isLoading = false, step = ForgotPasswordStep.CONFIRM) }
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = result.error) }
            }
        }
    }

    private fun confirmReset() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = confirmPasswordResetUseCase(
                email = current.email,
                otp = current.otp,
                newPassword = current.newPassword,
                confirmPassword = current.confirmPassword
            )
            if (result.isSuccess) {
                _state.update { it.copy(isLoading = false) }
                _effect.send(ForgotPasswordEffect.PasswordResetSuccess)
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = result.error) }
            }
        }
    }
}
