package org.gce.racehub.race.domain.repository

import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race

/**
 * Contract for fetching race and standings data.
 *
 * Two access styles are intentionally provided:
 * - **List-based** (`getRaceSchedule`, `getDriverStandings`) — idiomatic for
 *   Kotlin/Android; used by the use cases.
 * - **Index-based** (`getRaceCount`, `getRace`, …) — Swift-friendly; Kotlin
 *   `List<T>` does not bridge to a Swift `[T]` array directly in Kotlin/Native,
 *   so iOS calls these accessors instead.
 */
interface HomeRepository {

    /** Returns the full race calendar for the current season. */
    fun getRaceSchedule(): List<Race>

    /** Returns the current Drivers' Championship standings table. */
    fun getDriverStandings(): List<DriverStanding>

    // ── Swift-friendly index-based accessors ─────────────────────────────────

    /** Number of races in the calendar; use with [getRace] from Swift. */
    fun getRaceCount(): Int

    /**
     * Returns the race at [index] in the calendar.
     * @throws IndexOutOfBoundsException if [index] is out of range.
     */
    fun getRace(index: Int): Race

    /** Number of entries in the standings; use with [getStanding] from Swift. */
    fun getStandingCount(): Int

    /**
     * Returns the standings entry at [index].
     * @throws IndexOutOfBoundsException if [index] is out of range.
     */
    fun getStanding(index: Int): DriverStanding
}
