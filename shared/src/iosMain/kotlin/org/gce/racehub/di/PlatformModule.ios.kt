package org.gce.racehub.di

import org.gce.racehub.auth.data.storage.IosSessionStorage
import org.gce.racehub.auth.domain.session.SessionStorage
import org.gce.racehub.db.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
    single<SessionStorage> { IosSessionStorage() }
}
