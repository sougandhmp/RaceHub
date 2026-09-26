package org.gce.racehub.core

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SingleFlightTest {

    @Test
    fun `concurrent callers share one run`() = runTest {
        val singleFlight = SingleFlight<Int>(backgroundScope)
        val gate = CompletableDeferred<Unit>()
        var runs = 0
        val block: suspend () -> Int = { runs++; gate.await(); 7 }

        val first = async { singleFlight.run(block) }
        val second = async { singleFlight.run(block) }
        val third = async { singleFlight.run(block) }
        advanceUntilIdle()
        gate.complete(Unit)

        assertEquals(listOf(7, 7, 7), listOf(first.await(), second.await(), third.await()))
        assertEquals(1, runs)
    }

    @Test
    fun `a call after the previous run finished starts a new run`() = runTest {
        val singleFlight = SingleFlight<Int>(backgroundScope)
        var runs = 0

        singleFlight.run { ++runs }
        singleFlight.run { ++runs }

        assertEquals(2, runs)
    }

    @Test
    fun `cancelling one caller doesn't cancel the shared run for the others`() = runTest {
        val singleFlight = SingleFlight<Int>(backgroundScope)
        val gate = CompletableDeferred<Unit>()
        val block: suspend () -> Int = { gate.await(); 7 }

        val leaving = launch { singleFlight.run(block) }
        val staying = async { singleFlight.run(block) }
        advanceUntilIdle()
        leaving.cancel()
        gate.complete(Unit)

        assertEquals(7, staying.await())
    }
}
