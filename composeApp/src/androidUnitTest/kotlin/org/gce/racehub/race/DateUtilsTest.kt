package org.gce.racehub.race

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateUtilsTest {

    private fun utc(dateTime: String): Long =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .parse(dateTime)!!.time

    @Test
    fun `minute precision timestamp from the API keeps its time of day`() {
        assertEquals(utc("2026-05-24 20:00:00"), parseIsoToDate("2026-05-24T20:00")?.time)
    }

    @Test
    fun `seconds precision timestamp is parsed as UTC`() {
        assertEquals(utc("2026-05-24 20:00:30"), parseIsoToDate("2026-05-24T20:00:30")?.time)
    }

    @Test
    fun `zulu timestamp is parsed as UTC`() {
        assertEquals(utc("2025-03-16 05:00:00"), parseIsoToDate("2025-03-16T05:00:00Z")?.time)
    }

    @Test
    fun `offset is honoured rather than dropped`() {
        assertEquals(utc("2026-05-24 18:00:00"), parseIsoToDate("2026-05-24T20:00:00.000+02:00")?.time)
    }

    @Test
    fun `date only is midnight UTC`() {
        assertEquals(utc("2026-05-24 00:00:00"), parseIsoToDate("2026-05-24")?.time)
    }

    @Test
    fun `garbage returns null`() {
        assertNull(parseIsoToDate("not a date"))
        assertNull(parseIsoToDate(""))
    }

    @Test
    fun `formatRaceDate shows the race start time`() {
        assertEquals("24 May 2026 · 20:00 UTC", formatRaceDate("2026-05-24T20:00"))
    }
}
