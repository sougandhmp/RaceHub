package org.gce.racehub.race.domain.usecase

import org.gce.racehub.race.domain.model.ThreadComment
import org.gce.racehub.race.domain.repository.HomeRepository

class AddCommentUseCase(private val repository: HomeRepository) {

    @Throws(Exception::class)
    suspend operator fun invoke(
        userId: String,
        threadId: String,
        content: String
    ): ThreadComment {
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
