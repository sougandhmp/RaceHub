package org.gce.racehub.race.domain.usecase

import org.gce.racehub.core.DataError
import org.gce.racehub.core.DataResult
import org.gce.racehub.race.domain.model.UserProfile
import org.gce.racehub.race.domain.repository.ProfileRepository

class GetMyProfileUseCase(private val repository: ProfileRepository) {
    @Throws(Exception::class)
    suspend operator fun invoke(userId: String, token: String): DataResult<UserProfile> {
        if (userId.isBlank()) return DataResult.failure(DataError.InvalidInput("You must be signed in."))
        if (token.isBlank()) return DataResult.failure(DataError.InvalidInput("Missing auth token."))
        return repository.getMyProfile(userId = userId, token = token)
    }
}
