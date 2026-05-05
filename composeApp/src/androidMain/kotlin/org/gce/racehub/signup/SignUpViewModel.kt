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
import org.gce.racehub.auth.domain.usecase.SignUpUseCase

class SignUpViewModel(private val signUpUseCase: SignUpUseCase) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    private val _effect = Channel<SignUpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: SignUpIntent) {
        when (intent) {
            is SignUpIntent.NameChanged ->
                _state.update { it.copy(name = intent.name) }

            is SignUpIntent.EmailChanged ->
                _state.update { it.copy(email = intent.email) }

            is SignUpIntent.PasswordChanged ->
                _state.update { it.copy(password = intent.password) }

            is SignUpIntent.ConfirmPasswordChanged ->
                _state.update { it.copy(confirmPassword = intent.confirmPassword) }

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
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val s = _state.value
            val result = signUpUseCase.execute(s.name, s.email, s.password, s.confirmPassword)

            if (result.isSuccess) {
                _state.update { it.copy(isLoading = false) }
                _effect.send(SignUpEffect.NavigateToHome)
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = result.error) }
            }
        }
    }
}
