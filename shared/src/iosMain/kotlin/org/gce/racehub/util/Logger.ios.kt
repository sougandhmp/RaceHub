package org.gce.racehub.util

internal actual fun logError(tag: String, message: String, throwable: Throwable?) {
    val detail = throwable?.message?.let { ": $it" } ?: ""
    println("[$tag/ERROR] $message$detail")
}
