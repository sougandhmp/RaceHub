package org.gce.racehub.profile.presentation

/** Pure: the only place [ProfileState] changes. */
internal object ProfileReducer {
    fun reduce(state: ProfileState, mutation: ProfileMutation): ProfileState = when (mutation) {
        // A different (or no) user must never see the previous user's profile.
        is ProfileMutation.UserChanged ->
            if (mutation.user?.id == state.user?.id) state.copy(user = mutation.user)
            else ProfileState(user = mutation.user)

        ProfileMutation.LoadStarted -> state.copy(isLoadingProfile = true)
        is ProfileMutation.Loaded -> state.copy(isLoadingProfile = false, profile = mutation.profile)
        ProfileMutation.LoadFailed -> state.copy(isLoadingProfile = false)
        ProfileMutation.SignOutStarted -> state.copy(isSigningOut = true)
        ProfileMutation.SignedOut -> ProfileState()
    }
}
