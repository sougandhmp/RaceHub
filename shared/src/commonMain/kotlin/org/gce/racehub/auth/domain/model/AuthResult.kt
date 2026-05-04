package org.gce.racehub.auth.domain.model

/**
 * The outcome of an authentication operation (login or sign-up).
 *
 * Uses a plain-class wrapper instead of a sealed class so that both
 * [user] and [error] are directly accessible as nullable properties from
 * Swift/Objective-C, avoiding the cumbersome cast syntax that sealed
 * subclasses produce in Kotlin/Native.
 *
 * Construct via the [success] or [failure] factory functions — the primary
 * constructor is private to enforce that exactly one field is populated.
 */
class AuthResult private constructor(

    /** The authenticated [User]; non-null only on success. */
    val user: User? = null,

    /** A human-readable error description; non-null only on failure. */
    val error: String? = null
) {
    /** True when authentication succeeded and [user] is available. */
    val isSuccess: Boolean get() = user != null

    companion object {
        /** Creates a successful result carrying the authenticated [user]. */
        fun success(user: User): AuthResult = AuthResult(user = user)

        /** Creates a failed result carrying the [error] message to display. */
        fun failure(error: String): AuthResult = AuthResult(error = error)
    }
}
