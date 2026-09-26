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
import org.gce.racehub.auth.domain.usecase.SendOtpUseCase
import org.gce.racehub.auth.domain.usecase.SignUpUseCase

/**
 * Shared MVI ViewModel for the sign-up form (Android and iOS). Creating the
 * account does not sign the user in: they must verify their email first.
 */
class SignUpViewModel(
    private val signUp: SignUpUseCase,
    private val sendOtp: SendOtpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())

    /** Swift sees `state` and `stateFlow` via KMP-NativeCoroutines. */
    @NativeCoroutinesState
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    private val _effects = Channel<SignUpEffect>(Channel.BUFFERED)

    /** One-off events. Swift sees `effects` as a native flow. */
    @NativeCoroutines
    val effects: Flow<SignUpEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: SignUpIntent) {
        when (intent) {
            is SignUpIntent.UsernameChanged -> edit { it.copy(username = intent.username) }
            is SignUpIntent.EmailChanged -> edit { it.copy(email = intent.email) }
            is SignUpIntent.PasswordChanged -> edit { it.copy(password = intent.password) }
            is SignUpIntent.ConfirmPasswordChanged -> edit { it.copy(confirmPassword = intent.confirmPassword) }
            is SignUpIntent.CountryChanged -> edit { it.copy(country = intent.country) }
            SignUpIntent.TogglePasswordVisibility -> edit { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            SignUpIntent.ToggleConfirmPasswordVisibility ->
                edit { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            SignUpIntent.Submit -> submit()
        }
    }

    private fun edit(transform: (SignUpState) -> SignUpState) = mutate(SignUpMutation.FieldsChanged(transform))

    private fun mutate(mutation: SignUpMutation) = _state.update { SignUpReducer.reduce(it, mutation) }

    private fun submit() {
        val form = _state.value
        if (form.isLoading) return
        mutate(SignUpMutation.Submitted)
        viewModelScope.launch {
            val result = signUp(form.username, form.email, form.password, form.country, form.confirmPassword)
            if (!result.isSuccess) {
                mutate(SignUpMutation.Failed(result.error ?: "Sign up failed. Please try again."))
                return@launch
            }
            val otp = sendOtp(form.email, OtpPurpose.EMAIL_VERIFICATION)
            if (otp.isSuccess) {
                mutate(SignUpMutation.Succeeded)
                _effects.send(SignUpEffect.NavigateToEmailVerification(form.email))
            } else {
                mutate(SignUpMutation.Failed(otp.error ?: "Could not send the verification code."))
            }
        }
    }
}
