package org.gce.racehub.profile

/**
 * Every user interaction targeted at the Profile tab expressed as an explicit event.
 * The View dispatches intents; [ProfileViewModel] is the sole handler.
 */
sealed class ProfileIntent {

    /** User tapped the Sign Out button. */
    data object SignOut : ProfileIntent()

    /** Clears the active error message without changing any other state. */
    data object DismissError : ProfileIntent()
}
