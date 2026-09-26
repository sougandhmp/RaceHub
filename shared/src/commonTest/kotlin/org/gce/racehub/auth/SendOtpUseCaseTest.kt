package org.gce.racehub.auth

import org.gce.racehub.auth.domain.model.AuthFailure
import org.gce.racehub.auth.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import org.gce.racehub.auth.domain.model.EmailVerificationResult
import org.gce.racehub.auth.domain.model.OtpPurpose
import org.gce.racehub.auth.domain.usecase.SendOtpUseCase
import org.gce.racehub.fake.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SendOtpUseCaseTest {

    private val repository = FakeAuthRepository()
    private val useCase = SendOtpUseCase(repository)

    @Test
    fun `blank email returns failure without hitting repository`() = runTest {
        val result = useCase("   ", OtpPurpose.EMAIL_VERIFICATION)
        assertFalse(result.isSuccess)
        assertEquals(AuthError.EmailRequired, result.failure?.reason)
        assertEquals(null, repository.lastSendOtpEmail)
    }

    @Test
    fun `email verification uses the verification subject`() = runTest {
        useCase("alice@test.com", OtpPurpose.EMAIL_VERIFICATION)
        assertEquals("alice@test.com", repository.lastSendOtpEmail)
        assertEquals("Email verification OTP", repository.lastSendOtpSubject)
    }

    @Test
    fun `password reset uses a different subject for the same use case`() = runTest {
        useCase("alice@test.com", OtpPurpose.PASSWORD_RESET)
        assertEquals("Password reset OTP", repository.lastSendOtpSubject)
    }

    @Test
    fun `email is trimmed before being sent`() = runTest {
        useCase("  alice@test.com  ", OtpPurpose.EMAIL_VERIFICATION)
        assertEquals("alice@test.com", repository.lastSendOtpEmail)
    }

    @Test
    fun `successful send is reported`() = runTest {
        repository.sendOtpResult = EmailVerificationResult.success()
        assertTrue(useCase("alice@test.com", OtpPurpose.EMAIL_VERIFICATION).isSuccess)
    }

    @Test
    fun `repository failure is propagated`() = runTest {
        repository.sendOtpResult = EmailVerificationResult.failure(AuthError.Rejected, "Too many requests")
        val result = useCase("alice@test.com", OtpPurpose.EMAIL_VERIFICATION)
        assertFalse(result.isSuccess)
        assertEquals(AuthFailure(AuthError.Rejected, "Too many requests"), result.failure)
    }
}
