package org.gce.racehub.auth

import kotlinx.coroutines.test.runTest
import org.gce.racehub.auth.data.repository.AuthRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {

    private val repository = AuthRepositoryImpl()

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    fun `login with correct credentials returns success`() = runTest {
        val result = repository.login("driver@racehub.com", "race123")
        assertTrue(result.isSuccess)
        assertNotNull(result.user)
        assertEquals("driver@racehub.com", result.user!!.email)
    }

    @Test
    fun `login with wrong password returns failure`() = runTest {
        val result = repository.login("driver@racehub.com", "wrongpassword")
        assertFalse(result.isSuccess)
        assertEquals("Invalid email or password", result.error)
        assertNull(result.user)
    }

    @Test
    fun `login with unknown email returns failure`() = runTest {
        val result = repository.login("unknown@test.com", "race123")
        assertFalse(result.isSuccess)
        assertEquals("Invalid email or password", result.error)
    }

    @Test
    fun `login with both wrong email and password returns failure`() = runTest {
        val result = repository.login("wrong@wrong.com", "badpass")
        assertFalse(result.isSuccess)
    }

    @Test
    fun `login success returns user with id and name`() = runTest {
        val result = repository.login("driver@racehub.com", "race123")
        val user = result.user!!
        assertEquals("1", user.id)
        assertEquals("Race Driver", user.name)
    }

    // ── signUp ────────────────────────────────────────────────────────────────

    @Test
    fun `signUp always returns success`() = runTest {
        val result = repository.signUp("Alice", "alice@test.com", "secret1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `signUp result carries provided name and email`() = runTest {
        val result = repository.signUp("Bob", "bob@test.com", "password")
        val user = result.user!!
        assertEquals("Bob", user.name)
        assertEquals("bob@test.com", user.email)
    }

    @Test
    fun `signUp result has non-blank id`() = runTest {
        val result = repository.signUp("Alice", "a@b.com", "secret1")
        assertTrue(result.user!!.id.isNotBlank())
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    fun `logout always returns true`() = runTest {
        assertTrue(repository.logout("any-token"))
    }

    @Test
    fun `logout with empty token still returns true`() = runTest {
        assertTrue(repository.logout(""))
    }
}
