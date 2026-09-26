package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.PasswordResetResult
import org.gce.racehub.auth.domain.repository.AuthRepository

class ConfirmPasswordResetUseCase(private val authRepository: AuthRepository) {

    @Throws(Exception::class)
    suspend operator fun invoke(
        email: String,
        otp: String,
        newPassword: String,
        confirmPassword: String
    ): PasswordResetResult {
        if (otp.isBlank()) return PasswordResetResult.failure("Reset code cannot be empty")
        if (newPassword.length < 6) return PasswordResetResult.failure("Password must be at least 6 characters")
        if (newPassword != confirmPassword) return PasswordResetResult.failure("Passwords do not match")
        return authRepository.confirmPasswordReset(email, otp, newPassword)
    }
}
