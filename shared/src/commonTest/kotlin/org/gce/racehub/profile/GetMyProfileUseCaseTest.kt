package org.gce.racehub.profile

import kotlinx.coroutines.test.runTest
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.fake.FakeProfileRepository
import org.gce.racehub.profile.domain.model.UserProfile
import org.gce.racehub.profile.domain.usecase.GetMyProfileUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetMyProfileUseCaseTest {

    private val repository = FakeProfileRepository()
    private val useCase = GetMyProfileUseCase(repository)

    @Test
    fun `blank userId throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(userId = "", token = "tok")
        }
    }

    @Test
    fun `blank token throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(userId = "u1", token = "")
        }
    }

    @Test
    fun `valid args return repository result`() = runTest {
        val profile = UserProfile("alice", "a@b.com", "A", 5, 3, listOf("Thread 1"), listOf("Saved 1"))
        repository.profileResult = profile
        val result = useCase(userId = "u1", token = "tok")
        assertEquals(DataResult.Success(profile), result)
    }
}
