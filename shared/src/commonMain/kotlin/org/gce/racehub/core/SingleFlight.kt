package org.gce.racehub.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Shares one in-flight run of a suspend operation between concurrent callers.
 *
 * If [run] is called while a previous run is still active, the caller awaits that
 * run instead of starting another. For example, the Race tab asking for standings
 * and trending threads at the same time triggers only one dashboard request.
 *
 * The work runs in [scope], not in the caller's coroutine: if one caller is cancelled,
 * the shared run keeps going for the others.
 */
class SingleFlight<T>(private val scope: CoroutineScope) {
    private val mutex = Mutex()
    private var inFlight: Deferred<T>? = null

    suspend fun run(block: suspend () -> T): T {
        val deferred = mutex.withLock {
            inFlight?.takeIf { it.isActive } ?: scope.async { block() }.also { inFlight = it }
        }
        return deferred.await()
    }
}
