package org.gce.racehub.fake

import org.gce.racehub.core.domain.model.User
import org.gce.racehub.auth.domain.repository.AuthRepository
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.auth.domain.model.AuthOutcome

internal class FakeAuthRepository : AuthRepository {
    var loginResult: AuthOutcome<User> = DataResult.Success(User("1", "test@test.com", "Test User", token = "tok"))
    var signUpResult: AuthOutcome<User> = DataResult.Success(User("1", "test@test.com", "Test User", token = "tok"))
    var logoutResult: Boolean = true
    var sendOtpResult: AuthOutcome<Unit> = DataResult.Success(Unit)
    var resendOtpResult: AuthOutcome<Unit> = DataResult.Success(Unit)
    var verifyOtpResult: AuthOutcome<Unit> = DataResult.Success(Unit)
    var requestResetResult: AuthOutcome<Unit> = DataResult.Success(Unit)
    var confirmResetResult: AuthOutcome<Unit> = DataResult.Success(Unit)

    var loginCallCount = 0
    var logoutCallCount = 0
    var lastLogoutToken: String? = null

    // Captured arguments from the most recent OTP calls (for assertions).
    var lastSendOtpEmail: String? = null
    var lastSendOtpSubject: String? = null
    var lastResendOtpEmail: String? = null
    var lastResendOtpSubject: String? = null
    var lastVerifyOtpEmail: String? = null
    var lastVerifyOtpCode: String? = null

    override suspend fun login(email: String, password: String): AuthOutcome<User> {
        loginCallCount++
        return loginResult
    }

    override suspend fun signUp(username: String, email: String, password: String, country: String): AuthOutcome<User> = signUpResult

    override suspend fun logout(token: String): Boolean {
        logoutCallCount++
        lastLogoutToken = token
        return logoutResult
    }

    override suspend fun requestPasswordReset(email: String): AuthOutcome<Unit> = requestResetResult

    override suspend fun confirmPasswordReset(email: String, otp: String, newPassword: String): AuthOutcome<Unit> =
        confirmResetResult

    override suspend fun sendOtp(email: String, subject: String): AuthOutcome<Unit> {
        lastSendOtpEmail = email
        lastSendOtpSubject = subject
        return sendOtpResult
    }

    override suspend fun resendOtp(email: String, subject: String): AuthOutcome<Unit> {
        lastResendOtpEmail = email
        lastResendOtpSubject = subject
        return resendOtpResult
    }

    override suspend fun verifyOtp(email: String, otp: String): AuthOutcome<Unit> {
        lastVerifyOtpEmail = email
        lastVerifyOtpCode = otp
        return verifyOtpResult
    }
}
