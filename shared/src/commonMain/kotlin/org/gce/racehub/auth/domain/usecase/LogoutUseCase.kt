package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.repository.AuthRepository
import org.gce.racehub.auth.domain.session.UserSession

class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val userSession: UserSession
) {
    suspend operator fun invoke(): Boolean {
        val token = userSession.currentUser.value?.token
        if (token.isNullOrBlank()) {
            // No server token — clear local session without a network call
            userSession.clear()
            return true
        }
        val success = authRepository.logout(token)
        if (success) {
            userSession.clear()
        }
        return success
    }
}
