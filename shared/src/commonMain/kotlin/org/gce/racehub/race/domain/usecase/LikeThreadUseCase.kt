package org.gce.racehub.race.domain.usecase

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.race.domain.repository.HomeRepository

class LikeThreadUseCase(private val repository: HomeRepository) {
    @Throws(Exception::class)
    suspend operator fun invoke(threadId: String): DataResult<Int> {
        require(threadId.isNotBlank()) { "Invalid thread." }
        return repository.likeThread(threadId)
    }
}
