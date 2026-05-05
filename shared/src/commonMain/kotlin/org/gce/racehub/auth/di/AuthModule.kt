package org.gce.racehub.auth.di

import org.gce.racehub.auth.data.network.AuthService
import org.gce.racehub.auth.data.network.HttpClientFactory
import org.gce.racehub.auth.data.repository.AuthRepositoryImpl
import org.gce.racehub.auth.data.repository.AuthRepositoryNetworkImpl
import org.gce.racehub.auth.domain.repository.AuthRepository
import org.gce.racehub.auth.domain.usecase.LoginUseCase
import org.gce.racehub.auth.domain.usecase.SignUpUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin dependency injection module for authentication.
 *
 * Provides different repository implementations based on configuration.
 * Use [createAuthModule] to create the module with your desired configuration.
 */

/**
 * Creates a Koin module for authentication dependencies.
 *
 * @param baseUrl The API base URL for network calls
 * @param useFakeRepository Whether to use fake repository for testing (default: false)
 * @return Koin module with authentication dependencies
 */
fun createAuthModule(baseUrl: String, useFakeRepository: Boolean = false): Module = module {

    // HTTP Client - platform-specific engine will be selected automatically
    single {
        HttpClientFactory.create(baseUrl)
    }

    // Auth Service - depends on HTTP client
    single {
        AuthService(get(), baseUrl)
    }

    // Repository - choose implementation based on configuration
    single<AuthRepository> {
        if (useFakeRepository) {
            AuthRepositoryImpl()
        } else {
            AuthRepositoryNetworkImpl(get())
        }
    }

    factory { LoginUseCase(get()) }
    factory { SignUpUseCase(get()) }
}

/**
 * Convenience function to create a production auth module.
 *
 * @param baseUrl The API base URL
 * @return Koin module configured for production
 */
fun createProductionAuthModule(baseUrl: String): Module =
    createAuthModule(baseUrl, useFakeRepository = false)

/**
 * Convenience function to create a test/development auth module.
 *
 * @param baseUrl The API base URL (ignored when using fake repository)
 * @return Koin module configured for testing
 */
fun createTestAuthModule(baseUrl: String = "https://test.example.com"): Module =
    createAuthModule(baseUrl, useFakeRepository = true)

/**
 * Pre-configured module for development with fake data.
 * Useful for UI development and testing without network dependencies.
 */
val fakeAuthModule = createTestAuthModule()

/**
 * Example of how to create a module with custom configuration.
 * Uncomment and modify as needed:
 */
/*
// Custom auth module with specific configuration
val customAuthModule = module {
    // Custom HTTP client configuration
    single(named("custom")) {
        HttpClientFactory.create("https://custom-api.example.com").apply {
            // Add custom configuration here
        }
    }

    // Custom auth service
    single(named("custom")) {
        AuthService(get(named("custom")), "https://custom-api.example.com")
    }

    // Custom repository
    single<AuthRepository>(named("custom")) {
        AuthRepositoryNetworkImpl(get(named("custom")))
    }
}
*/
