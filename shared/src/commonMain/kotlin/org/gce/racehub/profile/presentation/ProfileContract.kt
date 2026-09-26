package org.gce.racehub.profile.presentation

import org.gce.racehub.auth.domain.model.User
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.race.domain.model.UserProfile

// MVI contract for the Profile tab.

/** Immutable snapshot of the Profile tab. Only [ProfileReducer] produces new values. */
data class ProfileState(
    /** Signed-in user from the session; null once signed out. */
    val user: User? = null,
    /** Server profile for [user]; null until loaded. */
    val profile: UserProfile? = null,
    val isLoadingProfile: Boolean = false,
    val isSigningOut: Boolean = false
) {
    /** Posts to show: the server's count once loaded, else the count cached on the session user. */
    val postsCount: Int get() = profile?.postsCount ?: user?.postsCount ?: 0

    /** Saved threads to show; null (render "—") until the profile loads. */
    val savedCount: Int? get() = profile?.savedCount

    val recentThreadTitles: List<String> get() = profile?.recentThreadTitles.orEmpty()
    val savedThreadTitles: List<String> get() = profile?.savedThreadTitles.orEmpty()
}

sealed class ProfileIntent {
    data object Refresh : ProfileIntent()
    data object SignOut : ProfileIntent()
}

sealed class ProfileEffect {
    /** Session cleared; the view should return to the login screen. */
    data object SignedOut : ProfileEffect()
    data class ShowLoadError(val error: DataError) : ProfileEffect()
}

internal sealed interface ProfileMutation {
    data class UserChanged(val user: User?) : ProfileMutation
    data object LoadStarted : ProfileMutation
    data class Loaded(val profile: UserProfile) : ProfileMutation
    data object LoadFailed : ProfileMutation
    data object SignOutStarted : ProfileMutation
    data object SignedOut : ProfileMutation
}
