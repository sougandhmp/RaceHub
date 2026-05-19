package org.gce.racehub.fake

import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.model.PasswordResetResult
import org.gce.racehub.auth.domain.model.User
import org.gce.racehub.auth.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var loginResult: AuthResult = AuthResult.success(User("1", "test@test.com", "Test User", token = "tok"))
    var signUpResult: AuthResult = AuthResult.success(User("1", "test@test.com", "Test User", token = "tok"))
    var logoutResult: Boolean = true

    var loginCallCount = 0
    var logoutCallCount = 0
    var lastLogoutToken: String? = null

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

    override suspend fun requestPasswordReset(email: String): PasswordResetResult = PasswordResetResult.success()

    override suspend fun confirmPasswordReset(email: String, otp: String, newPassword: String): PasswordResetResult =
        PasswordResetResult.success()
}
