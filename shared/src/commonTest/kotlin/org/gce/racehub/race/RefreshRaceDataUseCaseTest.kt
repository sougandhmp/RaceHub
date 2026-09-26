package org.gce.racehub.race

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.gce.racehub.core.DataError
import org.gce.racehub.core.DataResult
import org.gce.racehub.fake.*
import org.gce.racehub.race.domain.model.*
import org.gce.racehub.race.domain.usecase.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RefreshRaceDataUseCaseTest {

    private val repository = FakeRaceRepository()
    private val useCase = RefreshRaceDataUseCase(repository)

    @Test
    fun `refreshes the calendar and the dashboard once each`() = runTest {
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(1, repository.scheduleRefreshCount)
        assertEquals(1, repository.dashboardRefreshCount)
    }

    @Test
    fun `returns the calendar failure`() = runTest {
        repository.scheduleRefreshResult = DataResult.failure(DataError.NoConnection)
        val result = useCase()
        assertFalse(result.isSuccess)
        assertEquals(DataError.NoConnection, result.error)
    }

    @Test
    fun `returns the dashboard failure and still refreshes the calendar`() = runTest {
        repository.dashboardRefreshResult = DataResult.failure(DataError.Timeout)
        val result = useCase()
        assertEquals(DataError.Timeout, result.error)
        assertEquals(1, repository.scheduleRefreshCount)
    }
}
