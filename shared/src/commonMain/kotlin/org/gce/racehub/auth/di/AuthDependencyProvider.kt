package org.gce.racehub.auth.di

import org.gce.racehub.auth.domain.repository.AuthRepository
import org.gce.racehub.auth.domain.usecase.LoginUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Helper class for Swift/iOS to access dependency-injected components.
 *
 * This class provides a bridge between Koin dependency injection and Swift code,
 * allowing Swift ViewModels to access injected dependencies without manual instantiation.
 */
class AuthDependencyProvider : KoinComponent {

    /**
     * Gets the injected AuthRepository instance.
     * This will be either the network or fake implementation based on Koin configuration.
     */
    val authRepository: AuthRepository by inject()

    /**
     * Creates a LoginUseCase with the injected AuthRepository.
     * This is a convenience method for Swift code that needs a LoginUseCase instance.
     */
    fun createLoginUseCase(): LoginUseCase {
        return LoginUseCase(authRepository)
    }

    companion object {
        /**
         * Shared instance for easy access from Swift.
         * Note: In a real app, you might want to use proper singleton pattern or injection.
         */
        val shared = AuthDependencyProvider()
    }
}
