package org.gce.racehub.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DataResultTest {

    @Test
    fun `success has data and no error`() {
        val result = DataResult.success("value")
        assertTrue(result.isSuccess)
        assertEquals("value", result.data)
        assertNull(result.error)
    }

    @Test
    fun `failure has an error and no data`() {
        val result = DataResult.failure<String>(DataError.Timeout)
        assertFalse(result.isSuccess)
        assertNull(result.data)
        assertEquals(DataError.Timeout, result.error)
    }

    @Test
    fun `map transforms success and passes failure through`() {
        assertEquals(4, DataResult.success(2).map { it * 2 }.data)
        assertEquals(DataError.NoConnection, DataResult.failure<Int>(DataError.NoConnection).map { it * 2 }.error)
    }
}
