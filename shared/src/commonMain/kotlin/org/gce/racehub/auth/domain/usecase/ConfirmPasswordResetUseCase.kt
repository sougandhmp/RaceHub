package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.PasswordResetResult
import org.gce.racehub.auth.domain.repository.AuthRepository

internal class ConfirmPasswordResetUseCase(private val authRepository: AuthRepository) {

    @Throws(Exception::class)
    suspend operator fun invoke(
        email: String,
        otp: String,
        newPassword: String,
        confirmPassword: String
    ): PasswordResetResult {
        if (otp.isBlank()) return PasswordResetResult.failure(AuthError.CodeRequired)
        if (newPassword.length < 6) return PasswordResetResult.failure(AuthError.PasswordTooShort)
        if (newPassword != confirmPassword) return PasswordResetResult.failure(AuthError.PasswordsDoNotMatch)
        return authRepository.confirmPasswordReset(email, otp, newPassword)
    }
}
