package org.gce.racehub.auth

import kotlinx.coroutines.test.runTest
import org.gce.racehub.auth.domain.model.EmailVerificationResult
import org.gce.racehub.auth.domain.model.OtpPurpose
import org.gce.racehub.auth.domain.usecase.ResendOtpUseCase
import org.gce.racehub.fake.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResendOtpUseCaseTest {

    private val repository = FakeAuthRepository()
    private val useCase = ResendOtpUseCase(repository)

    @Test
    fun `blank email returns failure without hitting repository`() = runTest {
        val result = useCase("", OtpPurpose.EMAIL_VERIFICATION)
        assertFalse(result.isSuccess)
        assertEquals("Email cannot be empty", result.error)
        assertEquals(null, repository.lastResendOtpEmail)
    }

    @Test
    fun `resend forwards email and verification subject`() = runTest {
        useCase("  bob@test.com ", OtpPurpose.EMAIL_VERIFICATION)
        assertEquals("bob@test.com", repository.lastResendOtpEmail)
        assertEquals("Email verification OTP", repository.lastResendOtpSubject)
    }

    @Test
    fun `successful resend is reported`() = runTest {
        repository.resendOtpResult = EmailVerificationResult.success()
        assertTrue(useCase("bob@test.com", OtpPurpose.EMAIL_VERIFICATION).isSuccess)
    }

    @Test
    fun `repository failure is propagated`() = runTest {
        repository.resendOtpResult = EmailVerificationResult.failure("Please wait before retrying")
        val result = useCase("bob@test.com", OtpPurpose.EMAIL_VERIFICATION)
        assertFalse(result.isSuccess)
        assertEquals("Please wait before retrying", result.error)
    }
}
