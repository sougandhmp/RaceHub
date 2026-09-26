package org.gce.racehub.race.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/** One session of a race weekend. [start] is null when the time is unknown. */
data class ScheduledSession(
    /** Normalised label, e.g. "PRACTICE 1", "QUALIFYING", "SPRINT". */
    val label: String,
    val start: Instant?
)

/**
 * Interprets a race timestamp. The API sends zone-less values such as
 * `2026-05-24T20:00`, which are UTC by contract; values carrying `Z` or an
 * offset keep it. Returns null for anything unparseable.
 */
fun parseRaceInstant(dateTime: String): Instant? {
    val s = dateTime.trim().replaceFirst(' ', 'T')
    if (s.isEmpty()) return null
    runCatching { return Instant.parse(s) }
    runCatching { return LocalDateTime.parse(s).toInstant(TimeZone.UTC) }
    runCatching { return LocalDate.parse(s).atStartOfDayIn(TimeZone.UTC) }
    return null
}

/** The race to feature next: the first, by round, that is not completed. */
fun List<Race>.nextRace(): Race? = sortedBy { it.round }.firstOrNull { !it.isCompleted }

/**
 * The weekend schedule for [race]: the real sessions from [detail] when the API
 * supplied them; otherwise a standard weekend estimated from the race start
 * (FP1/FP2 two days before, FP3/qualifying the day before); otherwise the
 * standard sessions with unknown times.
 */
fun weekendSchedule(race: Race?, detail: RaceDetail?): List<ScheduledSession> {
    val real = detail?.sessions.orEmpty()
    if (real.isNotEmpty()) {
        return real.map { ScheduledSession(normalizeSessionLabel(it.label), parseRaceInstant(it.dateTime)) }
    }
    val raceStart = race?.let { parseRaceInstant(it.dateTime) }
    return STANDARD_WEEKEND.map { (label, daysBefore, hoursBefore) ->
        val start = raceStart?.let { it.minus(daysBefore, DateTimeUnit.DAY, TimeZone.UTC) - hoursBefore.hours }
        ScheduledSession(label, start)
    }
}

/** Maps the API's many spellings ("FP1", "Practice 1", "Q", …) onto one label. */
fun normalizeSessionLabel(raw: String): String = when (raw.uppercase().trim()) {
    "FP1", "PRACTICE 1", "P1", "PRACTICE1" -> "PRACTICE 1"
    "FP2", "PRACTICE 2", "P2", "PRACTICE2" -> "PRACTICE 2"
    "FP3", "PRACTICE 3", "P3", "PRACTICE3" -> "PRACTICE 3"
    "QUAL", "QUALIFYING", "Q" -> "QUALIFYING"
    "RACE", "GRAND PRIX" -> "RACE"
    "SPRINT QUALIFYING", "SPRINT QUAL", "SQ" -> "SPRINT QUAL"
    "SPRINT" -> "SPRINT"
    else -> raw.uppercase().trim()
}

/** (label, days before the race, extra hours before) for a standard weekend. */
private val STANDARD_WEEKEND = listOf(
    Triple("PRACTICE 1", 2, 3),
    Triple("PRACTICE 2", 2, 0),
    Triple("PRACTICE 3", 1, 3),
    Triple("QUALIFYING", 1, 0),
    Triple("RACE", 0, 0),
)
