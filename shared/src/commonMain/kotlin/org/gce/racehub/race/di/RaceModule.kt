package org.gce.racehub.race.di

import org.gce.racehub.race.data.repository.HomeRepositoryImpl
import org.gce.racehub.race.domain.repository.HomeRepository
import org.gce.racehub.race.domain.usecase.GetDriverStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetRaceScheduleUseCase
import org.koin.dsl.module

val raceModule = module {
    single<HomeRepository> { HomeRepositoryImpl() }
    factory { GetRaceScheduleUseCase(get()) }
    factory { GetDriverStandingsUseCase(get()) }
}
