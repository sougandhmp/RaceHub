package org.gce.racehub.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
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

    private const val CONNECT_TIMEOUT_MS = 10_000L
    private const val REQUEST_TIMEOUT_MS = 15_000L

    /**
     * Creates the Ktor client used against the RaceHub API: JSON and timeouts on
     * the platform engine, plus header-redacted logging when [logNetwork] is set.
     */
    fun create(logNetwork: Boolean): HttpClient = create(getHttpClientEngine(), logNetwork)

    /** Same configuration on a given [engine]; tests pass a `MockEngine`. */
    fun create(engine: HttpClientEngine, logNetwork: Boolean = false): HttpClient {
        return HttpClient(engine) {
            // Release builds log nothing: even URLs can identify users and threads.
            if (logNetwork) install(Logging) {
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
