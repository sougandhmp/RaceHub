package org.gce.racehub.race.domain.usecase

import org.gce.racehub.race.domain.model.UserProfile
import org.gce.racehub.race.domain.repository.HomeRepository

class GetMyProfileUseCase(private val repository: HomeRepository) {

    @Throws(Exception::class)
    suspend operator fun invoke(userId: String, token: String): UserProfile {
        require(userId.isNotBlank()) { "You must be signed in." }
        require(token.isNotBlank()) { "Missing auth token." }
        return repository.getMyProfile(userId = userId, token = token)
    }
}
