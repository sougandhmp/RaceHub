package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthOutcome
import org.gce.racehub.auth.domain.model.authFailure
import org.gce.racehub.auth.domain.repository.AuthRepository

internal class ConfirmPasswordResetUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(
        email: String,
        otp: String,
        newPassword: String,
        confirmPassword: String
    ): AuthOutcome<Unit> {
        if (otp.isBlank()) return authFailure(AuthError.CodeRequired)
        if (newPassword.length < 6) return authFailure(AuthError.PasswordTooShort)
        if (newPassword != confirmPassword) return authFailure(AuthError.PasswordsDoNotMatch)
        return authRepository.confirmPasswordReset(email, otp, newPassword)
    }
}
