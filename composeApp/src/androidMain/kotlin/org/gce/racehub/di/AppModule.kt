package org.gce.racehub.di

import org.gce.racehub.emailverification.EmailVerificationViewModel
import org.gce.racehub.forgotpassword.ForgotPasswordViewModel
import org.gce.racehub.login.LoginViewModel
import org.gce.racehub.signup.SignUpViewModel
import org.gce.racehub.theme.ThemeManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { ThemeManager(androidContext()) }
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignUpViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::EmailVerificationViewModel)
}
