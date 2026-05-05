package org.gce.racehub.race.domain.usecase

import org.gce.racehub.race.domain.model.TrendingThread
import org.gce.racehub.race.domain.repository.HomeRepository

class GetTrendingThreadsUseCase(private val repository: HomeRepository) {
    suspend fun execute(): List<TrendingThread> = repository.getTrendingThreads()
}
