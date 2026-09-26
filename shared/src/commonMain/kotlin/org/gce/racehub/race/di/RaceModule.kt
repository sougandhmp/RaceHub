package org.gce.racehub.race.di

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import org.gce.racehub.db.DatabaseDriverFactory
import org.gce.racehub.db.LocalDataSource
import org.gce.racehub.race.data.repository.RaceRepositoryNetworkImpl
import org.gce.racehub.race.domain.repository.RaceRepository
import org.gce.racehub.race.domain.usecase.GetConstructorStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetDriverStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetRaceDetailUseCase
import org.gce.racehub.race.domain.usecase.GetRaceScheduleUseCase
import org.gce.racehub.race.domain.usecase.GetTrendingThreadsUseCase
import org.koin.dsl.module

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun createRaceModule(baseUrl: String) = module {
    single { LocalDataSource(get<DatabaseDriverFactory>().createDriver()) }
    single<RaceRepository> {
        RaceRepositoryNetworkImpl(get(), baseUrl, get())
    }
    factory { GetRaceScheduleUseCase(get()) }
    factory { GetDriverStandingsUseCase(get()) }
    factory { GetConstructorStandingsUseCase(get()) }
    factory { GetTrendingThreadsUseCase(get()) }
    factory { GetRaceDetailUseCase(get()) }
}

