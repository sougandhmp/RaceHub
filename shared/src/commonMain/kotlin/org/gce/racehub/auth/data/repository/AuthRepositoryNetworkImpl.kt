package org.gce.racehub.auth.data.repository

import org.gce.racehub.auth.data.dto.toDomainModel
import org.gce.racehub.auth.data.network.AuthService
import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthFailure
import org.gce.racehub.auth.domain.model.AuthOutcome
import org.gce.racehub.auth.domain.model.authFailure
import org.gce.racehub.auth.domain.repository.AuthRepository
import org.gce.racehub.core.data.safeCall
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.core.domain.dataOrNull
import org.gce.racehub.core.domain.model.User
import org.gce.racehub.core.domain.mapError

/**
 * Network-based implementation of [AuthRepository].
 *
 * Makes authenticated HTTP requests to the RaceHub API.
 *
 * @param authService The network service that handles API calls
 */
internal class AuthRepositoryNetworkImpl(private val authService: AuthService) : AuthRepository {

    private companion object {
        const val TAG = "AuthRepository"
    }

    override suspend fun login(email: String, password: String): AuthOutcome<User> = call("log in") {
        val response = authService.login(email, password)
        when {
            !response.success || response.data == null -> authFailure(AuthError.Rejected, response.message)
            else -> response.toDomainModel()?.let { DataResult.Success(it) } ?: authFailure(AuthError.Server)
        }
    }

    override suspend fun signUp(username: String, email: String, password: String, country: String): AuthOutcome<User> =
        call("sign up") {
            val response = authService.signUp(username, email, password, country)
            if (response.success && response.data != null) {
                DataResult.Success(response.data.user.toDomainModel(response.data.token))
            } else {
                authFailure(AuthError.Rejected, response.message)
            }
        }

    /** Best effort: false on any failure (the caller signs out locally regardless). */
    override suspend fun logout(token: String): Boolean =
        safeCall(TAG, "log out") { authService.logout(token).success }.dataOrNull() ?: false

    override suspend fun requestPasswordReset(email: String): AuthOutcome<Unit> =
        acknowledged("request password reset") {
            authService.requestPasswordReset(email).let { it.success to it.message }
        }

    override suspend fun confirmPasswordReset(email: String, otp: String, newPassword: String): AuthOutcome<Unit> =
        acknowledged("confirm password reset") {
            authService.confirmPasswordReset(email, otp, newPassword).let { it.success to it.message }
        }

    override suspend fun sendOtp(email: String, subject: String): AuthOutcome<Unit> =
        acknowledged("send verification code") { authService.sendOtp(email, subject).let { it.success to it.message } }

    override suspend fun resendOtp(email: String, subject: String): AuthOutcome<Unit> =
        acknowledged("resend verification code") { authService.resendOtp(email, subject).let { it.success to it.message } }

    override suspend fun verifyOtp(email: String, otp: String): AuthOutcome<Unit> =
        acknowledged("verify code") { authService.verifyOtp(email, otp).let { it.success to it.message } }

    /** For endpoints that answer only yes or no: [request] returns the success flag and the server's message. */
    private suspend fun acknowledged(what: String, request: suspend () -> Pair<Boolean, String>): AuthOutcome<Unit> =
        call(what) {
            val (success, message) = request()
            if (success) DataResult.Success(Unit) else authFailure(AuthError.Rejected, message)
        }

    /**
     * The auth error boundary: a server "no" is returned by [block]; an exception
     * is logged by [safeCall] and becomes Network / Server / Unknown.
     * Cancellation propagates.
     */
    private suspend fun <T> call(what: String, block: suspend () -> AuthOutcome<T>): AuthOutcome<T> =
        when (val result = safeCall(TAG, what) { block() }) {
            is DataResult.Success -> result.data
            is DataResult.Failure -> result.mapError(AuthFailure::from)
        }
}
