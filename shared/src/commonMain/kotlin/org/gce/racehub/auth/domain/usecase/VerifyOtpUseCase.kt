package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.EmailVerificationResult
import org.gce.racehub.auth.domain.repository.AuthRepository

/** Verifies the OTP the user entered. Purpose-agnostic: the server matches by email + code. */
internal class VerifyOtpUseCase(private val authRepository: AuthRepository) {

    @Throws(Exception::class)
    suspend operator fun invoke(email: String, otp: String): EmailVerificationResult {
        if (email.isBlank()) return EmailVerificationResult.failure(AuthError.EmailRequired)
        if (otp.isBlank()) return EmailVerificationResult.failure(AuthError.CodeRequired)
        return authRepository.verifyOtp(email.trim(), otp.trim())
    }
}
