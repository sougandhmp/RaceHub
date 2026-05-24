package org.gce.racehub.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// Native has no dedicated IO dispatcher; Default is the closest equivalent.
actual val platformIoDispatcher: CoroutineDispatcher = Dispatchers.Default
