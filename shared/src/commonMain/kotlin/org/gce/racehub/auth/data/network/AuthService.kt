package org.gce.racehub.auth.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.gce.racehub.auth.data.dto.LoginRequestDto
import org.gce.racehub.auth.data.dto.LoginResponseDto
import org.gce.racehub.auth.data.dto.OtpResponseDto
import org.gce.racehub.auth.data.dto.OtpSendRequestDto
import org.gce.racehub.auth.data.dto.OtpVerifyRequestDto
import org.gce.racehub.auth.data.dto.LogoutResponseDto
import org.gce.racehub.auth.data.dto.PasswordResetConfirmDto
import org.gce.racehub.auth.data.dto.PasswordResetRequestDto
import org.gce.racehub.auth.data.dto.PasswordResetResponseDto
import org.gce.racehub.auth.data.dto.SignUpRequestDto

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

    suspend fun signUp(username: String, email: String, password: String, country: String): LoginResponseDto {
        return httpClient.post("$baseUrl/api/v1/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(SignUpRequestDto(username, email, password, country))
        }.body()
    }

    suspend fun logout(token: String): LogoutResponseDto {
        return httpClient.post("$baseUrl/api/v1/auth/logout") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun requestPasswordReset(email: String): PasswordResetResponseDto {
        return httpClient.post("$baseUrl/api/v1/auth/password-reset/request") {
            contentType(ContentType.Application.Json)
            setBody(PasswordResetRequestDto(email))
        }.body()
    }

    suspend fun confirmPasswordReset(
        email: String,
        otp: String,
        newPassword: String
    ): PasswordResetResponseDto {
        return httpClient.post("$baseUrl/api/v1/auth/password-reset/confirm") {
            contentType(ContentType.Application.Json)
            setBody(PasswordResetConfirmDto(email, otp, newPassword))
        }.body()
    }

    suspend fun sendOtp(email: String, subject: String): OtpResponseDto {
        return httpClient.post("$baseUrl/api/v1/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody(OtpSendRequestDto(email, subject))
        }.body()
    }

    suspend fun resendOtp(email: String, subject: String): OtpResponseDto {
        return httpClient.post("$baseUrl/api/v1/auth/otp/resend") {
            contentType(ContentType.Application.Json)
            setBody(OtpSendRequestDto(email, subject))
        }.body()
    }

    suspend fun verifyOtp(email: String, otp: String): OtpResponseDto {
        return httpClient.post("$baseUrl/api/v1/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody(OtpVerifyRequestDto(email, otp))
        }.body()
    }
}

