package org.gce.racehub.auth.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Android implementation of the HTTP client engine using OkHttp.
 */
actual fun getHttpClientEngine(): HttpClientEngine = OkHttp.create()

