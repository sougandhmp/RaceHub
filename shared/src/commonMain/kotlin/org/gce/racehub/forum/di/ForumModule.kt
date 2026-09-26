package org.gce.racehub.forum.di

import org.gce.racehub.forum.data.repository.ForumRepositoryNetworkImpl
import org.gce.racehub.forum.domain.repository.ForumRepository
import org.gce.racehub.forum.domain.usecase.AddCommentUseCase
import org.gce.racehub.forum.domain.usecase.CreateThreadUseCase
import org.gce.racehub.forum.domain.usecase.GetThreadsUseCase
import org.gce.racehub.forum.domain.usecase.LikeThreadUseCase
import org.koin.dsl.module

/** Forum data and use cases. Needs coreModule (HttpClient). */
internal fun createForumModule(baseUrl: String) = module {
    single<ForumRepository> { ForumRepositoryNetworkImpl(get(), baseUrl) }
    factory { GetThreadsUseCase(get()) }
    factory { CreateThreadUseCase(get()) }
    factory { AddCommentUseCase(get()) }
    factory { LikeThreadUseCase(get()) }
}
