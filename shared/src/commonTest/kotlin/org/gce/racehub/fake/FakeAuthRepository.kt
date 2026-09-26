package org.gce.racehub.fake

import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.model.EmailVerificationResult
import org.gce.racehub.auth.domain.model.PasswordResetResult
import org.gce.racehub.auth.domain.model.User
import org.gce.racehub.auth.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var loginResult: AuthResult = AuthResult.success(User("1", "test@test.com", "Test User", token = "tok"))
    var signUpResult: AuthResult = AuthResult.success(User("1", "test@test.com", "Test User", token = "tok"))
    var logoutResult: Boolean = true
    var sendOtpResult: EmailVerificationResult = EmailVerificationResult.success()
    var resendOtpResult: EmailVerificationResult = EmailVerificationResult.success()
    var verifyOtpResult: EmailVerificationResult = EmailVerificationResult.success()
    var requestResetResult: PasswordResetResult = PasswordResetResult.success()
    var confirmResetResult: PasswordResetResult = PasswordResetResult.success()

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

    override suspend fun login(email: String, password: String): AuthResult {
        loginCallCount++
        return loginResult
    }

    override suspend fun signUp(username: String, email: String, password: String, country: String): AuthResult = signUpResult

    override suspend fun logout(token: String): Boolean {
        logoutCallCount++
        lastLogoutToken = token
        return logoutResult
    }

    override suspend fun requestPasswordReset(email: String): PasswordResetResult = requestResetResult

    override suspend fun confirmPasswordReset(email: String, otp: String, newPassword: String): PasswordResetResult =
        confirmResetResult

    override suspend fun sendOtp(email: String, subject: String): EmailVerificationResult {
        lastSendOtpEmail = email
        lastSendOtpSubject = subject
        return sendOtpResult
    }

    override suspend fun resendOtp(email: String, subject: String): EmailVerificationResult {
        lastResendOtpEmail = email
        lastResendOtpSubject = subject
        return resendOtpResult
    }

    override suspend fun verifyOtp(email: String, otp: String): EmailVerificationResult {
        lastVerifyOtpEmail = email
        lastVerifyOtpCode = otp
        return verifyOtpResult
    }
}
