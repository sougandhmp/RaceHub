package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.repository.AuthRepository

class SignUpUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        country: String,
        confirmPassword: String
    ): AuthResult {
        if (username.isBlank()) return AuthResult.failure("Username cannot be empty")
        if (email.isBlank()) return AuthResult.failure("Email cannot be empty")
        if (!email.contains("@")) return AuthResult.failure("Invalid email format")
        if (password.length < 6) return AuthResult.failure("Password must be at least 6 characters")
        if (password != confirmPassword) return AuthResult.failure("Passwords do not match")
        if (country.isBlank()) return AuthResult.failure("Country cannot be empty")
        return authRepository.signUp(username, email, password, country)
    }
}
