package org.gce.racehub.auth.domain.repository

import org.gce.racehub.auth.domain.model.AuthFailure
import org.gce.racehub.auth.domain.model.AuthOutcome
import org.gce.racehub.core.domain.model.User

/**
 * Contract for authentication operations.
 *
 * The domain layer depends only on this interface; concrete implementations
 * (network, local fake, test doubles) are wired in the data layer. This
 * inversion keeps the domain free of I/O concerns. Implementations are the
 * error boundary: failures come back as an [AuthFailure], never as exceptions.
 *
 * Shared between Android and iOS via the KMP `shared` module.
 */
internal interface AuthRepository {

    /**
     * Attempts to authenticate an existing user.
     *
     * @param email    The user's registered email address.
     * @param password The user's plain-text password.
     * @return the user profile, or
     *         the [AuthFailure] explaining why not.
     */
    suspend fun login(email: String, password: String): AuthOutcome<User>

    /**
     * Creates a new user account.
     *
     * @param username The user's chosen display name / handle.
     * @param email    The email address to register.
     * @param password The chosen plain-text password (pre-validated by use case).
     * @param country  ISO 3166-1 alpha-2 country code, e.g. "AU".
     * @return the new user profile, or
     *         the [AuthFailure] explaining why not.
     */
    suspend fun signUp(username: String, email: String, password: String, country: String): AuthOutcome<User>

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
     * @return success if the server accepted the request,
     *         otherwise the [AuthFailure] explaining why not.
     */
    suspend fun requestPasswordReset(email: String): AuthOutcome<Unit>

    /**
     * Confirms the password reset using the OTP the user received.
     *
     * @param email       The address the reset was requested for.
     * @param otp         The one-time code from the email.
     * @param newPassword The new plain-text password (pre-validated by the use case).
     * @return success once the password is changed,
     *         otherwise the [AuthFailure] explaining why not.
     */
    suspend fun confirmPasswordReset(email: String, otp: String, newPassword: String): AuthOutcome<Unit>

    /**
     * Sends a one-time verification code to [email].
     *
     * @param subject The subject line for the OTP email (varies by purpose,
     *                e.g. email verification vs. password reset).
     * @return success if the server accepted the request,
     *         otherwise the [AuthFailure] explaining why not.
     */
    suspend fun sendOtp(email: String, subject: String): AuthOutcome<Unit>

    /**
     * Requests a fresh OTP to be re-sent to [email].
     *
     * @param subject The subject line for the OTP email.
     * @return success if the server accepted the request,
     *         otherwise the [AuthFailure] explaining why not.
     */
    suspend fun resendOtp(email: String, subject: String): AuthOutcome<Unit>

    /**
     * Verifies the OTP the user received at [email].
     *
     * @param otp The one-time code from the email.
     * @return success once the code is accepted,
     *         otherwise the [AuthFailure] explaining why not.
     */
    suspend fun verifyOtp(email: String, otp: String): AuthOutcome<Unit>
}
