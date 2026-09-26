package org.gce.racehub.auth

import org.gce.racehub.auth.domain.model.AuthFailure
import org.gce.racehub.auth.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import org.gce.racehub.auth.domain.model.EmailVerificationResult
import org.gce.racehub.auth.domain.usecase.VerifyOtpUseCase
import org.gce.racehub.fake.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VerifyOtpUseCaseTest {

    private val repository = FakeAuthRepository()
    private val useCase = VerifyOtpUseCase(repository)

    @Test
    fun `blank email returns failure`() = runTest {
        val result = useCase("", "1234")
        assertFalse(result.isSuccess)
        assertEquals(AuthError.EmailRequired, result.failure?.reason)
        assertEquals(null, repository.lastVerifyOtpEmail)
    }

    @Test
    fun `blank otp returns failure`() = runTest {
        val result = useCase("carol@test.com", "   ")
        assertFalse(result.isSuccess)
        assertEquals(AuthError.CodeRequired, result.failure?.reason)
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
        repository.verifyOtpResult = EmailVerificationResult.success()
        assertTrue(useCase("carol@test.com", "1234").isSuccess)
    }

    @Test
    fun `invalid code failure is propagated`() = runTest {
        repository.verifyOtpResult = EmailVerificationResult.failure(AuthError.Rejected, "Invalid verification code")
        val result = useCase("carol@test.com", "0000")
        assertFalse(result.isSuccess)
        assertEquals(AuthFailure(AuthError.Rejected, "Invalid verification code"), result.failure)
    }
}
