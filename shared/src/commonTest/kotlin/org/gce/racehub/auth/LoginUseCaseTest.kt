package org.gce.racehub.auth

import org.gce.racehub.auth.domain.model.AuthFailure
import org.gce.racehub.auth.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import org.gce.racehub.auth.domain.usecase.LoginUseCase
import org.gce.racehub.fake.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginUseCaseTest {

    private val repository = FakeAuthRepository()
    private val useCase = LoginUseCase(repository)

    @Test
    fun `blank email returns failure without calling repository`() = runTest {
        val result = useCase("", "password123")
        assertFalse(result.isSuccess)
        assertEquals(AuthError.EmailAndPasswordRequired, result.failure?.reason)
        assertEquals(0, repository.loginCallCount)
    }

    @Test
    fun `blank password returns failure without calling repository`() = runTest {
        val result = useCase("user@test.com", "")
        assertFalse(result.isSuccess)
        assertEquals(AuthError.EmailAndPasswordRequired, result.failure?.reason)
        assertEquals(0, repository.loginCallCount)
    }

    @Test
    fun `email without at-sign returns failure`() = runTest {
        val result = useCase("notanemail", "password123")
        assertFalse(result.isSuccess)
        assertEquals(AuthError.InvalidEmail, result.failure?.reason)
        assertEquals(0, repository.loginCallCount)
    }

    @Test
    fun `password shorter than 6 characters returns failure`() = runTest {
        val result = useCase("user@test.com", "12345")
        assertFalse(result.isSuccess)
        assertEquals(AuthError.PasswordTooShort, result.failure?.reason)
        assertEquals(0, repository.loginCallCount)
    }

    @Test
    fun `password of exactly 6 characters passes validation`() = runTest {
        val result = useCase("user@test.com", "123456")
        assertTrue(result.isSuccess)
        assertEquals(1, repository.loginCallCount)
    }

    @Test
    fun `valid credentials delegate to repository`() = runTest {
        val result = useCase("user@test.com", "password123")
        assertTrue(result.isSuccess)
        assertEquals(1, repository.loginCallCount)
    }

    @Test
    fun `repository failure is propagated`() = runTest {
        repository.loginResult = org.gce.racehub.auth.domain.model.AuthResult.failure(AuthError.Rejected, "Invalid credentials")
        val result = useCase("user@test.com", "password123")
        assertFalse(result.isSuccess)
        assertEquals(AuthFailure(AuthError.Rejected, "Invalid credentials"), result.failure)
    }
}
