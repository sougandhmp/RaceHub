package org.gce.racehub.profile

import org.gce.racehub.auth.domain.model.User

/**
 * Immutable snapshot of the Profile tab. Produced by [ProfileViewModel] on
 * every state change; the View never mutates this object directly.
 */
data class ProfileState(

    /** The currently signed-in user, or `null` while no user is in the session. */
    val user: User? = null,

    /** True while a sign-out request is in flight. */
    val isSigningOut: Boolean = false,

    /** Non-null when an error should be shown to the user. */
    val errorMessage: String? = null
)
