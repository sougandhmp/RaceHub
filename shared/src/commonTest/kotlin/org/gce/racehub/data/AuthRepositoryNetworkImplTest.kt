package org.gce.racehub.data

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.gce.racehub.auth.data.network.AuthService
import org.gce.racehub.auth.data.repository.AuthRepositoryNetworkImpl
import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthFailure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.core.domain.errorOrNull
import org.gce.racehub.core.domain.dataOrNull

/**
 * The auth repository is the auth error boundary: a server "no" keeps the
 * server's message as [AuthError.Rejected]; transport failures become typed
 * reasons and never leak raw exception text to the UI.
 */
class AuthRepositoryNetworkImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val api = FakeApi(dispatcher)
    private val repo = AuthRepositoryNetworkImpl(AuthService(api.client, "https://api.test"))

    private fun test(block: suspend () -> Unit) = runTest(dispatcher) { block() }

    private val login = "/api/v1/auth/login"

    @Test
    fun `successful login maps the user and token`() = test {
        api.responses[login] = """
            {"success":true,"message":"ok","data":{"token":"tok","user":{"id":"u1","username":"ann",
             "email":"ann@racehub.com","country":"AU","avatar":"A","role":"member","joinedAt":"2026-04-01",
             "postsCount":2,"emailVerified":false}}}
        """.trimIndent()
        val user = repo.login("ann@racehub.com", "secret1").dataOrNull()!!
        assertEquals("tok", user.token)
        assertEquals("ann", user.username)
        assertFalse(user.isEmailVerified)
    }

    @Test
    fun `a server refusal keeps the server message`() = test {
        api.responses[login] = """{"success":false,"message":"Invalid credentials"}"""
        assertEquals(AuthFailure(AuthError.Rejected, "Invalid credentials"), repo.login("a@b.c", "secret1").errorOrNull())
    }

    @Test
    fun `offline login is a Network failure without raw exception text`() = test {
        api.failures[login] = FakeApi.Failure.Offline
        assertEquals(AuthFailure(AuthError.Network), repo.login("a@b.c", "secret1").errorOrNull())
    }

    @Test
    fun `an HTTP 500 without a JSON body is a Server failure`() = test {
        api.failures[login] = FakeApi.Failure.ServerError
        assertEquals(AuthFailure(AuthError.Server), repo.login("a@b.c", "secret1").errorOrNull())
    }

    @Test
    fun `sign up refusal keeps the server message`() = test {
        api.responses["/api/v1/auth/signup"] = """{"success":false,"message":"Email already in use"}"""
        assertEquals(
            AuthFailure(AuthError.Rejected, "Email already in use"),
            repo.signUp("ann", "ann@racehub.com", "secret1", "AU").errorOrNull()
        )
    }

    @Test
    fun `logout is best effort and false when offline`() = test {
        api.failures["/api/v1/auth/logout"] = FakeApi.Failure.Offline
        assertFalse(repo.logout("tok"))
    }

    @Test
    fun `verification code refusal and resend offline are typed`() = test {
        api.responses["/api/v1/auth/otp/verify"] = """{"success":false,"message":"Invalid verification code"}"""
        assertEquals(AuthFailure(AuthError.Rejected, "Invalid verification code"), repo.verifyOtp("a@b.c", "000000").errorOrNull())

        api.failures["/api/v1/auth/otp/resend"] = FakeApi.Failure.Offline
        assertEquals(AuthFailure(AuthError.Network), repo.resendOtp("a@b.c", "subject").errorOrNull())
    }

    @Test
    fun `password reset confirm succeeds`() = test {
        api.responses["/api/v1/auth/password-reset/confirm"] = """{"success":true,"message":"Password updated"}"""
        val result = repo.confirmPasswordReset("a@b.c", "123456", "secret9")
        assertTrue(result is DataResult.Success)
        assertNull(result.errorOrNull())
    }
}
