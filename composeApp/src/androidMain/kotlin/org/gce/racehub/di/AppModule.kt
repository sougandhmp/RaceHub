package org.gce.racehub.di

import org.gce.racehub.home.CreateThreadViewModel
import org.gce.racehub.home.HomeViewModel
import org.gce.racehub.login.LoginViewModel
import org.gce.racehub.signup.SignUpViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignUpViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::CreateThreadViewModel)
}
