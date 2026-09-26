package org.gce.racehub.race.domain.usecase

import org.gce.racehub.core.DataError
import org.gce.racehub.core.DataResult
import org.gce.racehub.race.domain.model.ThreadComment
import org.gce.racehub.race.domain.repository.ForumRepository

class AddCommentUseCase(private val repository: ForumRepository) {
    @Throws(Exception::class)
    suspend operator fun invoke(
        userId: String,
        threadId: String,
        content: String
    ): DataResult<ThreadComment> {
        if (userId.isBlank()) return DataResult.failure(DataError.InvalidInput("You must be signed in to comment."))
        if (threadId.isBlank()) return DataResult.failure(DataError.InvalidInput("Invalid thread."))
        if (content.isBlank()) return DataResult.failure(DataError.InvalidInput("Comment can't be empty."))
        return repository.addComment(userId = userId, threadId = threadId, content = content.trim())
    }
}
