package org.gce.racehub.race.domain.model

/**
 * One row in the Drivers' Championship table.
 *
 * Shape mirrors the GraphQL `DriverStanding` type: `position`, `name`, `team`,
 * `points`, `wins`. All fields are primitives or [String] so they bridge
 * cleanly to Swift via the KMP `shared` framework.
 */
data class DriverStanding(

    /** Current championship position (1 = leader). */
    val position: Int,

    /** Driver's display name, e.g. "Max Verstappen". */
    val driverName: String,

    /** Constructor/team name, e.g. "Red Bull Racing". */
    val team: String,

    /** Accumulated championship points this season. */
    val points: Int,

    /** Race wins this season. */
    val wins: Int
)
