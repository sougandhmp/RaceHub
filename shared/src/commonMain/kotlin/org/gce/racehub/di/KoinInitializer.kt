package org.gce.racehub.di

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
     * @param baseUrl The API base URL for authentication
     * @param additionalModules Additional Koin modules to include
     */
    fun init(baseUrl: String, vararg additionalModules: Module)

    /**
     * Initializes Koin with test configuration (fake repository).
     *
     * @param additionalModules Additional Koin modules to include
     */
    fun initForTesting(vararg additionalModules: Module)
}
