package org.gce.racehub.auth.domain.repository

import org.gce.racehub.auth.domain.model.AuthResult

/**
 * Contract for authentication operations.
 *
 * The domain layer depends only on this interface; concrete implementations
 * (network, local fake, test doubles) are wired in the data layer. This
 * inversion keeps the domain free of I/O concerns.
 *
 * Shared between Android and iOS via the KMP `shared` module.
 */
interface AuthRepository {

    /**
     * Attempts to authenticate an existing user.
     *
     * @param email    The user's registered email address.
     * @param password The user's plain-text password.
     * @return [AuthResult.success] with the user profile, or
     *         [AuthResult.failure] with an error message.
     */
    suspend fun login(email: String, password: String): AuthResult

    /**
     * Creates a new user account.
     *
     * @param name     The user's chosen display name.
     * @param email    The email address to register.
     * @param password The chosen plain-text password (pre-validated by use case).
     * @return [AuthResult.success] with the new user profile, or
     *         [AuthResult.failure] with an error message.
     */
    suspend fun signUp(name: String, email: String, password: String): AuthResult
}
