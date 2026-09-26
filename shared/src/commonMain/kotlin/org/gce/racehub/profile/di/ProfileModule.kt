package org.gce.racehub.profile.di

import org.gce.racehub.profile.data.repository.ProfileRepositoryNetworkImpl
import org.gce.racehub.profile.domain.repository.ProfileRepository
import org.gce.racehub.profile.domain.usecase.GetMyProfileUseCase
import org.koin.dsl.module

/** Profile data and use cases. Reuses the app's single HttpClient. */
internal fun createProfileModule(baseUrl: String) = module {
    single<ProfileRepository> { ProfileRepositoryNetworkImpl(get(), baseUrl) }
    factory { GetMyProfileUseCase(get()) }
}
