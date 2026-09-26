package org.gce.racehub.auth.domain.usecase

import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthOutcome
import org.gce.racehub.auth.domain.model.authFailure
import org.gce.racehub.core.domain.model.User
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
internal class LoginUseCase(private val authRepository: AuthRepository) {

    /**
     * Validates [email] and [password], then calls [AuthRepository.login].
     *
     * @param email    The user's email address.
     * @param password The plain-text password (minimum 6 characters).
     * @return The authenticated [User] on success, otherwise the
     *         [org.gce.racehub.auth.domain.model.AuthFailure] explaining why not.
     */
    suspend operator fun invoke(email: String, password: String): AuthOutcome<User> {
        if (email.isBlank() || password.isBlank()) {
            return authFailure(AuthError.EmailAndPasswordRequired)
        }
        if (!email.contains("@")) {
            return authFailure(AuthError.InvalidEmail)
        }
        if (password.length < 6) {
            return authFailure(AuthError.PasswordTooShort)
        }
        return authRepository.login(email, password)
    }
}
