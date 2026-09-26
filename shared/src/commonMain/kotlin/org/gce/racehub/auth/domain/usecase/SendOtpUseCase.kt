package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthOutcome
import org.gce.racehub.auth.domain.model.authFailure
import org.gce.racehub.auth.domain.model.OtpPurpose
import org.gce.racehub.auth.domain.repository.AuthRepository

/** Sends an OTP to [email] for the given [OtpPurpose], which sets the email subject. */
internal class SendOtpUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String, purpose: OtpPurpose): AuthOutcome<Unit> {
        if (email.isBlank()) return authFailure(AuthError.EmailRequired)
        return authRepository.sendOtp(email.trim(), purpose.subject)
    }
}
