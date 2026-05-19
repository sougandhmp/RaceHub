package org.gce.racehub.auth

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
        name: String = "Alice",
        email: String = "alice@test.com",
        password: String = "secret1",
        confirmPassword: String = "secret1"
    ) = useCase(name, email, password, confirmPassword)

    @Test
    fun `blank name returns failure`() = runTest {
        val result = invoke(name = "")
        assertFalse(result.isSuccess)
        assertEquals("Name cannot be empty", result.error)
    }

    @Test
    fun `blank email returns failure`() = runTest {
        val result = invoke(email = "")
        assertFalse(result.isSuccess)
        assertEquals("Email cannot be empty", result.error)
    }

    @Test
    fun `email without at-sign returns failure`() = runTest {
        val result = invoke(email = "notanemail")
        assertFalse(result.isSuccess)
        assertEquals("Invalid email format", result.error)
    }

    @Test
    fun `short password returns failure before checking mismatch`() = runTest {
        val result = invoke(password = "12345", confirmPassword = "different")
        assertFalse(result.isSuccess)
        assertEquals("Password must be at least 6 characters", result.error)
    }

    @Test
    fun `mismatched passwords returns failure`() = runTest {
        val result = invoke(password = "secret1", confirmPassword = "secret2")
        assertFalse(result.isSuccess)
        assertEquals("Passwords do not match", result.error)
    }

    @Test
    fun `valid input delegates to repository`() = runTest {
        val result = invoke()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `repository failure is propagated`() = runTest {
        repository.signUpResult = AuthResult.failure("Email already in use")
        val result = invoke()
        assertFalse(result.isSuccess)
        assertEquals("Email already in use", result.error)
    }
}
