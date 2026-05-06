package org.gce.racehub.profile

/**
 * One-time side effects produced by [ProfileViewModel] — typically navigation
 * events that must not be replayed on recomposition.
 */
sealed class ProfileEffect {

    /** Sign the current user out and return to the Login screen. */
    data object SignedOut : ProfileEffect()
}
