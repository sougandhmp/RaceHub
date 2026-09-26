package org.gce.racehub.auth.data.repository

import kotlinx.coroutines.CancellationException
import org.gce.racehub.auth.data.dto.OtpResponseDto
import org.gce.racehub.auth.data.dto.toDomainModel
import org.gce.racehub.auth.data.network.AuthService
import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.model.EmailVerificationResult
import org.gce.racehub.auth.domain.model.PasswordResetResult
import org.gce.racehub.auth.domain.repository.AuthRepository
import org.gce.racehub.core.data.toDataError
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.util.logError

/**
 * Network-based implementation of [AuthRepository].
 *
 * Makes authenticated HTTP requests to the RaceHub API.
 * Replaces [AuthRepositoryImpl] when the backend is available.
 *
 * @param authService The network service that handles API calls
 */
internal class AuthRepositoryNetworkImpl(private val authService: AuthService) : AuthRepository {

    private companion object {
        const val TAG = "AuthRepository"
    }

    override suspend fun login(email: String, password: String): AuthResult =
        call("log in", { AuthResult.failure(it) }) {
            val response = authService.login(email, password)
            when {
                !response.success || response.data == null -> AuthResult.failure(AuthError.Rejected, response.message)
                else -> response.toDomainModel()?.let { AuthResult.success(it) } ?: AuthResult.failure(AuthError.Server)
            }
        }

    override suspend fun signUp(username: String, email: String, password: String, country: String): AuthResult =
        call("sign up", { AuthResult.failure(it) }) {
            val response = authService.signUp(username, email, password, country)
            if (response.success && response.data != null) {
                AuthResult.success(response.data.user.toDomainModel(response.data.token))
            } else {
                AuthResult.failure(AuthError.Rejected, response.message)
            }
        }

    /** Best effort: false on any failure (the caller signs out locally regardless). */
    override suspend fun logout(token: String): Boolean =
        call("log out", { false }) { authService.logout(token).success }

    override suspend fun requestPasswordReset(email: String): PasswordResetResult =
        call("request password reset", { PasswordResetResult.failure(it) }) {
            val response = authService.requestPasswordReset(email)
            if (response.success) PasswordResetResult.success()
            else PasswordResetResult.failure(AuthError.Rejected, response.message)
        }

    override suspend fun confirmPasswordReset(email: String, otp: String, newPassword: String): PasswordResetResult =
        call("confirm password reset", { PasswordResetResult.failure(it) }) {
            val response = authService.confirmPasswordReset(email, otp, newPassword)
            if (response.success) PasswordResetResult.success()
            else PasswordResetResult.failure(AuthError.Rejected, response.message)
        }

    override suspend fun sendOtp(email: String, subject: String): EmailVerificationResult =
        otpCall("send verification code") { authService.sendOtp(email, subject) }

    override suspend fun resendOtp(email: String, subject: String): EmailVerificationResult =
        otpCall("resend verification code") { authService.resendOtp(email, subject) }

    override suspend fun verifyOtp(email: String, otp: String): EmailVerificationResult =
        otpCall("verify code") { authService.verifyOtp(email, otp) }

    private suspend fun otpCall(what: String, request: suspend () -> OtpResponseDto): EmailVerificationResult =
        call(what, { EmailVerificationResult.failure(it) }) {
            val response = request()
            if (response.success) EmailVerificationResult.success()
            else EmailVerificationResult.failure(AuthError.Rejected, response.message)
        }

    /**
     * The auth error boundary: a server "no" is handled by [block]; an exception
     * is logged and mapped to Network / Server / Unknown via [onFailure].
     * Cancellation propagates.
     */
    private suspend fun <T> call(what: String, onFailure: (AuthError) -> T, block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logError(TAG, "Failed to $what", e)
        onFailure(
            when (e.toDataError()) {
                DataError.Network -> AuthError.Network
                DataError.Server -> AuthError.Server
                DataError.Unknown -> AuthError.Unknown
            }
        )
    }
}
