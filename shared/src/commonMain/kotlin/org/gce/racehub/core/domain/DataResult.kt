package org.gce.racehub.core.domain

/**
 * Outcome of a data operation, returned across the repository boundary so the
 * domain and presentation layers handle failures as values instead of catching
 * data-layer exceptions (Ktor, SQLDelight, serialization) they must not know about.
 */
sealed interface DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
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

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Failure -> this
}
