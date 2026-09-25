package org.gce.racehub.fake

import org.gce.racehub.core.DataResult
import org.gce.racehub.race.domain.model.UserProfile
import org.gce.racehub.race.domain.repository.ProfileRepository

class FakeProfileRepository : ProfileRepository {
    var profileResult: DataResult<UserProfile> =
        DataResult.success(UserProfile("user", "u@e.com", "", 0, 0, emptyList(), emptyList()))
    var lastArgs: Pair<String, String>? = null

    override suspend fun getMyProfile(userId: String, token: String): DataResult<UserProfile> {
        lastArgs = userId to token
        return profileResult
    }
}
