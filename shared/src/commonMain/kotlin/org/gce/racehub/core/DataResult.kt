package org.gce.racehub.core

/**
 * The outcome of a data operation: either [data] or an [error], never both.
 *
 * Follows the plain-class pattern of [org.gce.racehub.auth.domain.model.AuthResult]
 * rather than a sealed class, so Swift can read [isSuccess], [data] and [error]
 * directly without casting to subclasses.
 */
class DataResult<out T : Any> private constructor(
    /** The value on success; null on failure. */
    val data: T?,
    /** Why the operation failed; null on success. */
    val error: DataError?
) {
    val isSuccess: Boolean get() = error == null

    /** Transforms the value on success; passes a failure through unchanged. */
    fun <R : Any> map(transform: (T) -> R): DataResult<R> =
        if (error == null) success(transform(data!!)) else failure(error)

    companion object {
        fun <T : Any> success(data: T): DataResult<T> = DataResult(data, null)
        fun <T : Any> failure(error: DataError): DataResult<T> = DataResult(null, error)
    }
}
