package org.gce.racehub.auth.di

import org.gce.racehub.auth.domain.repository.AuthRepository
import org.gce.racehub.auth.domain.usecase.LoginUseCase
import org.gce.racehub.auth.domain.usecase.LogoutUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Helper class for Swift/iOS to access dependency-injected components.
 *
 * This class provides a bridge between Koin dependency injection and Swift code,
 * allowing Swift ViewModels to access injected dependencies without manual instantiation.
 */
class AuthDependencyProvider : KoinComponent {

    val authRepository: AuthRepository by inject()
    val logoutUseCase: LogoutUseCase by inject()

    fun createLoginUseCase(): LoginUseCase = LoginUseCase(authRepository)

    companion object {
        val shared = AuthDependencyProvider()
    }
}
