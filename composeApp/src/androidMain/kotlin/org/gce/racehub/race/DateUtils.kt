package org.gce.racehub.race

import java.text.ParsePosition
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
        "yyyy-MM-dd'T'HH:mm",
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
        parseWhole(s, SimpleDateFormat(fmt, Locale.US).apply { timeZone = utc })?.let { return it }
    }
    for (fmt in tzPatterns) {
        parseWhole(s, SimpleDateFormat(fmt, Locale.US))?.let { return it }
    }
    return null
}

// SimpleDateFormat.parse(String) accepts a match on just a prefix, so e.g.
// "yyyy-MM-dd" would swallow "2026-05-24T20:00" and drop the time. Only accept
// a pattern that consumes the whole string.
private fun parseWhole(s: String, format: SimpleDateFormat): Date? {
    format.isLenient = false
    val pos = ParsePosition(0)
    val date = format.parse(s, pos)
    return if (date != null && pos.index == s.length) date else null
}

internal fun formatRaceDate(dateTime: String): String {
    val date = parseIsoToDate(dateTime) ?: return dateTime
    return SimpleDateFormat("d MMM yyyy · HH:mm 'UTC'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(date)
}
