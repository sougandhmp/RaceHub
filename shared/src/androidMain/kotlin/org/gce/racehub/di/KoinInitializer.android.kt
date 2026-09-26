package org.gce.racehub.di

import org.gce.racehub.core.di.coreModule
import org.gce.racehub.forum.di.createForumModule
import org.gce.racehub.profile.di.createProfileModule
import android.app.Application
import org.gce.racehub.auth.di.createProductionAuthModule
import org.gce.racehub.race.di.createRaceModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/** Stored by [KoinInitializer.setApplication] before [KoinInitializer.init] is called. */
private var androidApplication: Application? = null

/**
 * Android-specific Koin initialization.
 *
 * Call [setApplication] then [init] from [RaceHubApplication.onCreate].
 * [setApplication] is not part of the `expect` interface — it is an
 * Android-only extension of the actual object that allows the Application
 * context to be supplied without changing the shared `expect` signature.
 */
actual object KoinInitializer {

    /**
     * Stores the [Application] instance so [init] can supply it to
     * `startKoin { androidContext(...) }`.
     *
     * Must be called before [init].
     */
    fun setApplication(application: Application) {
        androidApplication = application
    }

    /** Starts the Koin container with the production auth module. */
    actual fun init(baseUrl: String, vararg additionalModules: Module) {
        startKoin {
            androidApplication?.let { androidContext(it) }
            modules(coreModule, createProductionAuthModule(baseUrl), createRaceModule(baseUrl), createForumModule(baseUrl), createProfileModule(baseUrl), platformModule, presentationModule, *additionalModules)
        }
    }

}
