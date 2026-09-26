package org.gce.racehub.util

internal expect fun logError(tag: String, message: String, throwable: Throwable? = null)
