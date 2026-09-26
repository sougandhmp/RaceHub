package org.gce.racehub.race.domain.usecase

import org.gce.racehub.core.DataError
import org.gce.racehub.core.DataResult
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.race.domain.repository.ForumRepository

class CreateThreadUseCase(private val repository: ForumRepository) {
    @Throws(Exception::class)
    suspend operator fun invoke(
        userId: String,
        title: String,
        category: String,
        content: String
    ): DataResult<Thread> {
        if (userId.isBlank()) return DataResult.failure(DataError.InvalidInput("You must be signed in to post."))
        if (title.isBlank()) return DataResult.failure(DataError.InvalidInput("Title can't be empty."))
        if (content.isBlank()) return DataResult.failure(DataError.InvalidInput("Content can't be empty."))
        val resolvedCategory = category.ifBlank { "General Discussion" }
        return repository.createThread(
            userId = userId,
            title = title.trim(),
            category = resolvedCategory.trim(),
            content = content.trim()
        )
    }
}
