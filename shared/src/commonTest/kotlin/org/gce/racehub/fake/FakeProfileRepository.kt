package org.gce.racehub.fake

import org.gce.racehub.core.domain.DataError
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.profile.domain.model.UserProfile
import org.gce.racehub.profile.domain.repository.ProfileRepository

internal class FakeProfileRepository : ProfileRepository {
    var profileResult: UserProfile = UserProfile("user", "u@e.com", "", 0, 0, emptyList(), emptyList())
    var profileError: DataError? = null

    override suspend fun getMyProfile(userId: String, token: String): DataResult<UserProfile> {
        profileError?.let { return DataResult.Failure(it) }
        return DataResult.Success(profileResult)
    }
}
