package org.gce.racehub.auth

import kotlinx.coroutines.test.runTest
import org.gce.racehub.auth.domain.model.User
import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.auth.domain.usecase.LogoutUseCase
import org.gce.racehub.fake.FakeAuthRepository
import org.gce.racehub.fake.FakeSessionStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class LogoutUseCaseTest {

    private val repository = FakeAuthRepository()

    private fun makeUseCase(user: User?): Pair<LogoutUseCase, UserSession> {
        val session = UserSession(FakeSessionStorage(initialUser = user))
        return LogoutUseCase(repository, session) to session
    }

    @Test
    fun `no session user clears local state and returns true without network call`() = runTest {
        val (useCase, session) = makeUseCase(user = null)
        val result = useCase()
        assertTrue(result)
        assertEquals(0, repository.logoutCallCount)
        assertNull(session.currentUser.value)
    }

    @Test
    fun `user with blank token clears local state and returns true without network call`() = runTest {
        val user = User("1", "a@b.com", "Alice", token = "")
        val (useCase, session) = makeUseCase(user)
        val result = useCase()
        assertTrue(result)
        assertEquals(0, repository.logoutCallCount)
        assertNull(session.currentUser.value)
    }

    @Test
    fun `user with valid token calls repository with token`() = runTest {
        val user = User("1", "a@b.com", "Alice", token = "my-token")
        val (useCase, _) = makeUseCase(user)
        useCase()
        assertEquals(1, repository.logoutCallCount)
        assertEquals("my-token", repository.lastLogoutToken)
    }

    @Test
    fun `successful server logout clears the session`() = runTest {
        val user = User("1", "a@b.com", "Alice", token = "my-token")
        val (useCase, session) = makeUseCase(user)
        repository.logoutResult = true
        val result = useCase()
        assertTrue(result)
        assertNull(session.currentUser.value)
    }

    @Test
    fun `failed server logout keeps the session intact`() = runTest {
        val user = User("1", "a@b.com", "Alice", token = "my-token")
        val (useCase, session) = makeUseCase(user)
        repository.logoutResult = false
        val result = useCase()
        assertFalse(result)
        assertEquals(user, session.currentUser.value)
    }
}
