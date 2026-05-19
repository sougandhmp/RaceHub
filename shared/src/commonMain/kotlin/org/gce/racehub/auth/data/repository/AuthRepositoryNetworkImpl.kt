package org.gce.racehub.auth.data.repository

import org.gce.racehub.auth.data.dto.toDomainModel
import org.gce.racehub.auth.data.network.AuthService
import org.gce.racehub.auth.domain.model.AuthResult
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
class AuthRepositoryNetworkImpl(private val authService: AuthService) : AuthRepository {

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

    /**
     * Creates a new user account via the sign-up API endpoint.
     *
     * Note: This method is a placeholder. The actual endpoint needs to be
     * implemented once the backend sign-up API is ready.
     *
     * @param name The user's chosen display name
     * @param email The email address to register
     * @param password The chosen password
     * @return [AuthResult] with success or failure
     */
    override suspend fun signUp(name: String, email: String, password: String): AuthResult {
        // TODO: Implement sign-up API call once endpoint is available
        return AuthResult.failure("Sign-up is not yet implemented")
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
}

