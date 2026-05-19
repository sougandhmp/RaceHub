package org.gce.racehub.auth.di

import org.gce.racehub.auth.domain.repository.AuthRepository
import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.auth.domain.usecase.ConfirmPasswordResetUseCase
import org.gce.racehub.auth.domain.usecase.LoginUseCase
import org.gce.racehub.auth.domain.usecase.LogoutUseCase
import org.gce.racehub.auth.domain.usecase.RequestPasswordResetUseCase
import org.gce.racehub.auth.domain.usecase.SignUpUseCase
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
    val userSession: UserSession by inject()

    fun createLoginUseCase(): LoginUseCase = LoginUseCase(authRepository)
    fun createSignUpUseCase(): SignUpUseCase = SignUpUseCase(authRepository)
    fun createRequestPasswordResetUseCase(): RequestPasswordResetUseCase = RequestPasswordResetUseCase(authRepository)
    fun createConfirmPasswordResetUseCase(): ConfirmPasswordResetUseCase = ConfirmPasswordResetUseCase(authRepository)

    companion object {
        val shared = AuthDependencyProvider()
    }
}
