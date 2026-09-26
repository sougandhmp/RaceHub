package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.repository.AuthRepository
import org.gce.racehub.core.domain.session.UserSession

/**
 * Signs the user out. Signing out always succeeds on the device: the local
 * session is cleared unconditionally, and revoking the token on the server is
 * best effort — offline, or with an expired token, the user is still signed
 * out and the token simply lapses server-side.
 */
internal class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val userSession: UserSession
) {
    /** @return true if the server confirmed the token was revoked (informational only). */
    @Throws(Exception::class)
    suspend operator fun invoke(): Boolean {
        val token = userSession.currentUser.value?.token
        userSession.clear()
        if (token.isNullOrBlank()) return true
        return authRepository.logout(token)
    }
}
