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
import org.gce.racehub.auth.domain.model.OtpPurpose
import org.gce.racehub.core.domain.session.UserSession
import org.gce.racehub.auth.domain.usecase.LoginUseCase
import org.gce.racehub.auth.domain.usecase.SendOtpUseCase
import org.gce.racehub.core.domain.DataResult

/**
 * Shared MVI ViewModel for the login form (Android and iOS). A verified account
 * starts a session; an unverified one gets a fresh code and goes to verification
 * without a session being started.
 */
class LoginViewModel internal constructor(
    private val login: LoginUseCase,
    private val userSession: UserSession,
    private val sendOtp: SendOtpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())

    /** Swift sees `state` and `stateFlow` via KMP-NativeCoroutines. */
    @NativeCoroutinesState
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _effects = Channel<LoginEffect>(Channel.BUFFERED)

    /** One-off events. Swift sees `effects` as a native flow. */
    @NativeCoroutines
    val effects: Flow<LoginEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> mutate(LoginMutation.EmailChanged(intent.email))
            is LoginIntent.PasswordChanged -> mutate(LoginMutation.PasswordChanged(intent.password))
            LoginIntent.TogglePasswordVisibility -> mutate(LoginMutation.PasswordVisibilityToggled)
            LoginIntent.Submit -> submit()
        }
    }

    private fun mutate(mutation: LoginMutation) = _state.update { LoginReducer.reduce(it, mutation) }

    private fun submit() {
        val form = _state.value
        if (form.isLoading) return
        mutate(LoginMutation.Submitted)
        viewModelScope.launch {
            val user = when (val result = login(form.email, form.password)) {
                is DataResult.Failure -> return@launch mutate(LoginMutation.Failed(result.error))
                is DataResult.Success -> result.data
            }
            when {
                // A verified email is required to enter the app, even if the server issued a token.
                !user.isEmailVerified -> {
                    sendOtp(user.email, OtpPurpose.EMAIL_VERIFICATION)
                    mutate(LoginMutation.Succeeded)
                    _effects.send(LoginEffect.NavigateToEmailVerification(user.email))
                }
                else -> {
                    userSession.setUser(user)
                    mutate(LoginMutation.Succeeded)
                    _effects.send(LoginEffect.NavigateToHome)
                }
            }
        }
    }
}
