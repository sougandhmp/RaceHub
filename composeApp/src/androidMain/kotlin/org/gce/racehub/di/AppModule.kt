package org.gce.racehub.di

import org.gce.racehub.home.HomeViewModel
import org.gce.racehub.login.LoginViewModel
import org.gce.racehub.signup.SignUpViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { SignUpViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
}
