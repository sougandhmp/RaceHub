package org.gce.racehub.util

import android.util.Log

internal actual fun logError(tag: String, message: String, throwable: Throwable?) {
    Log.e(tag, message, throwable)
}
