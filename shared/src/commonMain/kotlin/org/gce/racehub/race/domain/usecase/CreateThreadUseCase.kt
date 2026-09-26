package org.gce.racehub.race.domain.usecase

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.race.domain.model.ForumCategories
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.race.domain.repository.HomeRepository

class CreateThreadUseCase(private val repository: HomeRepository) {

    /**
     * Validates the input and posts the new thread.
     *
     * @throws IllegalArgumentException when input validation fails.
     * @throws Exception for transport / server errors raised by the repository.
     */
    @Throws(Exception::class)
    suspend operator fun invoke(
        userId: String,
        title: String,
        category: String,
        content: String
    ): DataResult<Thread> {
        require(userId.isNotBlank()) { "You must be signed in to post." }
        require(title.isNotBlank()) { "Title can't be empty." }
        require(content.isNotBlank()) { "Content can't be empty." }
        val resolvedCategory = category.ifBlank { ForumCategories.DEFAULT }

        return repository.createThread(
            userId = userId,
            title = title.trim(),
            category = resolvedCategory.trim(),
            content = content.trim()
        )
    }
}
