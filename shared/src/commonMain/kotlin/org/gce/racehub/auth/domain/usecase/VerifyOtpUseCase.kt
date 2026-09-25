package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.EmailVerificationResult
import org.gce.racehub.auth.domain.repository.AuthRepository

/** Verifies the OTP the user entered. Purpose-agnostic: the server matches by email + code. */
class VerifyOtpUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String, otp: String): EmailVerificationResult {
        if (email.isBlank()) return EmailVerificationResult.failure("Email cannot be empty")
        if (otp.isBlank()) return EmailVerificationResult.failure("Verification code cannot be empty")
        return authRepository.verifyOtp(email.trim(), otp.trim())
    }
}
