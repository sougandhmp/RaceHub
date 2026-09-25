package org.gce.racehub.util

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Platform dispatcher for blocking I/O (database, disk).
 *
 * `Dispatchers.IO` only exists on the JVM, so it is provided via expect/actual:
 * Android maps to `Dispatchers.IO`, iOS/Native falls back to `Dispatchers.Default`.
 */
expect val platformIoDispatcher: CoroutineDispatcher
