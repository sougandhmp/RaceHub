package org.gce.racehub.race.presentation

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * One session of a race weekend, already formatted for display so Android and
 * iOS render identical values. [date] and [time] are "—" when unknown.
 */
data class WeekendSession(
    /** Normalised label, e.g. "PRACTICE 1", "QUALIFYING", "SPRINT". */
    val label: String,
    /** Compact label for chips, e.g. "FP1", "QUAL", "SPR". */
    val shortLabel: String,
    /** Local date, e.g. "May 23". */
    val date: String,
    /** Local 24-hour time, e.g. "06:00". */
    val time: String
)

/**
 * Parses a timestamp from the API. The API sends zone-less values such as
 * `2026-05-24T20:00`, which are UTC; values carrying `Z` or an offset keep it.
 */
fun parseRaceInstant(dateTime: String): Instant? {
    val s = dateTime.trim().replaceFirst(' ', 'T')
    if (s.isEmpty()) return null
    runCatching { return Instant.parse(s) }
    runCatching { return LocalDateTime.parse(s).toInstant(TimeZone.UTC) }
    runCatching { return LocalDate.parse(s).atStartOfDayIn(TimeZone.UTC) }
    return null
}

/** Race-day label for the header, e.g. "SUN MAY 24" (UTC, the official race date); "" if unparseable. */
fun raceHeaderDate(dateTime: String): String {
    val instant = parseRaceInstant(dateTime) ?: return ""
    return headerFormat.format(instant.toLocalDateTime(TimeZone.UTC)).uppercase()
}

/** Weekend sessions for [race] in the device's time zone. See the overload for the rules. */
fun weekendSessions(race: Race?, detail: RaceDetail?): List<WeekendSession> =
    weekendSessions(race, detail, TimeZone.currentSystemDefault())

/**
 * Weekend sessions for [race], formatted in [timeZone].
 *
 * Uses the real sessions from [detail] when the API supplied them. Otherwise
 * estimates a standard weekend from the race start (FP1/FP2 two days before,
 * FP3/qualifying the day before), and falls back to placeholders when the race
 * date is unknown.
 */
fun weekendSessions(race: Race?, detail: RaceDetail?, timeZone: TimeZone): List<WeekendSession> {
    val real = detail?.sessions.orEmpty()
    if (real.isNotEmpty()) {
        return real.map { session(it.label, parseRaceInstant(it.dateTime), timeZone) }
    }
    val raceStart = race?.let { parseRaceInstant(it.dateTime) }
        ?: return ESTIMATED_OFFSETS.map { (label, _, _) -> session(label, null, timeZone) }
    return ESTIMATED_OFFSETS.map { (label, days, hours) ->
        val start = raceStart.minus(days, DateTimeUnit.DAY, TimeZone.UTC) - hours.hours
        session(label, start, timeZone)
    }
}

/** Maps the API's many spellings ("FP1", "Practice 1", "Q", …) onto one label. */
fun sessionLabel(raw: String): String = when (raw.uppercase().trim()) {
    "FP1", "PRACTICE 1", "P1", "PRACTICE1" -> "PRACTICE 1"
    "FP2", "PRACTICE 2", "P2", "PRACTICE2" -> "PRACTICE 2"
    "FP3", "PRACTICE 3", "P3", "PRACTICE3" -> "PRACTICE 3"
    "QUAL", "QUALIFYING", "Q" -> "QUALIFYING"
    "RACE", "GRAND PRIX" -> "RACE"
    "SPRINT QUALIFYING", "SPRINT QUAL", "SQ" -> "SPRINT QUAL"
    "SPRINT" -> "SPRINT"
    else -> raw.uppercase().trim()
}

private fun shortSessionLabel(label: String): String = when (label) {
    "PRACTICE 1" -> "FP1"
    "PRACTICE 2" -> "FP2"
    "PRACTICE 3" -> "FP3"
    "QUALIFYING" -> "QUAL"
    "RACE" -> "RACE"
    "SPRINT" -> "SPR"
    "SPRINT QUAL" -> "SQ"
    else -> label.take(4)
}

private fun session(rawLabel: String, start: Instant?, timeZone: TimeZone): WeekendSession {
    val label = sessionLabel(rawLabel)
    val local = start?.toLocalDateTime(timeZone)
    return WeekendSession(
        label = label,
        shortLabel = shortSessionLabel(label),
        date = local?.let(dateFormat::format) ?: PLACEHOLDER,
        time = local?.let(timeFormat::format) ?: PLACEHOLDER
    )
}

private const val PLACEHOLDER = "—"

/** (label, days before the race, extra hours before) for an estimated standard weekend. */
private val ESTIMATED_OFFSETS = listOf(
    Triple("PRACTICE 1", 2, 3),
    Triple("PRACTICE 2", 2, 0),
    Triple("PRACTICE 3", 1, 3),
    Triple("QUALIFYING", 1, 0),
    Triple("RACE", 0, 0),
)

private val headerFormat = LocalDateTime.Format {
    dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED); char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED); char(' ')
    day(padding = Padding.NONE)
}

private val dateFormat = LocalDateTime.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED); char(' ')
    day(padding = Padding.NONE)
}

private val timeFormat = LocalDateTime.Format {
    hour(); char(':'); minute()
}
