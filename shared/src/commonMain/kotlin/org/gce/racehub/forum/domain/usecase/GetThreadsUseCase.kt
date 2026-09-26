package org.gce.racehub.forum.domain.usecase

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.forum.domain.model.Thread
import org.gce.racehub.forum.domain.model.ThreadSort
import org.gce.racehub.forum.domain.repository.ForumRepository

internal class GetThreadsUseCase(private val repository: ForumRepository) {
    suspend operator fun invoke(
        sort: ThreadSort = ThreadSort.Latest,
        category: String? = null,
        userId: String? = null
    ): DataResult<List<Thread>, DataError> = repository.getThreads(sort = sort, category = category, userId = userId)
}
