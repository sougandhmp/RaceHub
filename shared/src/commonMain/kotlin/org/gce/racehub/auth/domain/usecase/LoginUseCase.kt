package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.repository.AuthRepository

/**
 * Validates login credentials and delegates to [AuthRepository].
 *
 * Input validation is kept here (not in the repository) so the repository
 * never receives malformed data, and validation rules can be tested in
 * isolation without a fake repository.
 *
 * Shared between Android and iOS via the KMP `shared` module.
 */
class LoginUseCase(private val authRepository: AuthRepository) {

    /**
     * Validates [email] and [password], then calls [AuthRepository.login].
     *
     * @param email    The user's email address.
     * @param password The plain-text password (minimum 6 characters).
     * @return [AuthResult] with the authenticated [User] on success, or a
     *         human-readable error message on failure.
     */
    suspend fun execute(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.failure("Email and password cannot be empty")
        }
        if (!email.contains("@")) {
            return AuthResult.failure("Invalid email format")
        }
        if (password.length < 6) {
            return AuthResult.failure("Password must be at least 6 characters")
        }
        return authRepository.login(email, password)
    }
}
