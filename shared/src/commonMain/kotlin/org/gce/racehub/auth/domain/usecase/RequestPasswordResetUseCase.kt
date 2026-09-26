package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthOutcome
import org.gce.racehub.auth.domain.model.authFailure
import org.gce.racehub.auth.domain.repository.AuthRepository

internal class RequestPasswordResetUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String): AuthOutcome<Unit> {
        if (email.isBlank()) return authFailure(AuthError.EmailRequired)
        if (!email.contains("@")) return authFailure(AuthError.InvalidEmail)
        return authRepository.requestPasswordReset(email)
    }
}
