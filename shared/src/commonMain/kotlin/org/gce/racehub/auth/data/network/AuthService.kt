package org.gce.racehub.auth.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.gce.racehub.auth.data.dto.LoginRequestDto
import org.gce.racehub.auth.data.dto.LoginResponseDto

/**
 * Network service for authentication API calls.
 *
 * Handles HTTP communication with the RaceHub authentication endpoints.
 * Errors during network communication are thrown as exceptions and should
 * be handled by the calling [AuthRepository] implementation.
 */
class AuthService(private val httpClient: HttpClient, private val baseUrl: String) {

    /**
     * Calls the login endpoint with email and password credentials.
     *
     * @param email The user's email address
     * @param password The user's password
     * @return [LoginResponseDto] containing the authentication token and user details
     * @throws Exception if the network request fails
     */
    suspend fun login(email: String, password: String): LoginResponseDto {
        val request = LoginRequestDto(email = email, password = password)

        return httpClient.post("$baseUrl/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

