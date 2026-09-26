package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.repository.AuthRepository

internal class SignUpUseCase(private val authRepository: AuthRepository) {

    @Throws(Exception::class)
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        country: String,
        confirmPassword: String
    ): AuthResult {
        if (username.isBlank()) return AuthResult.failure(AuthError.UsernameRequired)
        if (email.isBlank()) return AuthResult.failure(AuthError.EmailRequired)
        if (!email.contains("@")) return AuthResult.failure(AuthError.InvalidEmail)
        if (password.length < 6) return AuthResult.failure(AuthError.PasswordTooShort)
        if (password != confirmPassword) return AuthResult.failure(AuthError.PasswordsDoNotMatch)
        if (country.isBlank()) return AuthResult.failure(AuthError.CountryRequired)
        return authRepository.signUp(username, email, password, country)
    }
}
