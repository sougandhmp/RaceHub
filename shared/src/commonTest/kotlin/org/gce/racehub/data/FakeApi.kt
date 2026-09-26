package org.gce.racehub.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.io.IOException
import org.gce.racehub.auth.data.network.HttpClientFactory

/**
 * A scriptable fake of the RaceHub API on Ktor's MockEngine, wired through the
 * production [HttpClientFactory] so JSON and timeout config are the real ones.
 * Tests register a response per GraphQL operation or REST path and can make
 * any of them fail; every request is recorded.
 */
internal class FakeApi(dispatcher: CoroutineDispatcher) {

    /** Keyed by GraphQL operation name (e.g. "GetRaces") or REST path. */
    val responses = mutableMapOf<String, String>()
    val failures = mutableMapOf<String, Failure>()
    val requests = mutableListOf<String>()

    enum class Failure { Offline, ServerError }

    val client: HttpClient = HttpClientFactory.create(
        MockEngine.create {
            this.dispatcher = dispatcher
            addHandler { request -> handle(request) }
        }
    )

    fun count(key: String) = requests.count { it == key }

    private suspend fun MockRequestHandleScope.handle(request: HttpRequestData): HttpResponseData {
        val path = request.url.encodedPath
        val key = if (path.endsWith("/graphql")) operationName(request.body.toByteArray().decodeToString()) else path
        requests += key
        return when (failures[key]) {
            Failure.Offline -> throw IOException("offline")
            Failure.ServerError -> respondError(HttpStatusCode.InternalServerError, "boom")
            null -> respond(
                content = responses[key] ?: error("FakeApi: no response registered for $key"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    }

    /** "query GetRaces {" / "mutation AddComment(" -> "GetRaces" / "AddComment". */
    private fun operationName(body: String): String =
        Regex("""(?:query|mutation)\s+(\w+)""").find(body)?.groupValues?.get(1) ?: "anonymous"
}
