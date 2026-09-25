package org.gce.racehub.emailverification

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
import org.gce.racehub.auth.domain.usecase.ResendOtpUseCase
import org.gce.racehub.auth.domain.usecase.VerifyOtpUseCase

class EmailVerificationViewModel(
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val resendOtpUseCase: ResendOtpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EmailVerificationState())
    val state: StateFlow<EmailVerificationState> = _state.asStateFlow()

    private val _effect = Channel<EmailVerificationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: EmailVerificationIntent) {
        when (intent) {
            is EmailVerificationIntent.SetEmail -> _state.update {
                if (it.email == intent.email) it else it.copy(email = intent.email)
            }
            is EmailVerificationIntent.OtpChanged -> _state.update { it.copy(otp = intent.otp) }
            is EmailVerificationIntent.Verify -> verify()
            is EmailVerificationIntent.ResendCode -> resend()
            is EmailVerificationIntent.DismissError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun verify() {
        if (_state.value.isLoading) return
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            val result = verifyOtpUseCase(current.email, current.otp)
            if (result.isSuccess) {
                _state.update { it.copy(isLoading = false) }
                _effect.send(EmailVerificationEffect.EmailVerified)
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = result.error) }
            }
        }
    }

    private fun resend() {
        if (_state.value.isResending) return
        val email = _state.value.email
        viewModelScope.launch {
            _state.update { it.copy(isResending = true, errorMessage = null, infoMessage = null) }
            val result = resendOtpUseCase(email, OtpPurpose.EMAIL_VERIFICATION)
            if (result.isSuccess) {
                _state.update { it.copy(isResending = false, infoMessage = "A new code has been sent.") }
            } else {
                _state.update { it.copy(isResending = false, errorMessage = result.error) }
            }
        }
    }
}
