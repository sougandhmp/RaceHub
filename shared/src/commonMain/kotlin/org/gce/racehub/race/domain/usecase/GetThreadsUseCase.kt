package org.gce.racehub.race.domain.usecase

import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.race.domain.repository.HomeRepository

class GetThreadsUseCase(private val repository: HomeRepository) {
    suspend fun execute(
        sort: String? = "latest",
        category: String? = null,
        userId: String? = null
    ): List<Thread> = repository.getThreads(sort = sort, category = category, userId = userId)
}
