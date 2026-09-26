package org.gce.racehub.race.domain.model

/**
 * Represents a single race event on the Formula 1 calendar.
 *
 * Shared between Android and iOS via the KMP `shared` module. All fields
 * are plain primitives or [String] so they bridge cleanly to Swift/Obj-C.
 */
data class Race(

    /** Unique identifier (slug) for this race event. */
    val id: String,

    /** Full official name, e.g. "Australian Grand Prix". */
    val name: String,

    /** Name of the circuit, e.g. "Albert Park Circuit". */
    val circuit: String,

    /** Country where the race is held, e.g. "Australia". */
    val country: String,

    /** City where the race is held, e.g. "Melbourne". */
    val city: String,

    /** ISO datetime string for the race start, e.g. "2025-03-16T05:00:00Z". */
    val dateTime: String,

    /** Position in the season calendar (1-indexed). */
    val round: Int,

    /** Race status from the API, e.g. "COMPLETED", "UPCOMING", "LIVE". */
    val status: String,

    /** Weather condition at race time, e.g. "Sunny", "Wet". Null if unknown. */
    val weather: String? = null
) {
    val isCompleted: Boolean get() = status.equals("COMPLETED", ignoreCase = true)
}
