package org.gce.racehub.auth

import org.gce.racehub.auth.domain.model.AuthFailure
import org.gce.racehub.auth.domain.model.AuthError
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
        assertEquals(AuthError.EmailRequired, result.failure?.reason)
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
        repository.resendOtpResult = EmailVerificationResult.failure(AuthError.Rejected, "Please wait before retrying")
        val result = useCase("bob@test.com", OtpPurpose.EMAIL_VERIFICATION)
        assertFalse(result.isSuccess)
        assertEquals(AuthFailure(AuthError.Rejected, "Please wait before retrying"), result.failure)
    }
}
