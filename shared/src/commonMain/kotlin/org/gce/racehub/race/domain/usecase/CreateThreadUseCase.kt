package org.gce.racehub.race.domain.usecase

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
    ): Thread {
        require(userId.isNotBlank()) { "You must be signed in to post." }
        require(title.isNotBlank()) { "Title can't be empty." }
        require(content.isNotBlank()) { "Content can't be empty." }
        val resolvedCategory = category.ifBlank { "General Discussion" }

        return repository.createThread(
            userId = userId,
            title = title.trim(),
            category = resolvedCategory.trim(),
            content = content.trim()
        )
    }
}
