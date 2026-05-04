package org.gce.racehub.login

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
import org.gce.racehub.auth.domain.usecase.LoginUseCase

/**
 * ViewModel for the Login screen following the MVI pattern.
 *
 * - Exposes [state] as an immutable [StateFlow] the View observes.
 * - Accepts user actions via [onIntent] and processes them into state mutations.
 * - Emits one-time navigation/side-effect events through [effect].
 *
 * The ViewModel is the single source of truth; the View is passive and never
 * holds any business logic.
 */
class LoginViewModel : ViewModel() {

    /** Use case that validates credentials and delegates to the repository. */
    private val loginUseCase = LoginUseCase(AuthRepositoryImpl())

    private val _state = MutableStateFlow(LoginState())

    /** Observable UI state. Collected by the View via [collectAsStateWithLifecycle]. */
    val state: StateFlow<LoginState> = _state.asStateFlow()

    // Channel is used instead of SharedFlow so each effect is consumed exactly once,
    // even if the collector is briefly inactive (e.g. during recomposition).
    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)

    /** Stream of one-time side effects (navigation, toasts, etc.). */
    val effect = _effect.receiveAsFlow()

    /**
     * Entry point for all View interactions.
     * Maps each [LoginIntent] to a state mutation or a command.
     */
    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged ->
                _state.update { it.copy(email = intent.email) }

            is LoginIntent.PasswordChanged ->
                _state.update { it.copy(password = intent.password) }

            is LoginIntent.TogglePasswordVisibility ->
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

            is LoginIntent.Login ->
                performLogin()

            is LoginIntent.DismissError ->
                _state.update { it.copy(errorMessage = null) }
        }
    }

    /**
     * Runs the login use case and updates state based on the result.
     * Executes inside [viewModelScope] to automatically cancel on ViewModel destruction.
     */
    private fun performLogin() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val result = loginUseCase.execute(_state.value.email, _state.value.password)

            if (result.isSuccess) {
                _state.update { it.copy(isLoading = false) }
                _effect.send(LoginEffect.NavigateToHome)
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = result.error) }
            }
        }
    }
}
