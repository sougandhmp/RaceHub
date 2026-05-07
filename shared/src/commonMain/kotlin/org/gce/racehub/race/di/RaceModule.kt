package org.gce.racehub.race.di

import org.gce.racehub.db.LocalDataSource
import org.gce.racehub.race.data.repository.HomeRepositoryNetworkImpl
import org.gce.racehub.race.domain.repository.HomeRepository
import org.gce.racehub.race.domain.usecase.AddCommentUseCase
import org.gce.racehub.race.domain.usecase.CreateThreadUseCase
import org.gce.racehub.race.domain.usecase.GetConstructorStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetDriverStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetMyProfileUseCase
import org.gce.racehub.race.domain.usecase.GetRaceDetailUseCase
import org.gce.racehub.race.domain.usecase.LikeThreadUseCase
import org.gce.racehub.race.domain.usecase.GetRaceScheduleUseCase
import org.gce.racehub.race.domain.usecase.GetThreadsUseCase
import org.gce.racehub.race.domain.usecase.GetTrendingThreadsUseCase
import org.koin.dsl.module

val raceModule = module {
    single { LocalDataSource(get()) }
    single<HomeRepository> {
        HomeRepositoryNetworkImpl(get(), "http://140.245.233.203:30018", get())
    }
    factory { GetRaceScheduleUseCase(get()) }
    factory { GetDriverStandingsUseCase(get()) }
    factory { GetConstructorStandingsUseCase(get()) }
    factory { GetTrendingThreadsUseCase(get()) }
    factory { GetThreadsUseCase(get()) }
    factory { CreateThreadUseCase(get()) }
    factory { AddCommentUseCase(get()) }
    factory { GetMyProfileUseCase(get()) }
    factory { LikeThreadUseCase(get()) }
    factory { GetRaceDetailUseCase(get()) }
}


