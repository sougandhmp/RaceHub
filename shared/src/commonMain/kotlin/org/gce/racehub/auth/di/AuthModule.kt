package org.gce.racehub.auth.di

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import org.gce.racehub.auth.data.network.AuthService
import org.gce.racehub.auth.data.repository.AuthRepositoryNetworkImpl
import org.gce.racehub.auth.domain.repository.AuthRepository
import org.gce.racehub.auth.domain.usecase.ConfirmPasswordResetUseCase
import org.gce.racehub.auth.domain.usecase.LoginUseCase
import org.gce.racehub.auth.domain.usecase.LogoutUseCase
import org.gce.racehub.auth.domain.usecase.RequestPasswordResetUseCase
import org.gce.racehub.auth.domain.usecase.ResendOtpUseCase
import org.gce.racehub.auth.domain.usecase.SendOtpUseCase
import org.gce.racehub.auth.domain.usecase.SignUpUseCase
import org.gce.racehub.auth.domain.usecase.VerifyOtpUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin dependency injection module for authentication.
 *
 \* Wires the auth network stack, the session and the auth use cases.
 */

/**
 * Creates a Koin module for authentication dependencies.
 *
 * @param baseUrl The API base URL for network calls
 */
internal fun createAuthModule(baseUrl: String): Module = module {

    // Uses the shared HttpClient from coreModule.
    single {
        AuthService(get(), baseUrl)
    }

    single<AuthRepository> { AuthRepositoryNetworkImpl(get()) }

    factory { LoginUseCase(get()) }
    factory { SignUpUseCase(get()) }
    factory { LogoutUseCase(get(), get()) }
    factory { RequestPasswordResetUseCase(get()) }
    factory { ConfirmPasswordResetUseCase(get()) }
    factory { SendOtpUseCase(get()) }
    factory { ResendOtpUseCase(get()) }
    factory { VerifyOtpUseCase(get()) }
}

/**
 * Convenience function to create a production auth module.
 *
 * @param baseUrl The API base URL
 * @return Koin module configured for production
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun createProductionAuthModule(baseUrl: String): Module =
    createAuthModule(baseUrl)
