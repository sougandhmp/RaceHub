package org.gce.racehub.race.domain.usecase

import org.gce.racehub.core.DataResult
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.race.domain.repository.ForumRepository

class GetThreadsUseCase(private val repository: ForumRepository) {
    @Throws(Exception::class)
    suspend operator fun invoke(
        sort: String? = "latest",
        category: String? = null,
        userId: String? = null
    ): DataResult<List<Thread>> = repository.getThreads(sort = sort, category = category, userId = userId)
}
