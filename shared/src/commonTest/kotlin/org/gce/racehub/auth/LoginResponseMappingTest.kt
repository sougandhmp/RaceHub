package org.gce.racehub.auth

import kotlinx.serialization.json.Json
import org.gce.racehub.auth.data.dto.LoginResponseDto
import org.gce.racehub.auth.data.dto.toDomainModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginResponseMappingTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun userJson(emailVerifiedField: String): String = """
        {
          "success": true,
          "message": "ok",
          "data": {
            "token": "tok",
            "user": {
              "id": "1",
              "username": "racer",
              "email": "racer@test.com",
              "country": "AU",
              "avatar": "RR",
              "role": "member",
              "joinedAt": "2026-01-01",
              "postsCount": 3$emailVerifiedField
            }
          }
        }
    """.trimIndent()

    @Test
    fun `emailVerified false maps to unverified user`() {
        val dto = json.decodeFromString<LoginResponseDto>(userJson(", \"emailVerified\": false"))
        val user = dto.toDomainModel()
        assertFalse(user!!.isEmailVerified)
    }

    @Test
    fun `emailVerified true maps to verified user`() {
        val dto = json.decodeFromString<LoginResponseDto>(userJson(", \"emailVerified\": true"))
        val user = dto.toDomainModel()
        assertTrue(user!!.isEmailVerified)
    }

    @Test
    fun `missing emailVerified defaults to verified so login is never blocked spuriously`() {
        val dto = json.decodeFromString<LoginResponseDto>(userJson(""))
        val user = dto.toDomainModel()
        assertTrue(user!!.isEmailVerified)
        assertEquals("racer@test.com", user.email)
    }
}
