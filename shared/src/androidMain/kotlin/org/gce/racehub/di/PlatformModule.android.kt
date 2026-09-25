package org.gce.racehub.di

import org.gce.racehub.auth.data.storage.AndroidSessionStorage
import org.gce.racehub.auth.data.storage.SessionStorage
import org.gce.racehub.db.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory(androidContext()) }
    single<SessionStorage> { AndroidSessionStorage(androidContext()) }
}
