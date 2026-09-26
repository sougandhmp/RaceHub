package org.gce.racehub.di

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import org.koin.core.module.Module

/**
 * Platform-agnostic Koin initialization.
 *
 * Use this to initialize dependency injection in your app.
 * The actual implementation will be platform-specific.
 */
expect object KoinInitializer {

    /**
     * Initializes Koin with production configuration.
     *
     * @param baseUrl The API base URL, set per build configuration by each app
     * @param logNetwork Log requests to the console; pass true only for debug builds
     * @param additionalModules Additional Koin modules to include
     */
    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    fun init(baseUrl: String, logNetwork: Boolean, vararg additionalModules: Module)
}
