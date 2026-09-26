package org.gce.racehub.auth

import org.gce.racehub.auth.domain.model.AuthFailure
import org.gce.racehub.auth.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import org.gce.racehub.auth.domain.usecase.SignUpUseCase
import org.gce.racehub.fake.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.core.domain.errorOrNull
import org.gce.racehub.auth.domain.model.authFailure

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
        assertFalse(result is DataResult.Success)
        assertEquals(AuthError.UsernameRequired, result.errorOrNull()?.reason)
    }

    @Test
    fun `blank email returns failure`() = runTest {
        val result = invoke(email = "")
        assertFalse(result is DataResult.Success)
        assertEquals(AuthError.EmailRequired, result.errorOrNull()?.reason)
    }

    @Test
    fun `email without at-sign returns failure`() = runTest {
        val result = invoke(email = "notanemail")
        assertFalse(result is DataResult.Success)
        assertEquals(AuthError.InvalidEmail, result.errorOrNull()?.reason)
    }

    @Test
    fun `short password returns failure before checking mismatch`() = runTest {
        val result = invoke(password = "12345", confirmPassword = "different")
        assertFalse(result is DataResult.Success)
        assertEquals(AuthError.PasswordTooShort, result.errorOrNull()?.reason)
    }

    @Test
    fun `mismatched passwords returns failure`() = runTest {
        val result = invoke(password = "secret1", confirmPassword = "secret2")
        assertFalse(result is DataResult.Success)
        assertEquals(AuthError.PasswordsDoNotMatch, result.errorOrNull()?.reason)
    }

    @Test
    fun `blank country returns failure`() = runTest {
        val result = invoke(country = "")
        assertFalse(result is DataResult.Success)
        assertEquals(AuthError.CountryRequired, result.errorOrNull()?.reason)
    }

    @Test
    fun `valid input delegates to repository`() = runTest {
        val result = invoke()
        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `repository failure is propagated`() = runTest {
        repository.signUpResult = authFailure(AuthError.Rejected, "Email already in use")
        val result = invoke()
        assertFalse(result is DataResult.Success)
        assertEquals(AuthFailure(AuthError.Rejected, "Email already in use"), result.errorOrNull())
    }
}
