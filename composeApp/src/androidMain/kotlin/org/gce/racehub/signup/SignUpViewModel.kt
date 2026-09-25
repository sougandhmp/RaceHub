package org.gce.racehub.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gce.racehub.auth.domain.model.OtpPurpose
import org.gce.racehub.auth.domain.usecase.SendOtpUseCase
import org.gce.racehub.auth.domain.usecase.SignUpUseCase

class SignUpViewModel(
    private val signUpUseCase: SignUpUseCase,
    private val sendOtpUseCase: SendOtpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    private val _effect = Channel<SignUpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: SignUpIntent) {
        when (intent) {
            is SignUpIntent.UsernameChanged ->
                _state.update { it.copy(username = intent.username) }

            is SignUpIntent.EmailChanged ->
                _state.update { it.copy(email = intent.email) }

            is SignUpIntent.PasswordChanged ->
                _state.update { it.copy(password = intent.password) }

            is SignUpIntent.ConfirmPasswordChanged ->
                _state.update { it.copy(confirmPassword = intent.confirmPassword) }

            is SignUpIntent.CountryChanged ->
                _state.update { it.copy(country = intent.country) }

            is SignUpIntent.TogglePasswordVisibility ->
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

            is SignUpIntent.ToggleConfirmPasswordVisibility ->
                _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }

            is SignUpIntent.SignUp ->
                performSignUp()

            is SignUpIntent.DismissError ->
                _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun performSignUp() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val s = _state.value
            val result = signUpUseCase(s.username, s.email, s.password, s.country, s.confirmPassword)

            if (!result.isSuccess) {
                _state.update { it.copy(isLoading = false, errorMessage = result.error) }
                return@launch
            }

            // Account created but NOT logged in: the user must verify their email
            // via OTP before the sign-up is considered complete.
            val otpResult = sendOtpUseCase(s.email, OtpPurpose.EMAIL_VERIFICATION)
            if (otpResult.isSuccess) {
                _state.update { it.copy(isLoading = false) }
                _effect.send(SignUpEffect.NavigateToEmailVerification(s.email))
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = otpResult.error) }
            }
        }
    }
}
