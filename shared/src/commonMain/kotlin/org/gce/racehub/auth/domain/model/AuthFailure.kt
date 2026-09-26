package org.gce.racehub.auth.domain.model

/** Why an auth operation failed. Each platform maps these to localized text. */
enum class AuthError {
    EmailRequired,
    EmailAndPasswordRequired,
    InvalidEmail,
    PasswordTooShort,
    PasswordsDoNotMatch,
    UsernameRequired,
    CountryRequired,
    CodeRequired,
    /** The server refused the request (wrong password, email taken, bad code…); see [AuthFailure.serverMessage]. */
    Rejected,
    Network,
    Server,
    Unknown
}

/**
 * A failed auth operation. [serverMessage] carries the server's own explanation
 * for [AuthError.Rejected] (e.g. "Invalid credentials"), shown when present.
 */
data class AuthFailure(val reason: AuthError, val serverMessage: String? = null) {
    companion object {
        /** Fallback when a failed result carries no reason. */
        val Unknown = AuthFailure(AuthError.Unknown)
    }
}
