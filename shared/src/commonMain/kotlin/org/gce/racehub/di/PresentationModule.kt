package org.gce.racehub.di

import org.gce.racehub.forum.presentation.CreateThreadViewModel
import org.gce.racehub.forum.presentation.ForumViewModel
import org.gce.racehub.forum.presentation.ThreadDetailViewModel
import org.gce.racehub.race.presentation.RaceViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Shared ViewModels, resolved by `koinViewModel()` on Android and [SharedViewModels] on iOS. */
val presentationModule = module {
    // Explicit factory: RaceViewModel's timeZone parameter keeps its default.
    viewModel { RaceViewModel(get(), get(), get(), get(), get()) }
    viewModelOf(::ForumViewModel)
    viewModelOf(::ThreadDetailViewModel)
    viewModelOf(::CreateThreadViewModel)
}
