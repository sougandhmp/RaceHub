package org.gce.racehub.race.di

import org.gce.racehub.db.LocalDataSource
import org.gce.racehub.race.data.network.GraphQLClient
import org.gce.racehub.race.data.repository.ForumRepositoryImpl
import org.gce.racehub.race.data.repository.ProfileRepositoryImpl
import org.gce.racehub.race.data.repository.RaceRepositoryImpl
import org.gce.racehub.race.domain.repository.ForumRepository
import org.gce.racehub.race.domain.repository.ProfileRepository
import org.gce.racehub.race.domain.repository.RaceRepository
import org.gce.racehub.race.domain.usecase.*
import org.koin.dsl.module

fun createRaceModule(baseUrl: String) = module {
    single { LocalDataSource(get()) }
    single { GraphQLClient(get(), baseUrl) }

    // Singletons: RaceRepositoryImpl owns the shared in-flight refreshes, so there must be one.
    single<RaceRepository> { RaceRepositoryImpl(get(), get()) }
    single<ForumRepository> { ForumRepositoryImpl(get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }

    // Race
    factory { ObserveRaceScheduleUseCase(get()) }
    factory { ObserveDriverStandingsUseCase(get()) }
    factory { ObserveConstructorStandingsUseCase(get()) }
    factory { ObserveTrendingThreadsUseCase(get()) }
    factory { GetRaceScheduleUseCase(get()) }
    factory { GetDriverStandingsUseCase(get()) }
    factory { GetConstructorStandingsUseCase(get()) }
    factory { GetTrendingThreadsUseCase(get()) }
    factory { RefreshRaceDataUseCase(get()) }
    factory { GetRaceDetailUseCase(get()) }

    // Forum
    factory { GetThreadsUseCase(get()) }
    factory { CreateThreadUseCase(get()) }
    factory { AddCommentUseCase(get()) }
    factory { LikeThreadUseCase(get()) }

    // Profile
    factory { GetMyProfileUseCase(get()) }
}
