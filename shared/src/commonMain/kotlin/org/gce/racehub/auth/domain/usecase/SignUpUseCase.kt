package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthOutcome
import org.gce.racehub.auth.domain.model.authFailure
import org.gce.racehub.core.domain.model.User
import org.gce.racehub.auth.domain.repository.AuthRepository

internal class SignUpUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        country: String,
        confirmPassword: String
    ): AuthOutcome<User> {
        if (username.isBlank()) return authFailure(AuthError.UsernameRequired)
        if (email.isBlank()) return authFailure(AuthError.EmailRequired)
        if (!email.contains("@")) return authFailure(AuthError.InvalidEmail)
        if (password.length < 6) return authFailure(AuthError.PasswordTooShort)
        if (password != confirmPassword) return authFailure(AuthError.PasswordsDoNotMatch)
        if (country.isBlank()) return authFailure(AuthError.CountryRequired)
        return authRepository.signUp(username, email, password, country)
    }
}
