package org.gce.racehub.di

import org.gce.racehub.auth.presentation.EmailVerificationViewModel
import org.gce.racehub.auth.presentation.ForgotPasswordViewModel
import org.gce.racehub.auth.presentation.LoginViewModel
import org.gce.racehub.auth.presentation.SignUpViewModel
import org.gce.racehub.forum.presentation.CreateThreadViewModel
import org.gce.racehub.forum.presentation.ForumViewModel
import org.gce.racehub.forum.presentation.ThreadDetailViewModel
import org.gce.racehub.home.presentation.HomeViewModel
import org.gce.racehub.profile.presentation.ProfileViewModel
import org.gce.racehub.race.presentation.RaceViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Shared ViewModels, resolved by `koinViewModel()` on Android and [SharedViewModels] on iOS. */
internal val presentationModule = module {
    viewModel { RaceViewModel(get(), get(), get(), get(), get()) }
    viewModelOf(::ForumViewModel)
    viewModelOf(::ThreadDetailViewModel)
    viewModelOf(::CreateThreadViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignUpViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::EmailVerificationViewModel)
}
