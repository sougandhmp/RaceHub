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
    val name: String
)
