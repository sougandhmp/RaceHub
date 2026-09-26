package org.gce.racehub.race.domain.usecase

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.race.domain.model.ThreadSort
import org.gce.racehub.race.domain.repository.HomeRepository

class GetThreadsUseCase(private val repository: HomeRepository) {
    @Throws(Exception::class)
    suspend operator fun invoke(
        sort: ThreadSort = ThreadSort.Latest,
        category: String? = null,
        userId: String? = null
    ): DataResult<List<Thread>> = repository.getThreads(sort = sort, category = category, userId = userId)
}
