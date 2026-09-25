package org.gce.racehub.core

import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SafeCallTest {

    @Test
    fun `success wraps the value`() = runTest {
        val result = safeCall("test") { 42 }
        assertTrue(result.isSuccess)
        assertEquals(42, result.data)
    }

    @Test
    fun `IOException becomes NoConnection`() = runTest {
        assertEquals(DataError.NoConnection, safeCall<Int>("test") { throw IOException("offline") }.error)
    }

    @Test
    fun `request timeout becomes Timeout`() = runTest {
        val result = safeCall<Int>("test") { throw HttpRequestTimeoutException("https://example.com/graphql", 15_000) }
        assertEquals(DataError.Timeout, result.error)
    }

    @Test
    fun `GraphQL error becomes Server with the server's message`() = runTest {
        val error = safeCall<Int>("test") { throw GraphQLException("Thread not found") }.error
        assertIs<DataError.Server>(error)
        assertEquals("Thread not found", error.message)
    }

    @Test
    fun `IllegalArgumentException becomes InvalidInput`() = runTest {
        assertIs<DataError.InvalidInput>(safeCall<Int>("test") { throw IllegalArgumentException("bad") }.error)
    }

    @Test
    fun `malformed response becomes Unknown`() = runTest {
        assertIs<DataError.Unknown>(safeCall<Int>("test") { throw SerializationException("bad json") }.error)
    }

    @Test
    fun `cancellation is rethrown instead of reported as an error`() = runTest {
        assertFailsWith<CancellationException> {
            safeCall<Int>("test") { throw CancellationException("screen closed") }
        }
    }

    @Test
    fun `Server error with a blank message uses a friendly fallback`() {
        assertEquals("Something went wrong on the server.", DataError.Server("  ").message)
    }
}
