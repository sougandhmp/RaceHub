package org.gce.racehub.race.di

import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.race.domain.repository.HomeRepository
import org.gce.racehub.race.domain.usecase.CreateThreadUseCase
import org.gce.racehub.race.domain.usecase.GetThreadsUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Bridge for Swift/iOS to access race-related dependencies registered with Koin.
 *
 * Mirrors [org.gce.racehub.auth.di.AuthDependencyProvider] so the iOS layer can
 * resolve dependencies without touching Koin directly.
 */
class RaceDependencyProvider : KoinComponent {

    val homeRepository: HomeRepository by inject()
    val userSession: UserSession by inject()

    fun createCreateThreadUseCase(): CreateThreadUseCase = CreateThreadUseCase(homeRepository)
    fun createGetThreadsUseCase(): GetThreadsUseCase = GetThreadsUseCase(homeRepository)

    companion object {
        val shared = RaceDependencyProvider()
    }
}
