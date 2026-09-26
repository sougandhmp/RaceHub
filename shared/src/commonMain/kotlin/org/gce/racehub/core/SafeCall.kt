package org.gce.racehub.core

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import org.gce.racehub.util.logError

/** Thrown by repositories when a GraphQL response has no data; carries the server's error message. */
class GraphQLException(message: String) : Exception(message)

/**
 * Runs [block] and turns any failure into a [DataResult] with a [DataError].
 *
 * [CancellationException] is always rethrown, so cancelling the caller (for example,
 * leaving a screen) still cancels the work instead of being reported as an error.
 */
suspend fun <T : Any> safeCall(tag: String, block: suspend () -> T): DataResult<T> =
    try {
        DataResult.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        logError(tag, "Request failed", e)
        DataResult.failure(e.toDataError())
    }

/** Maps an exception from Ktor, kotlinx.serialization or validation to a [DataError]. */
fun Throwable.toDataError(): DataError = when (this) {
    is HttpRequestTimeoutException, is ConnectTimeoutException, is SocketTimeoutException -> DataError.Timeout
    is ResponseException -> DataError.Server("Server error (${response.status.value}).")
    is GraphQLException -> DataError.Server(message)
    // SerializationException extends IllegalArgumentException, so it must be checked first.
    is SerializationException -> DataError.Unknown("Unexpected response from the server.")
    is IllegalArgumentException -> DataError.InvalidInput(message ?: "Invalid input.")
    is IOException -> DataError.NoConnection
    else -> DataError.Unknown(null)
}
