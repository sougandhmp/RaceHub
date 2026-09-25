package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.PasswordResetResult
import org.gce.racehub.auth.domain.repository.AuthRepository

class RequestPasswordResetUseCase(private val authRepository: AuthRepository) {

    @Throws(Exception::class)

    suspend operator fun invoke(email: String): PasswordResetResult {
        if (email.isBlank()) return PasswordResetResult.failure("Email cannot be empty")
        if (!email.contains("@")) return PasswordResetResult.failure("Invalid email format")
        return authRepository.requestPasswordReset(email)
    }
}
