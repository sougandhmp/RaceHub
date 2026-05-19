package org.gce.racehub.forgotpassword

sealed class ForgotPasswordEffect {
    data object PasswordResetSuccess : ForgotPasswordEffect()
}
