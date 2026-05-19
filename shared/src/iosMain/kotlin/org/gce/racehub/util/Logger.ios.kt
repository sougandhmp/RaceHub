package org.gce.racehub.util

actual fun logError(tag: String, message: String, throwable: Throwable?) {
    val detail = throwable?.message?.let { ": $it" } ?: ""
    println("[$tag/ERROR] $message$detail")
}
