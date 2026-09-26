package org.gce.racehub.core.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

/**
 * iOS implementation of the HTTP client engine using Darwin (native URLSession).
 */
internal actual fun getHttpClientEngine(): HttpClientEngine = Darwin.create()

