package org.gce.racehub.forum.domain.usecase

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.forum.domain.model.ForumCategories
import org.gce.racehub.forum.domain.model.Thread
import org.gce.racehub.forum.domain.repository.ForumRepository

internal class CreateThreadUseCase(private val repository: ForumRepository) {

    /**
     * Validates the input and posts the new thread.
     *
     * Transport and server errors come back as a [DataResult] failure.
     *
     * @throws IllegalArgumentException when input validation fails (callers check first).
     */
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
