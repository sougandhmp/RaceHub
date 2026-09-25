package org.gce.racehub.di

import org.gce.racehub.emailverification.EmailVerificationViewModel
import org.gce.racehub.forgotpassword.ForgotPasswordViewModel
import org.gce.racehub.forum.ForumViewModel
import org.gce.racehub.forum.ThreadDetailViewModel
import org.gce.racehub.home.CreateThreadViewModel
import org.gce.racehub.home.HomeViewModel
import org.gce.racehub.login.LoginViewModel
import org.gce.racehub.profile.ProfileViewModel
import org.gce.racehub.race.RaceViewModel
import org.gce.racehub.signup.SignUpViewModel
import org.gce.racehub.theme.ThemeManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.gce.racehub.home.ScheduleViewModel
import org.gce.racehub.home.StandingsViewModel
import org.gce.racehub.race.RaceDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { ThemeManager(androidContext()) }
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignUpViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::EmailVerificationViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::RaceViewModel)
    viewModelOf(::ForumViewModel)
    viewModelOf(::ThreadDetailViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::CreateThreadViewModel)
    viewModelOf(::ScheduleViewModel)
    viewModelOf(::StandingsViewModel)
    // The race slug comes from the navigation key: koinViewModel { parametersOf(slug) }
    viewModel { (slug: String) -> RaceDetailViewModel(slug, get()) }
}
