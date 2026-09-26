package org.gce.racehub.race.presentation

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.ScheduledSession
import org.gce.racehub.race.domain.model.parseRaceInstant
import org.gce.racehub.race.domain.model.weekendSchedule

/**
 * One session of a race weekend, formatted for display so Android and iOS
 * render identical values. [date] and [time] are "—" when unknown.
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

/** Race-day label for the header, e.g. "SUN MAY 24" (UTC, the official race date); "" if unparseable. */
fun raceHeaderDate(dateTime: String): String {
    val instant = parseRaceInstant(dateTime) ?: return ""
    return headerFormat.format(instant.toLocalDateTime(TimeZone.UTC)).uppercase()
}

/** Display sessions for [race] in the device's time zone (see [weekendSchedule] for the rules). */
fun weekendSessions(race: Race?, detail: RaceDetail?): List<WeekendSession> =
    formatSessions(weekendSchedule(race, detail), TimeZone.currentSystemDefault())

/** Formats domain [sessions] for display in [timeZone]. */
fun formatSessions(sessions: List<ScheduledSession>, timeZone: TimeZone): List<WeekendSession> =
    sessions.map { session ->
        val local = session.start?.toLocalDateTime(timeZone)
        WeekendSession(
            label = session.label,
            shortLabel = shortSessionLabel(session.label),
            date = local?.let(dateFormat::format) ?: PLACEHOLDER,
            time = local?.let(timeFormat::format) ?: PLACEHOLDER
        )
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

private const val PLACEHOLDER = "—"

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
