package org.gce.racehub.core.data

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.util.logError

/**
 * Runs a network call at the repository boundary: success becomes
 * [DataResult.Success]; any failure is logged under [tag] and returned as a
 * typed [DataResult.Failure]. Cancellation propagates.
 */
internal suspend fun <T> safeCall(tag: String, what: String, block: suspend () -> T): DataResult<T> = try {
    DataResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    logError(tag, "Failed to $what", e)
    DataResult.Failure(e.toDataError())
}

/** Maps data-layer exceptions onto the domain's [DataError]. */
internal fun Exception.toDataError(): DataError = when (this) {
    is IOException -> DataError.Network // includes timeouts and connection failures
    // The server answered but not with a payload we can use: an error status
    // (whose body can't be converted), malformed JSON, or GraphQL errors.
    is ResponseException, is SerializationException, is ContentConvertException,
    is NoTransformationFoundException, is IllegalStateException -> DataError.Server
    else -> DataError.Unknown
}

/** GraphQL error messages joined, or [fallback] when there are none. */
internal fun List<GraphQLError>?.toErrorMessage(fallback: String): String =
    this?.joinToString { it.message }?.takeIf { it.isNotBlank() } ?: fallback
