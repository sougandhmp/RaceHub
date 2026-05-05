package org.gce.racehub.di

import android.app.Application
import org.gce.racehub.auth.di.createProductionAuthModule
import org.gce.racehub.race.di.raceModule
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
            modules(createProductionAuthModule(baseUrl), raceModule, *additionalModules)
        }
    }

    /**
     * Starts the Koin container with the fake in-memory auth module.
     * Useful for Espresso tests or local dev builds without a backend.
     */
    actual fun initForTesting(vararg additionalModules: Module) {
        startKoin {
            androidApplication?.let { androidContext(it) }
            modules(org.gce.racehub.auth.di.fakeAuthModule, raceModule, *additionalModules)
        }
    }
}
