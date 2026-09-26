package org.gce.racehub.forum.domain.usecase

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.forum.domain.repository.ForumRepository

internal class LikeThreadUseCase(private val repository: ForumRepository) {
    suspend operator fun invoke(threadId: String): DataResult<Int> {
        require(threadId.isNotBlank()) { "Invalid thread." }
        return repository.likeThread(threadId)
    }
}
