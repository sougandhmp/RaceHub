package org.gce.racehub.core.domain

/**
 * Outcome of a data operation, returned across the repository boundary so the
 * domain and presentation layers handle failures as values instead of catching
 * data-layer exceptions (Ktor, SQLDelight, serialization) they must not know about.
 *
 * [E] is the failure type: [DataError] for plain data reads, or a feature's own
 * error when it can fail in more ways (e.g. auth validation and server rejections).
 */
sealed interface DataResult<out T, out E> {
    data class Success<out T>(val data: T) : DataResult<T, Nothing>
    data class Failure<out E>(val error: E) : DataResult<Nothing, E>
}

/** Why a data operation failed, in terms the UI can act on. */
enum class DataError {
    /** No connection, DNS failure or timeout: retrying later may help. */
    Network,

    /** The server answered but with an error or a payload we could not read. */
    Server,

    /** Anything else. */
    Unknown
}

inline fun <T, E, R> DataResult<T, E>.map(transform: (T) -> R): DataResult<R, E> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Failure -> this
}

inline fun <T, E, F> DataResult<T, E>.mapError(transform: (E) -> F): DataResult<T, F> = when (this) {
    is DataResult.Success -> this
    is DataResult.Failure -> DataResult.Failure(transform(error))
}

/** The data on success, otherwise null. */
fun <T> DataResult<T, *>.dataOrNull(): T? = (this as? DataResult.Success)?.data

/** The error on failure, otherwise null. */
fun <E> DataResult<*, E>.errorOrNull(): E? = (this as? DataResult.Failure)?.error
