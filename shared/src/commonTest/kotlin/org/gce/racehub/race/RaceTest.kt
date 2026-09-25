package org.gce.racehub.race

import org.gce.racehub.race.domain.model.Race
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RaceTest {

    private fun race(status: String) = Race(
        id = "r1",
        name = "Test GP",
        circuit = "Test Circuit",
        country = "Testland",
        city = "Testville",
        dateTime = "2025-03-16T15:00:00Z",
        round = 1,
        status = status
    )

    @Test
    fun `isCompleted is true for COMPLETED status`() {
        assertTrue(race("COMPLETED").isCompleted)
    }

    @Test
    fun `isCompleted is case-insensitive`() {
        assertTrue(race("completed").isCompleted)
        assertTrue(race("Completed").isCompleted)
    }

    @Test
    fun `isCompleted is false for UPCOMING status`() {
        assertFalse(race("UPCOMING").isCompleted)
    }

    @Test
    fun `isCompleted is false for LIVE status`() {
        assertFalse(race("LIVE").isCompleted)
    }
}
