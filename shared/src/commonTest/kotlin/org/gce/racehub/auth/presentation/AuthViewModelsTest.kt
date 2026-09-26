package org.gce.racehub.auth.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.model.EmailVerificationResult
import org.gce.racehub.auth.domain.model.PasswordResetResult
import org.gce.racehub.auth.domain.model.User
import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.auth.domain.usecase.ConfirmPasswordResetUseCase
import org.gce.racehub.auth.domain.usecase.LoginUseCase
import org.gce.racehub.auth.domain.usecase.RequestPasswordResetUseCase
import org.gce.racehub.auth.domain.usecase.ResendOtpUseCase
import org.gce.racehub.auth.domain.usecase.SendOtpUseCase
import org.gce.racehub.auth.domain.usecase.SignUpUseCase
import org.gce.racehub.auth.domain.usecase.VerifyOtpUseCase
import org.gce.racehub.fake.FakeAuthRepository
import org.gce.racehub.fake.FakeSessionStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Shared auth ViewModels: the auth flow rules both platforms now follow. */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = FakeAuthRepository()
    private val session = UserSession(FakeSessionStorage())
    private val verified = User("1", "ann@racehub.com", "Ann", token = "tok", isEmailVerified = true)

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun loginVm() = LoginViewModel(LoginUseCase(repo), session, SendOtpUseCase(repo))

    private fun LoginViewModel.fillAndSubmit() {
        onIntent(LoginIntent.EmailChanged("ann@racehub.com"))
        onIntent(LoginIntent.PasswordChanged("secret1"))
        onIntent(LoginIntent.Submit)
    }

    // ── Login ───────────────────────────────────────────────────────────────

    @Test
    fun `verified login starts a session and clears the form`() = runTest(dispatcher) {
        repo.loginResult = AuthResult.success(verified)
        val vm = loginVm()
        vm.fillAndSubmit()
        advanceUntilIdle()
        assertEquals(verified, session.currentUser.value)
        assertEquals(LoginEffect.NavigateToHome, vm.effects.first())
        assertEquals(LoginState(), vm.state.value)
    }

    @Test
    fun `unverified login sends a code and never starts a session`() = runTest(dispatcher) {
        repo.loginResult = AuthResult.success(verified.copy(isEmailVerified = false))
        val vm = loginVm()
        vm.fillAndSubmit()
        advanceUntilIdle()
        assertNull(session.currentUser.value)
        assertEquals("ann@racehub.com", repo.lastSendOtpEmail)
        assertEquals(LoginEffect.NavigateToEmailVerification("ann@racehub.com"), vm.effects.first())
    }

    @Test
    fun `failed login shows the error inline until the next edit`() = runTest(dispatcher) {
        repo.loginResult = AuthResult.failure("Invalid credentials")
        val vm = loginVm()
        vm.fillAndSubmit()
        advanceUntilIdle()
        assertEquals("Invalid credentials", vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)

        vm.onIntent(LoginIntent.PasswordChanged("secret2"))
        assertNull(vm.state.value.errorMessage)
    }

    // ── Sign up ─────────────────────────────────────────────────────────────

    private fun SignUpViewModel.fillAndSubmit() {
        onIntent(SignUpIntent.UsernameChanged("ann"))
        onIntent(SignUpIntent.EmailChanged("ann@racehub.com"))
        onIntent(SignUpIntent.PasswordChanged("secret1"))
        onIntent(SignUpIntent.ConfirmPasswordChanged("secret1"))
        onIntent(SignUpIntent.CountryChanged("AU"))
        onIntent(SignUpIntent.Submit)
    }

    @Test
    fun `sign up sends a code and goes to verification without signing in`() = runTest(dispatcher) {
        val vm = SignUpViewModel(SignUpUseCase(repo), SendOtpUseCase(repo))
        vm.fillAndSubmit()
        advanceUntilIdle()
        assertNull(session.currentUser.value)
        assertEquals("ann@racehub.com", repo.lastSendOtpEmail)
        assertEquals(SignUpEffect.NavigateToEmailVerification("ann@racehub.com"), vm.effects.first())
        assertEquals(SignUpState(), vm.state.value)
    }

    @Test
    fun `sign up keeps the form when the code cannot be sent`() = runTest(dispatcher) {
        repo.sendOtpResult = EmailVerificationResult.failure("Mail server down")
        val vm = SignUpViewModel(SignUpUseCase(repo), SendOtpUseCase(repo))
        vm.fillAndSubmit()
        advanceUntilIdle()
        assertEquals("Mail server down", vm.state.value.errorMessage)
        assertEquals("ann", vm.state.value.username)
    }

    // ── Forgot password ─────────────────────────────────────────────────────

    @Test
    fun `reset moves to the confirm step then completes`() = runTest(dispatcher) {
        val vm = ForgotPasswordViewModel(RequestPasswordResetUseCase(repo), ConfirmPasswordResetUseCase(repo))
        vm.onIntent(ForgotPasswordIntent.EmailChanged("ann@racehub.com"))
        vm.onIntent(ForgotPasswordIntent.RequestReset)
        advanceUntilIdle()
        assertEquals(ForgotPasswordStep.Confirm, vm.state.value.step)

        vm.onIntent(ForgotPasswordIntent.OtpChanged("123456"))
        vm.onIntent(ForgotPasswordIntent.NewPasswordChanged("secret9"))
        vm.onIntent(ForgotPasswordIntent.ConfirmPasswordChanged("secret9"))
        vm.onIntent(ForgotPasswordIntent.ConfirmReset)
        advanceUntilIdle()
        assertEquals(ForgotPasswordEffect.PasswordResetSuccess, vm.effects.first())
        assertEquals(ForgotPasswordState(), vm.state.value)
    }

    @Test
    fun `failed reset request stays on the request step with the error`() = runTest(dispatcher) {
        repo.requestResetResult = PasswordResetResult.failure("No account for that email")
        val vm = ForgotPasswordViewModel(RequestPasswordResetUseCase(repo), ConfirmPasswordResetUseCase(repo))
        vm.onIntent(ForgotPasswordIntent.EmailChanged("x@racehub.com"))
        vm.onIntent(ForgotPasswordIntent.RequestReset)
        advanceUntilIdle()
        assertEquals(ForgotPasswordStep.Request, vm.state.value.step)
        assertEquals("No account for that email", vm.state.value.errorMessage)
    }

    // ── Email verification ──────────────────────────────────────────────────

    private fun verificationVm() = EmailVerificationViewModel(VerifyOtpUseCase(repo), ResendOtpUseCase(repo))

    @Test
    fun `opening for a different email resets the screen`() {
        val vm = verificationVm()
        vm.onIntent(EmailVerificationIntent.Open("a@racehub.com"))
        vm.onIntent(EmailVerificationIntent.OtpChanged("111"))
        vm.onIntent(EmailVerificationIntent.Open("b@racehub.com"))
        assertEquals(EmailVerificationState(email = "b@racehub.com"), vm.state.value)
    }

    @Test
    fun `correct code verifies`() = runTest(dispatcher) {
        val vm = verificationVm()
        vm.onIntent(EmailVerificationIntent.Open("ann@racehub.com"))
        vm.onIntent(EmailVerificationIntent.OtpChanged("123456"))
        vm.onIntent(EmailVerificationIntent.Verify)
        advanceUntilIdle()
        assertEquals("123456", repo.lastVerifyOtpCode)
        assertEquals(EmailVerificationEffect.EmailVerified, vm.effects.first())
    }

    @Test
    fun `wrong code shows the error and resend confirms`() = runTest(dispatcher) {
        repo.verifyOtpResult = EmailVerificationResult.failure("Invalid verification code")
        val vm = verificationVm()
        vm.onIntent(EmailVerificationIntent.Open("ann@racehub.com"))
        vm.onIntent(EmailVerificationIntent.OtpChanged("000000"))
        vm.onIntent(EmailVerificationIntent.Verify)
        advanceUntilIdle()
        assertEquals("Invalid verification code", vm.state.value.errorMessage)

        vm.onIntent(EmailVerificationIntent.ResendCode)
        advanceUntilIdle()
        assertTrue(vm.state.value.codeResent)
        assertNull(vm.state.value.errorMessage)
        assertEquals("ann@racehub.com", repo.lastResendOtpEmail)
    }
}
