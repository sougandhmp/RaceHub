package org.gce.racehub.auth.data.repository

import org.gce.racehub.auth.data.dto.toDomainModel
import org.gce.racehub.auth.data.network.AuthService
import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.model.EmailVerificationResult
import org.gce.racehub.auth.domain.model.PasswordResetResult
import org.gce.racehub.auth.domain.repository.AuthRepository

/**
 * Network-based implementation of [AuthRepository].
 *
 * Makes authenticated HTTP requests to the RaceHub API.
 * Replaces [AuthRepositoryImpl] when the backend is available.
 *
 * @param authService The network service that handles API calls
 */
internal class AuthRepositoryNetworkImpl(private val authService: AuthService) : AuthRepository {

    /**
     * Authenticates a user by calling the login API endpoint.
     *
     * Communicates with: POST /api/v1/auth/login
     * Expected response: [LoginResponseDto] containing token and user data
     *
     * @param email The user's email address
     * @param password The user's password
     * @return [AuthResult.success] with user data and token on success,
     *         or [AuthResult.failure] with an error message on failure
     */
    override suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = authService.login(email, password)

            if (response.success && response.data != null) {
                val user = response.toDomainModel()
                if (user != null) {
                    AuthResult.success(user)
                } else {
                    AuthResult.failure("Failed to parse user data from response")
                }
            } else {
                AuthResult.failure(response.message)
            }
        } catch (e: Exception) {
            AuthResult.failure(
                e.message ?: "An error occurred during login. Please check your connection and try again."
            )
        }
    }

    override suspend fun signUp(username: String, email: String, password: String, country: String): AuthResult {
        return try {
            val response = authService.signUp(username, email, password, country)
            if (response.success && response.data != null) {
                AuthResult.success(response.data.user.toDomainModel(response.data.token))
            } else {
                AuthResult.failure(response.message)
            }
        } catch (_: Exception) {
            AuthResult.failure("Could not create account. Check your connection and try again.")
        }
    }

    override suspend fun logout(token: String): Boolean {
        return try {
            authService.logout(token).success
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun requestPasswordReset(email: String): PasswordResetResult {
        return try {
            val response = authService.requestPasswordReset(email)
            if (response.success) PasswordResetResult.success()
            else PasswordResetResult.failure(response.message)
        } catch (e: Exception) {
            PasswordResetResult.failure("Could not send reset code. Check your connection and try again.")
        }
    }

    override suspend fun confirmPasswordReset(
        email: String,
        otp: String,
        newPassword: String
    ): PasswordResetResult {
        return try {
            val response = authService.confirmPasswordReset(email, otp, newPassword)
            if (response.success) PasswordResetResult.success()
            else PasswordResetResult.failure(response.message)
        } catch (e: Exception) {
            PasswordResetResult.failure("Could not reset password. Check your connection and try again.")
        }
    }

    override suspend fun sendOtp(email: String, subject: String): EmailVerificationResult {
        return try {
            val response = authService.sendOtp(email, subject)
            if (response.success) EmailVerificationResult.success()
            else EmailVerificationResult.failure(response.message)
        } catch (e: Exception) {
            EmailVerificationResult.failure("Could not send verification code. Check your connection and try again.")
        }
    }

    override suspend fun resendOtp(email: String, subject: String): EmailVerificationResult {
        return try {
            val response = authService.resendOtp(email, subject)
            if (response.success) EmailVerificationResult.success()
            else EmailVerificationResult.failure(response.message)
        } catch (e: Exception) {
            EmailVerificationResult.failure("Could not resend code. Check your connection and try again.")
        }
    }

    override suspend fun verifyOtp(email: String, otp: String): EmailVerificationResult {
        return try {
            val response = authService.verifyOtp(email, otp)
            if (response.success) EmailVerificationResult.success()
            else EmailVerificationResult.failure(response.message)
        } catch (e: Exception) {
            EmailVerificationResult.failure("Could not verify code. Check your connection and try again.")
        }
    }
}

