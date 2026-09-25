package org.gce.racehub.race

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal fun parseIsoToDate(dateTime: String): Date? {
    if (dateTime.isBlank()) return null
    val s = dateTime.trim().replace(Regex("([+-]\\d{2}):(\\d{2})$"), "$1$2")
    val utcPatterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
    )
    val tzPatterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ",
    )
    val utc = TimeZone.getTimeZone("UTC")
    for (fmt in utcPatterns) {
        try {
            val d = SimpleDateFormat(fmt, Locale.US).apply { timeZone = utc }.parse(s)
            if (d != null) return d
        } catch (_: Exception) {}
    }
    for (fmt in tzPatterns) {
        try {
            val d = SimpleDateFormat(fmt, Locale.US).parse(s)
            if (d != null) return d
        } catch (_: Exception) {}
    }
    return null
}

internal fun formatRaceDate(dateTime: String): String {
    val date = parseIsoToDate(dateTime) ?: return dateTime
    return SimpleDateFormat("d MMM yyyy · HH:mm 'UTC'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(date)
}
