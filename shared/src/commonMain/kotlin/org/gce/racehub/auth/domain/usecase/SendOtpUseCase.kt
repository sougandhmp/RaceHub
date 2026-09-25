package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.EmailVerificationResult
import org.gce.racehub.auth.domain.model.OtpPurpose
import org.gce.racehub.auth.domain.repository.AuthRepository

/** Sends an OTP to [email] for the given [OtpPurpose], which sets the email subject. */
class SendOtpUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String, purpose: OtpPurpose): EmailVerificationResult {
        if (email.isBlank()) return EmailVerificationResult.failure("Email cannot be empty")
        return authRepository.sendOtp(email.trim(), purpose.subject)
    }
}
