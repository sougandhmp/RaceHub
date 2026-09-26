package org.gce.racehub.race

import org.gce.racehub.race.domain.model.parseRaceInstant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Parses an API timestamp via the shared parser (zone-less values are UTC). */
internal fun parseIsoToDate(dateTime: String): Date? =
    parseRaceInstant(dateTime)?.let { Date(it.toEpochMilliseconds()) }

internal fun formatRaceDate(dateTime: String): String {
    val date = parseIsoToDate(dateTime) ?: return dateTime
    return SimpleDateFormat("d MMM yyyy · HH:mm 'UTC'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(date)
}
