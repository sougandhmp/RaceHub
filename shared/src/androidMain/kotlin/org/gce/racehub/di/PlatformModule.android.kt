package org.gce.racehub.di

import org.gce.racehub.core.data.storage.AndroidSessionStorage
import org.gce.racehub.core.domain.session.SessionStorage
import org.gce.racehub.db.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    single { DatabaseDriverFactory(androidContext()) }
    single<SessionStorage> { AndroidSessionStorage(androidContext()) }
}
