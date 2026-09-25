package org.gce.racehub.di

import org.gce.racehub.auth.data.storage.IosSessionStorage
import org.gce.racehub.auth.data.storage.SessionStorage
import org.gce.racehub.db.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
    single<SessionStorage> { IosSessionStorage() }
}
