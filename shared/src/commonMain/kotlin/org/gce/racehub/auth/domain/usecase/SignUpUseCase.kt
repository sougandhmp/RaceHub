package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.repository.AuthRepository

/**
 * Validates the sign-up form and delegates account creation to [AuthRepository].
 *
 * All field-level validation lives here so the repository only receives
 * well-formed data and validation logic can be unit-tested without I/O.
 *
 * Shared between Android and iOS via the KMP `shared` module.
 */
class SignUpUseCase(private val authRepository: AuthRepository) {

    /**
     * Validates the four sign-up fields, then calls [AuthRepository.signUp].
     *
     * @param name            The user's display name.
     * @param email           The user's email address.
     * @param password        The chosen password (minimum 6 characters).
     * @param confirmPassword Must match [password] exactly.
     * @return [AuthResult] with the created [User] on success, or a
     *         human-readable error message on the first validation failure.
     */
    suspend fun execute(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): AuthResult {
        if (name.isBlank()) return AuthResult.failure("Name cannot be empty")
        if (email.isBlank()) return AuthResult.failure("Email cannot be empty")
        if (!email.contains("@")) return AuthResult.failure("Invalid email format")
        if (password.length < 6) return AuthResult.failure("Password must be at least 6 characters")
        // Checked last so length feedback takes priority over mismatch feedback.
        if (password != confirmPassword) return AuthResult.failure("Passwords do not match")
        return authRepository.signUp(name, email, password)
    }
}
