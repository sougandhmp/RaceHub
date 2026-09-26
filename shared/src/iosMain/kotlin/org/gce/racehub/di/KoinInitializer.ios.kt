package org.gce.racehub.di

import org.gce.racehub.forum.di.createForumModule
import org.gce.racehub.profile.di.createProfileModule
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import org.gce.racehub.auth.di.createProductionAuthModule
import org.gce.racehub.race.di.createRaceModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 * iOS-specific Koin initialization.
 *
 * Call this from your iOS app delegate or main function to set up dependency injection.
 */
actual object KoinInitializer {

    /**
     * Initializes Koin with production configuration.
     *
     * @param baseUrl The API base URL for authentication
     * @param additionalModules Additional Koin modules to include
     */
    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    actual fun init(baseUrl: String, vararg additionalModules: Module) {
        startKoin {
            modules(
                createProductionAuthModule(baseUrl),
                createRaceModule(baseUrl), createForumModule(baseUrl), createProfileModule(baseUrl),
                platformModule, presentationModule,
                *additionalModules
            )
        }
    }

    // Swift-friendly overload: `vararg` doesn't bridge cleanly from Swift,
    // so iOS callers use this instead of `init(baseUrl:additionalModules:)`.
    fun start(baseUrl: String) {
        init(baseUrl = baseUrl)
    }
}
