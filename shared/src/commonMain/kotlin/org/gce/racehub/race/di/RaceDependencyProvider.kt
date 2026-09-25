package org.gce.racehub.race.di

import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.race.domain.usecase.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

/**
 * Bridge for Swift/iOS to access race, forum and profile dependencies registered with Koin.
 *
 * Mirrors [org.gce.racehub.auth.di.AuthDependencyProvider] so the iOS layer can
 * resolve dependencies without touching Koin directly. Swift gets use cases only,
 * never repositories, so both platforms go through the same domain layer.
 */
class RaceDependencyProvider : KoinComponent {

    val userSession: UserSession by inject()

    // Race
    fun createRefreshRaceDataUseCase(): RefreshRaceDataUseCase = get()
    fun createGetRaceScheduleUseCase(): GetRaceScheduleUseCase = get()
    fun createGetDriverStandingsUseCase(): GetDriverStandingsUseCase = get()
    fun createGetConstructorStandingsUseCase(): GetConstructorStandingsUseCase = get()
    fun createGetTrendingThreadsUseCase(): GetTrendingThreadsUseCase = get()
    fun createGetRaceDetailUseCase(): GetRaceDetailUseCase = get()

    // Forum
    fun createGetThreadsUseCase(): GetThreadsUseCase = get()
    fun createCreateThreadUseCase(): CreateThreadUseCase = get()
    fun createAddCommentUseCase(): AddCommentUseCase = get()
    fun createLikeThreadUseCase(): LikeThreadUseCase = get()

    // Profile
    fun createGetMyProfileUseCase(): GetMyProfileUseCase = get()

    companion object {
        val shared = RaceDependencyProvider()
    }
}
