package org.gce.racehub.auth.domain.model

import org.gce.racehub.core.domain.DataError
import org.gce.racehub.core.domain.DataResult

/** Result of an auth operation: the value on success, an [AuthFailure] otherwise. */
typealias AuthOutcome<T> = DataResult<T, AuthFailure>

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
    internal companion object {
        /** A transport or payload failure, in auth terms. */
        fun from(error: DataError): AuthFailure = AuthFailure(
            when (error) {
                DataError.Network -> AuthError.Network
                DataError.Server -> AuthError.Server
                DataError.Unknown -> AuthError.Unknown
            }
        )
    }
}

/** A failed [AuthOutcome] for [reason]. */
internal fun authFailure(reason: AuthError, serverMessage: String? = null): AuthOutcome<Nothing> =
    DataResult.Failure(AuthFailure(reason, serverMessage))
