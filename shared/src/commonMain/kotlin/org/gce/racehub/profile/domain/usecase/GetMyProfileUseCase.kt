package org.gce.racehub.profile.domain.usecase

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.profile.domain.model.UserProfile
import org.gce.racehub.profile.domain.repository.ProfileRepository

internal class GetMyProfileUseCase(private val repository: ProfileRepository) {

    @Throws(Exception::class)
    suspend operator fun invoke(userId: String, token: String): DataResult<UserProfile> {
        require(userId.isNotBlank()) { "You must be signed in." }
        require(token.isNotBlank()) { "Missing auth token." }
        return repository.getMyProfile(userId = userId, token = token)
    }
}
