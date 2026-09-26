package org.gce.racehub.auth

import org.gce.racehub.auth.domain.model.AuthFailure
import org.gce.racehub.auth.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.usecase.SignUpUseCase
import org.gce.racehub.fake.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SignUpUseCaseTest {

    private val repository = FakeAuthRepository()
    private val useCase = SignUpUseCase(repository)

    private suspend fun invoke(
        username: String = "RaceFan",
        email: String = "alice@test.com",
        password: String = "secret1",
        country: String = "AU",
        confirmPassword: String = "secret1"
    ) = useCase(username, email, password, country, confirmPassword)

    @Test
    fun `blank username returns failure`() = runTest {
        val result = invoke(username = "")
        assertFalse(result.isSuccess)
        assertEquals(AuthError.UsernameRequired, result.failure?.reason)
    }

    @Test
    fun `blank email returns failure`() = runTest {
        val result = invoke(email = "")
        assertFalse(result.isSuccess)
        assertEquals(AuthError.EmailRequired, result.failure?.reason)
    }

    @Test
    fun `email without at-sign returns failure`() = runTest {
        val result = invoke(email = "notanemail")
        assertFalse(result.isSuccess)
        assertEquals(AuthError.InvalidEmail, result.failure?.reason)
    }

    @Test
    fun `short password returns failure before checking mismatch`() = runTest {
        val result = invoke(password = "12345", confirmPassword = "different")
        assertFalse(result.isSuccess)
        assertEquals(AuthError.PasswordTooShort, result.failure?.reason)
    }

    @Test
    fun `mismatched passwords returns failure`() = runTest {
        val result = invoke(password = "secret1", confirmPassword = "secret2")
        assertFalse(result.isSuccess)
        assertEquals(AuthError.PasswordsDoNotMatch, result.failure?.reason)
    }

    @Test
    fun `blank country returns failure`() = runTest {
        val result = invoke(country = "")
        assertFalse(result.isSuccess)
        assertEquals(AuthError.CountryRequired, result.failure?.reason)
    }

    @Test
    fun `valid input delegates to repository`() = runTest {
        val result = invoke()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `repository failure is propagated`() = runTest {
        repository.signUpResult = AuthResult.failure(AuthError.Rejected, "Email already in use")
        val result = invoke()
        assertFalse(result.isSuccess)
        assertEquals(AuthFailure(AuthError.Rejected, "Email already in use"), result.failure)
    }
}
