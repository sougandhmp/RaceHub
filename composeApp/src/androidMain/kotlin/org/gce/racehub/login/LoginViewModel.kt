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
import org.gce.racehub.auth.domain.model.OtpPurpose
import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.auth.domain.usecase.LoginUseCase
import org.gce.racehub.auth.domain.usecase.SendOtpUseCase

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val userSession: UserSession,
    private val sendOtpUseCase: SendOtpUseCase
) : ViewModel() {

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

            val result = loginUseCase(_state.value.email, _state.value.password)
            val user = result.user

            when {
                !result.isSuccess -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.error) }
                }
                // Client-side guard: a verified email is required to enter the app.
                // Even if the server issued a token, refuse to start a session for an
                // unverified account; send a fresh code and divert to verification.
                user != null && !user.isEmailVerified -> {
                    sendOtpUseCase(user.email, OtpPurpose.EMAIL_VERIFICATION)
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(LoginEffect.NavigateToEmailVerification(user.email))
                }
                else -> {
                    user?.let(userSession::setUser)
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(LoginEffect.NavigateToHome)
                }
            }
        }
    }
}
