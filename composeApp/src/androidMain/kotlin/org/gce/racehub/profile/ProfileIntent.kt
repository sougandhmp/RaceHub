package org.gce.racehub.profile

/**
 * Every user interaction targeted at the Profile tab expressed as an explicit event.
 * The View dispatches intents; [ProfileViewModel] is the sole handler.
 */
sealed class ProfileIntent {
    data object SignOut : ProfileIntent()
    data object RefreshProfile : ProfileIntent()
    data object DismissError : ProfileIntent()
}
