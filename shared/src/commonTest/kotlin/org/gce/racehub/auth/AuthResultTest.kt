package org.gce.racehub.auth

import org.gce.racehub.auth.domain.model.AuthResult
import org.gce.racehub.auth.domain.model.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthResultTest {

    private val user = User(id = "1", email = "a@b.com", name = "Alice", token = "tok")

    @Test
    fun `success has isSuccess true and carries the user`() {
        val result = AuthResult.success(user)
        assertTrue(result.isSuccess)
        assertEquals(user, result.user)
        assertNull(result.error)
    }

    @Test
    fun `failure has isSuccess false and carries the error message`() {
        val result = AuthResult.failure("Something went wrong")
        assertFalse(result.isSuccess)
        assertEquals("Something went wrong", result.error)
        assertNull(result.user)
    }

    @Test
    fun `success user is the exact instance provided`() {
        val result = AuthResult.success(user)
        assertNotNull(result.user)
        assertEquals("1", result.user.id)
        assertEquals("tok", result.user.token)
    }
}
