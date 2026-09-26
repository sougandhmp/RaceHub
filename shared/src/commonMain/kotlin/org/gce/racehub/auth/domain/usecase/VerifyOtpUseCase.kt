package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthOutcome
import org.gce.racehub.auth.domain.model.authFailure
import org.gce.racehub.auth.domain.repository.AuthRepository

/** Verifies the OTP the user entered. Purpose-agnostic: the server matches by email + code. */
internal class VerifyOtpUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String, otp: String): AuthOutcome<Unit> {
        if (email.isBlank()) return authFailure(AuthError.EmailRequired)
        if (otp.isBlank()) return authFailure(AuthError.CodeRequired)
        return authRepository.verifyOtp(email.trim(), otp.trim())
    }
}
