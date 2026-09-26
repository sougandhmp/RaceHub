package org.gce.racehub.di

import org.gce.racehub.race.presentation.RaceViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Shared ViewModels, resolved by `koinViewModel()` on Android and [SharedViewModels] on iOS. */
val presentationModule = module {
    viewModelOf(::RaceViewModel)
}
