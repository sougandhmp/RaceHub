package org.gce.racehub.auth

import org.gce.racehub.auth.domain.model.User
import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.fake.FakeSessionStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserSessionTest {

    private val userWithToken = User(id = "1", email = "a@b.com", name = "Alice", token = "tok", username = "alice")
    private val userNameOnly = User(id = "2", email = "b@c.com", name = "Bob", token = "tok2")

    @Test
    fun `currentUser starts null when storage is empty`() {
        val session = UserSession(FakeSessionStorage())
        assertNull(session.currentUser.value)
    }

    @Test
    fun `currentUser restored from storage on construction`() {
        val session = UserSession(FakeSessionStorage(initialUser = userWithToken))
        assertEquals(userWithToken, session.currentUser.value)
    }

    @Test
    fun `setUser updates currentUser`() {
        val session = UserSession(FakeSessionStorage())
        session.setUser(userWithToken)
        assertEquals(userWithToken, session.currentUser.value)
    }

    @Test
    fun `setUser persists user to storage`() {
        val storage = FakeSessionStorage()
        val session = UserSession(storage)
        session.setUser(userWithToken)
        assertEquals(userWithToken, storage.restoreUser())
    }

    @Test
    fun `clear sets currentUser to null`() {
        val session = UserSession(FakeSessionStorage(initialUser = userWithToken))
        session.clear()
        assertNull(session.currentUser.value)
    }

    @Test
    fun `clear wipes storage`() {
        val storage = FakeSessionStorage(initialUser = userWithToken)
        val session = UserSession(storage)
        session.clear()
        assertNull(storage.restoreUser())
    }

    @Test
    fun `userId returns id when signed in`() {
        val session = UserSession(FakeSessionStorage(initialUser = userWithToken))
        assertEquals("1", session.userId)
    }

    @Test
    fun `userId returns null when signed out`() {
        val session = UserSession(FakeSessionStorage())
        assertNull(session.userId)
    }

    @Test
    fun `username returns username field when set`() {
        val session = UserSession(FakeSessionStorage(initialUser = userWithToken))
        assertEquals("alice", session.username)
    }

    @Test
    fun `username falls back to name when username field is null`() {
        val session = UserSession(FakeSessionStorage(initialUser = userNameOnly))
        assertEquals("Bob", session.username)
    }

    @Test
    fun `username returns null when signed out`() {
        val session = UserSession(FakeSessionStorage())
        assertNull(session.username)
    }
}
