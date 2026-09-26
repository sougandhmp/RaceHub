package org.gce.racehub.di

import org.gce.racehub.theme.ThemeManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { ThemeManager(androidContext()) }
}
