package org.gce.racehub.race.domain.repository

import org.gce.racehub.core.DataResult
import org.gce.racehub.race.domain.model.UserProfile

/** The signed-in user's profile. */
interface ProfileRepository {

    suspend fun getMyProfile(userId: String, token: String): DataResult<UserProfile>
}
