package org.gce.racehub.profile

/**
 * Immutable snapshot of the Profile tab. The screen is a placeholder for now;
 * fields will be added as the profile feature is built out.
 */
data class ProfileState(

    /** True while profile data is being fetched. */
    val isLoading: Boolean = false,

    /** Non-null when an error should be shown to the user. */
    val errorMessage: String? = null
)
