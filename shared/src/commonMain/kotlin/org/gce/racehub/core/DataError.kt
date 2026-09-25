package org.gce.racehub.core

/**
 * Why a data operation failed, as seen by the UI.
 *
 * Repositories are the error boundary: every network, parsing or validation failure
 * is turned into one of these, so ViewModels never handle raw exceptions.
 * [message] is ready to show to the user; subclasses exist so callers can react
 * differently (for example, offer "Retry" only for [NoConnection] or [Timeout]).
 */
sealed class DataError(val message: String) {

    /** The device couldn't reach the server (offline, DNS, connection refused). */
    data object NoConnection : DataError("No internet connection. Check your connection and try again.")

    /** The server didn't answer in time. */
    data object Timeout : DataError("The server took too long to respond. Please try again.")

    /** The server answered with an error, such as a GraphQL error or an HTTP 4xx/5xx. */
    class Server(detail: String?) : DataError(detail?.takeIf { it.isNotBlank() } ?: "Something went wrong on the server.")

    /** The request was rejected before it was sent, because an input was invalid. */
    class InvalidInput(reason: String) : DataError(reason)

    /** Anything else, such as a response that couldn't be parsed. */
    class Unknown(detail: String?) : DataError(detail?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again.")
}
