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

class GetMyProfileUseCaseTest {

    private val repository = FakeProfileRepository()
    private val useCase = GetMyProfileUseCase(repository)

    @Test
    fun `blank userId is rejected without calling the repository`() = runTest {
        assertIs<DataError.InvalidInput>(useCase(userId = "", token = "tok").error)
        assertNull(repository.lastArgs)
    }

    @Test
    fun `blank token is rejected`() = runTest {
        assertIs<DataError.InvalidInput>(useCase(userId = "u1", token = "").error)
    }

    @Test
    fun `valid args return repository result`() = runTest {
        val profile = UserProfile("alice", "a@b.com", "A", 5, 3, listOf("Thread 1"), listOf("Saved 1"))
        repository.profileResult = DataResult.success(profile)
        assertEquals(profile, useCase(userId = "u1", token = "tok").data)
        assertEquals("u1" to "tok", repository.lastArgs)
    }
}
