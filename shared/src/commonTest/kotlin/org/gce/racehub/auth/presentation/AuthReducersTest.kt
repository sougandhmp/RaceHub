package org.gce.racehub.auth.presentation

import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthFailure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Sign-up and password-reset form rules: edits clear the error, toggles don't, finishing resets. */
class AuthReducersTest {

    private val failure = AuthFailure(AuthError.PasswordsDoNotMatch)

    @Test
    fun `a sign-up edit updates its field and clears the error`() {
        val state = SignUpState(error = failure)
        val edited = SignUpReducer.reduce(state, SignUpMutation.ConfirmPasswordChanged("secret1"))
        assertEquals("secret1", edited.confirmPassword)
        assertNull(edited.error)
    }

    @Test
    fun `toggling password visibility keeps the sign-up error`() {
        val state = SignUpState(error = failure)
        val toggled = SignUpReducer.reduce(state, SignUpMutation.ConfirmPasswordVisibilityToggled)
        assertTrue(toggled.isConfirmPasswordVisible)
        assertEquals(failure, toggled.error)
    }

    @Test
    fun `a successful sign-up resets the form`() {
        val state = SignUpState(username = "ann", password = "secret1", isLoading = true)
        assertEquals(SignUpState(), SignUpReducer.reduce(state, SignUpMutation.Succeeded))
    }

    @Test
    fun `a password-reset edit updates its field and clears the error`() {
        val state = ForgotPasswordState(error = failure)
        val edited = ForgotPasswordReducer.reduce(state, ForgotPasswordMutation.OtpChanged("123456"))
        assertEquals("123456", edited.otp)
        assertNull(edited.error)
    }

    @Test
    fun `toggling password visibility keeps the password-reset error`() {
        val state = ForgotPasswordState(error = failure)
        val toggled = ForgotPasswordReducer.reduce(state, ForgotPasswordMutation.PasswordVisibilityToggled)
        assertTrue(toggled.isPasswordVisible)
        assertEquals(failure, toggled.error)
    }

    @Test
    fun `sending the code moves to the confirm step and completing resets`() {
        val sent = ForgotPasswordReducer.reduce(ForgotPasswordState(email = "a@b.c", isLoading = true), ForgotPasswordMutation.CodeSent)
        assertEquals(ForgotPasswordStep.Confirm, sent.step)
        assertEquals("a@b.c", sent.email)
        assertEquals(ForgotPasswordState(), ForgotPasswordReducer.reduce(sent, ForgotPasswordMutation.ResetCompleted))
    }
}
