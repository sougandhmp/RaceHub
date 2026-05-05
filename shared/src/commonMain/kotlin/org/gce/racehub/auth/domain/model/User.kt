package org.gce.racehub.auth.domain.model

/**
 * Core domain entity representing an authenticated RaceHub user.
 *
 * Intentionally free of platform or framework dependencies so it can be
 * shared across Android and iOS via the KMP `shared` module.
 */
data class User(

    /** Unique identifier assigned by the backend. */
    val id: String,

    /** The user's email address, used as their login credential. */
    val email: String,

    /** The user's display name shown throughout the app. */
    val name: String,

    /** The user's authentication token returned by the server. */
    val token: String? = null,

    /** The user's username. */
    val username: String? = null,

    /** The user's country code. */
    val country: String? = null,

    /** The user's avatar initials or identifier. */
    val avatar: String? = null,

    /** The user's role (e.g., "member", "admin"). */
    val role: String? = null,

    /** When the user joined the platform (ISO 8601 format). */
    val joinedAt: String? = null,

    /** Total number of posts created by this user. */
    val postsCount: Int = 0
)
