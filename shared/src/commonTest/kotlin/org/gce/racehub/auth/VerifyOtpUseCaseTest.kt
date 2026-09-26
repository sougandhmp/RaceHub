package org.gce.racehub.auth

import org.gce.racehub.auth.domain.model.AuthFailure
import org.gce.racehub.auth.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import org.gce.racehub.auth.domain.usecase.VerifyOtpUseCase
import org.gce.racehub.fake.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.core.domain.errorOrNull
import org.gce.racehub.auth.domain.model.authFailure

class VerifyOtpUseCaseTest {

    private val repository = FakeAuthRepository()
    private val useCase = VerifyOtpUseCase(repository)

    @Test
    fun `blank email returns failure`() = runTest {
        val result = useCase("", "1234")
        assertFalse(result is DataResult.Success)
        assertEquals(AuthError.EmailRequired, result.errorOrNull()?.reason)
        assertEquals(null, repository.lastVerifyOtpEmail)
    }

    @Test
    fun `blank otp returns failure`() = runTest {
        val result = useCase("carol@test.com", "   ")
        assertFalse(result is DataResult.Success)
        assertEquals(AuthError.CodeRequired, result.errorOrNull()?.reason)
        assertEquals(null, repository.lastVerifyOtpCode)
    }

    @Test
    fun `email and otp are trimmed before verification`() = runTest {
        useCase("  carol@test.com ", "  1234 ")
        assertEquals("carol@test.com", repository.lastVerifyOtpEmail)
        assertEquals("1234", repository.lastVerifyOtpCode)
    }

    @Test
    fun `valid code succeeds`() = runTest {
        repository.verifyOtpResult = DataResult.Success(Unit)
        assertTrue(useCase("carol@test.com", "1234") is DataResult.Success)
    }

    @Test
    fun `invalid code failure is propagated`() = runTest {
        repository.verifyOtpResult = authFailure(AuthError.Rejected, "Invalid verification code")
        val result = useCase("carol@test.com", "0000")
        assertFalse(result is DataResult.Success)
        assertEquals(AuthFailure(AuthError.Rejected, "Invalid verification code"), result.errorOrNull())
    }
}
