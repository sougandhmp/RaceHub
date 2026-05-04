package org.gce.racehub.race.domain.model

/**
 * Represents a single row in the Drivers' Championship standings table.
 *
 * Shared between Android and iOS via the KMP `shared` module. All fields
 * are plain primitives or [String] so they bridge cleanly to Swift/Obj-C.
 */
data class DriverStanding(

    /** Current championship position (1 = leader). */
    val position: Int,

    /** Driver's full name, e.g. "Max Verstappen". */
    val driverName: String,

    /** Constructor/team name, e.g. "Red Bull Racing". */
    val team: String,

    /** Accumulated championship points this season. */
    val points: Int,

    /** Unicode flag emoji for the driver's nationality, e.g. "🇳🇱". */
    val flag: String
)
