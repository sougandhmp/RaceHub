package org.gce.racehub.auth

import org.gce.racehub.auth.data.dto.LoginDataDto
import org.gce.racehub.auth.data.dto.LoginResponseDto
import org.gce.racehub.auth.data.dto.UserResponseDto
import org.gce.racehub.auth.data.dto.toDomainModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DtoMappersTest {

    private val userDto = UserResponseDto(
        id = "u1",
        username = "alice",
        email = "alice@test.com",
        country = "GB",
        avatar = "A",
        role = "member",
        joinedAt = "2024-01-01",
        postsCount = 7
    )

    // ── UserResponseDto.toDomainModel ─────────────────────────────────────────

    @Test
    fun `UserResponseDto maps all fields to User`() {
        val user = userDto.toDomainModel(token = "tok123")
        assertEquals("u1", user.id)
        assertEquals("alice@test.com", user.email)
        assertEquals("alice", user.name)
        assertEquals("tok123", user.token)
        assertEquals("alice", user.username)
        assertEquals("GB", user.country)
        assertEquals("A", user.avatar)
        assertEquals("member", user.role)
        assertEquals("2024-01-01", user.joinedAt)
        assertEquals(7, user.postsCount)
    }

    @Test
    fun `UserResponseDto uses username as both name and username fields`() {
        val user = userDto.toDomainModel(token = "tok")
        assertEquals(user.username, user.name)
    }

    @Test
    fun `UserResponseDto maps with empty token`() {
        val user = userDto.toDomainModel(token = "")
        assertEquals("", user.token)
    }

    // ── LoginResponseDto.toDomainModel ────────────────────────────────────────

    @Test
    fun `LoginResponseDto with data maps to User`() {
        val response = LoginResponseDto(
            success = true,
            message = "OK",
            data = LoginDataDto(token = "tok456", user = userDto)
        )
        val user = response.toDomainModel()
        assertEquals("u1", user?.id)
        assertEquals("tok456", user?.token)
        assertEquals("alice", user?.username)
    }

    @Test
    fun `LoginResponseDto with null data returns null`() {
        val response = LoginResponseDto(success = false, message = "Unauthorized", data = null)
        assertNull(response.toDomainModel())
    }

    @Test
    fun `LoginResponseDto token is passed through to User`() {
        val response = LoginResponseDto(
            success = true,
            message = "OK",
            data = LoginDataDto(token = "secret-jwt", user = userDto)
        )
        assertEquals("secret-jwt", response.toDomainModel()?.token)
    }
}
