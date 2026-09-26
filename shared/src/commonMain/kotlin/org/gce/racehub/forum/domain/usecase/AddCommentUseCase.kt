package org.gce.racehub.forum.domain.usecase

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.forum.domain.model.ThreadComment
import org.gce.racehub.forum.domain.repository.ForumRepository

internal class AddCommentUseCase(private val repository: ForumRepository) {

    @Throws(Exception::class)
    suspend operator fun invoke(
        userId: String,
        threadId: String,
        content: String
    ): DataResult<ThreadComment> {
        require(userId.isNotBlank()) { "You must be signed in to comment." }
        require(threadId.isNotBlank()) { "Invalid thread." }
        require(content.isNotBlank()) { "Comment can't be empty." }
        return repository.addComment(
            userId = userId,
            threadId = threadId,
            content = content.trim()
        )
    }
}
