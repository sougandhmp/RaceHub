package org.gce.racehub.race.domain.usecase

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.race.domain.model.TrendingThread
import org.gce.racehub.race.domain.repository.RaceRepository

internal class GetTrendingThreadsUseCase(private val repository: RaceRepository) {
    suspend operator fun invoke(): DataResult<List<TrendingThread>, DataError> = repository.getTrendingThreads()
}
