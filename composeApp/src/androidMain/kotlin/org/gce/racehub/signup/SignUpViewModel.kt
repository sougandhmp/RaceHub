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
import org.gce.racehub.auth.data.repository.AuthRepositoryImpl
import org.gce.racehub.auth.domain.usecase.SignUpUseCase

/**
 * ViewModel for the Sign-Up screen following the MVI pattern.
 *
 * - Exposes [state] as an immutable [StateFlow] the View observes.
 * - Accepts user actions via [onIntent] and processes them into state mutations.
 * - Emits one-time navigation/side-effect events through [effect].
 *
 * The ViewModel is the single source of truth; the View is passive and never
 * holds any business logic.
 */
class SignUpViewModel : ViewModel() {

    /** Use case that validates the sign-up form and delegates to the repository. */
    private val signUpUseCase = SignUpUseCase(AuthRepositoryImpl())

    private val _state = MutableStateFlow(SignUpState())

    /** Observable UI state. Collected by the View via [collectAsStateWithLifecycle]. */
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    // Channel is used instead of SharedFlow so each effect is consumed exactly once,
    // even if the collector is briefly inactive (e.g. during recomposition).
    private val _effect = Channel<SignUpEffect>(Channel.BUFFERED)

    /** Stream of one-time side effects (navigation, toasts, etc.). */
    val effect = _effect.receiveAsFlow()

    /**
     * Entry point for all View interactions.
     * Maps each [SignUpIntent] to a state mutation or a command.
     */
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
        }
    }

    /**
     * Runs the sign-up use case and updates state based on the result.
     * Executes inside [viewModelScope] to automatically cancel on ViewModel destruction.
     */
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
