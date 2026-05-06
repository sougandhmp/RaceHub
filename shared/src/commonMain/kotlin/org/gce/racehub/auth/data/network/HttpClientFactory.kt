package org.gce.racehub.auth.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for creating and configuring the Ktor HTTP client.
 *
 * Provides platform-agnostic HTTP client configuration, with
 * platform-specific engine selection handled via expect/actual.
 */
object HttpClientFactory {

    /**
     * Creates a Ktor HTTP client configured for JSON serialization
     * and the RaceHub API base URL.
     *
     * @param baseUrl The API base URL (e.g., "https://api.example.com")
     * @return A configured [HttpClient] instance
     */
    fun create(baseUrl: String): HttpClient {
        return HttpClient(getHttpClientEngine()) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("HTTP: $message")
                    }
                }
                level = LogLevel.ALL
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
expect fun getHttpClientEngine(): io.ktor.client.engine.HttpClientEngine
