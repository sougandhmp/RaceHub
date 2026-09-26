package org.gce.racehub.auth.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for creating and configuring the Ktor HTTP client.
 *
 * Provides platform-agnostic HTTP client configuration, with
 * platform-specific engine selection handled via expect/actual.
 */
internal object HttpClientFactory {

    /**
     * Creates a Ktor HTTP client configured for JSON serialization
     * and the RaceHub API base URL.
     *
     * @param baseUrl The API base URL (e.g., "https://api.example.com")
     * @return A configured [HttpClient] instance
     */
    private const val CONNECT_TIMEOUT_MS = 10_000L
    private const val REQUEST_TIMEOUT_MS = 15_000L

    fun create(baseUrl: String): HttpClient {
        return HttpClient(getHttpClientEngine()) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("🚀 NETWORK LOG | $message")
                    }
                }
                    // SECURITY: never log request/response bodies or headers — they carry
                // login passwords, password-reset OTPs and Authorization bearer tokens.
                // INFO logs only method, URL, status and timing.
                level = LogLevel.INFO
                // Defense-in-depth: redact the auth token if the level is ever raised.
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
            // Without this a stalled request hangs forever. Timeouts surface as
            // HttpRequestTimeoutException (an IOException), i.e. DataError.Network.
            install(HttpTimeout) {
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
                socketTimeoutMillis = REQUEST_TIMEOUT_MS
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                    isLenient = true
                })
            }
        }
    }
}

/**
 * Platform-specific HTTP client engine selection.
 *
 * Implemented separately for Android (OkHttp) and iOS (Darwin).
 */
internal expect fun getHttpClientEngine(): io.ktor.client.engine.HttpClientEngine
