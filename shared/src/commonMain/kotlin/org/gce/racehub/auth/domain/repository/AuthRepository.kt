package org.gce.racehub.auth.domain.repository

import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.model.PasswordResetResult

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
     * @param username The user's chosen display name / handle.
     * @param email    The email address to register.
     * @param password The chosen plain-text password (pre-validated by use case).
     * @param country  ISO 3166-1 alpha-2 country code, e.g. "AU".
     * @return [AuthResult.success] with the new user profile, or
     *         [AuthResult.failure] with an error message.
     */
    suspend fun signUp(username: String, email: String, password: String, country: String): AuthResult

    /**
     * Invalidates the session on the server.
     *
     * @param token The bearer token issued at login.
     * @return `true` if the server acknowledged the logout, `false` otherwise.
     */
    suspend fun logout(token: String): Boolean

    /**
     * Sends a one-time password to [email] for the password-reset flow.
     *
     * @return [PasswordResetResult.success] if the server accepted the request,
     *         [PasswordResetResult.failure] with an error message otherwise.
     */
    suspend fun requestPasswordReset(email: String): PasswordResetResult

    /**
     * Confirms the password reset using the OTP the user received.
     *
     * @param email       The address the reset was requested for.
     * @param otp         The one-time code from the email.
     * @param newPassword The new plain-text password (pre-validated by the use case).
     * @return [PasswordResetResult.success] on success,
     *         [PasswordResetResult.failure] with an error message otherwise.
     */
    suspend fun confirmPasswordReset(email: String, otp: String, newPassword: String): PasswordResetResult
}
