package org.gce.racehub.auth.presentation

import org.gce.racehub.auth.domain.model.AuthFailure

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
import org.gce.racehub.auth.domain.usecase.ResendOtpUseCase
import org.gce.racehub.auth.domain.usecase.VerifyOtpUseCase

/**
 * Shared MVI ViewModel for email verification (Android and iOS). Send
 * [EmailVerificationIntent.Open] with the address when the screen appears.
 */
class EmailVerificationViewModel internal constructor(
    private val verifyOtp: VerifyOtpUseCase,
    private val resendOtp: ResendOtpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EmailVerificationState())

    /** Swift sees `state` and `stateFlow` via KMP-NativeCoroutines. */
    @NativeCoroutinesState
    val state: StateFlow<EmailVerificationState> = _state.asStateFlow()

    private val _effects = Channel<EmailVerificationEffect>(Channel.BUFFERED)

    /** One-off events. Swift sees `effects` as a native flow. */
    @NativeCoroutines
    val effects: Flow<EmailVerificationEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: EmailVerificationIntent) {
        when (intent) {
            is EmailVerificationIntent.Open -> mutate(EmailVerificationMutation.Opened(intent.email))
            is EmailVerificationIntent.OtpChanged -> mutate(EmailVerificationMutation.OtpChanged(intent.otp))
            EmailVerificationIntent.Verify -> verify()
            EmailVerificationIntent.ResendCode -> resend()
        }
    }

    private fun mutate(mutation: EmailVerificationMutation) =
        _state.update { EmailVerificationReducer.reduce(it, mutation) }

    private fun verify() {
        val current = _state.value
        if (current.isVerifying) return
        mutate(EmailVerificationMutation.VerifyStarted)
        viewModelScope.launch {
            val result = verifyOtp(current.email, current.otp)
            if (result.isSuccess) {
                mutate(EmailVerificationMutation.Verified)
                _effects.send(EmailVerificationEffect.EmailVerified)
            } else {
                mutate(EmailVerificationMutation.Failed(result.failure ?: AuthFailure.Unknown))
            }
        }
    }

    private fun resend() {
        val current = _state.value
        if (current.isResending) return
        mutate(EmailVerificationMutation.ResendStarted)
        viewModelScope.launch {
            val result = resendOtp(current.email, OtpPurpose.EMAIL_VERIFICATION)
            mutate(
                if (result.isSuccess) EmailVerificationMutation.Resent
                else EmailVerificationMutation.Failed(result.failure ?: AuthFailure.Unknown)
            )
        }
    }
}
