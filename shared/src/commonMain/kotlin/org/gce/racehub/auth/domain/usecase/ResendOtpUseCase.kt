package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.EmailVerificationResult
import org.gce.racehub.auth.domain.model.OtpPurpose
import org.gce.racehub.auth.domain.repository.AuthRepository

/** Re-sends an OTP to [email] for the given [OtpPurpose]. */
class ResendOtpUseCase(private val authRepository: AuthRepository) {

    @Throws(Exception::class)
    suspend operator fun invoke(email: String, purpose: OtpPurpose): EmailVerificationResult {
        if (email.isBlank()) return EmailVerificationResult.failure("Email cannot be empty")
        return authRepository.resendOtp(email.trim(), purpose.subject)
    }
}
