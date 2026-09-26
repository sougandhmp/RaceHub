package org.gce.racehub.profile.domain.repository

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.profile.domain.model.UserProfile

/** Contract for the signed-in user's profile. Failures come back as [DataResult.Failure]. */
internal interface ProfileRepository {

    /**
     * The signed-in user's profile: post/saved counts and recent/saved thread titles.
     *
     * @param token Sent as `Authorization: Bearer <token>`.
     */
    suspend fun getMyProfile(userId: String, token: String): DataResult<UserProfile>
}
