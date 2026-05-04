package org.gce.racehub.race.domain.model

/**
 * Represents a single race event on the Formula 1 calendar.
 *
 * Shared between Android and iOS via the KMP `shared` module. All fields
 * are plain primitives or [String] so they bridge cleanly to Swift/Obj-C.
 */
data class Race(

    /** Unique identifier for this race event. */
    val id: String,

    /** Full official name, e.g. "Australian Grand Prix". */
    val name: String,

    /** Name of the circuit, e.g. "Albert Park Circuit". */
    val circuit: String,

    /** Country or city where the race is held, e.g. "Australia". */
    val country: String,

    /** Unicode flag emoji for the host country, e.g. "🇦🇺". */
    val countryFlag: String,

    /** Human-readable date string, e.g. "Mar 16, 2025". */
    val date: String,

    /** Position in the season calendar (1-indexed). */
    val round: Int,

    /** True once the race has been run; false for future events. */
    val isCompleted: Boolean
)
