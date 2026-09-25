package org.gce.racehub.profile

import org.gce.racehub.auth.domain.model.User
import org.gce.racehub.race.domain.model.UserProfile

data class ProfileState(
    val user: User? = null,
    val profile: UserProfile? = null,
    val isLoadingProfile: Boolean = false,
    val isSigningOut: Boolean = false,
    val errorMessage: String? = null
)
