package org.gce.racehub.race.domain.usecase

import org.gce.racehub.core.DataError
import org.gce.racehub.core.DataResult
import org.gce.racehub.race.domain.repository.ForumRepository

/** Toggles the like on a thread. On success, [DataResult.data] is the updated like count. */
class LikeThreadUseCase(private val repository: ForumRepository) {
    @Throws(Exception::class)
    suspend operator fun invoke(threadId: String): DataResult<Int> {
        if (threadId.isBlank()) return DataResult.failure(DataError.InvalidInput("Invalid thread."))
        return repository.likeThread(threadId)
    }
}
