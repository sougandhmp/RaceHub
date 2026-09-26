package org.gce.racehub.profile.domain.usecase

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.profile.domain.model.UserProfile
import org.gce.racehub.profile.domain.repository.ProfileRepository

internal class GetMyProfileUseCase(private val repository: ProfileRepository) {

    suspend operator fun invoke(userId: String, token: String): DataResult<UserProfile, DataError> {
        require(userId.isNotBlank()) { "You must be signed in." }
        require(token.isNotBlank()) { "Missing auth token." }
        return repository.getMyProfile(userId = userId, token = token)
    }
}
