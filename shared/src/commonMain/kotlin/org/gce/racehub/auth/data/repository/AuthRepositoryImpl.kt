package org.gce.racehub.auth.data.repository

import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.model.EmailVerificationResult
import org.gce.racehub.auth.domain.model.PasswordResetResult
import org.gce.racehub.auth.domain.model.User
import org.gce.racehub.auth.domain.repository.AuthRepository

/**
 * Fake in-memory implementation of [AuthRepository].
 *
 * Simulates a backend without any network I/O. Replace this with a real
 * Ktor/Retrofit implementation when the API is ready — nothing outside
 * this class needs to change because the rest of the code depends on the
 * [AuthRepository] interface, not this concrete class.
 */
internal class AuthRepositoryImpl : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult {
        return when {
            email == "driver@racehub.com" && password == "race123" ->
                AuthResult.success(User(id = "1", email = email, name = "Race Driver"))
            else ->
                AuthResult.failure(AuthError.Rejected, "Invalid email or password")
        }
    }

    override suspend fun signUp(username: String, email: String, password: String, country: String): AuthResult {
        return AuthResult.success(User(id = "2", email = email, name = username))
    }

    override suspend fun logout(token: String): Boolean = true

    override suspend fun requestPasswordReset(email: String): PasswordResetResult =
        PasswordResetResult.success()

    override suspend fun confirmPasswordReset(email: String, otp: String, newPassword: String): PasswordResetResult =
        if (otp == "123456") PasswordResetResult.success()
        else PasswordResetResult.failure(AuthError.Rejected, "Invalid reset code")

    override suspend fun sendOtp(email: String, subject: String): EmailVerificationResult =
        EmailVerificationResult.success()

    override suspend fun resendOtp(email: String, subject: String): EmailVerificationResult =
        EmailVerificationResult.success()

    override suspend fun verifyOtp(email: String, otp: String): EmailVerificationResult =
        if (otp == "123456") EmailVerificationResult.success()
        else EmailVerificationResult.failure(AuthError.Rejected, "Invalid verification code")
}
