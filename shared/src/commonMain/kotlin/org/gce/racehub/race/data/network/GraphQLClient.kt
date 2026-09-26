package org.gce.racehub.race.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.gce.racehub.core.GraphQLException
import org.gce.racehub.race.data.dto.GraphQLResponse

/**
 * Sends GraphQL requests to `$baseUrl/graphql`.
 *
 * Returns the response's `data`, or throws [GraphQLException] with the server's error
 * messages when there is none. Repositories wrap calls in `safeCall`, which turns that
 * (and any network error) into a `DataResult` failure.
 */
class GraphQLClient(val httpClient: HttpClient, val baseUrl: String) {

    suspend inline fun <reified Body : Any, reified Data : Any> execute(
        body: Body,
        fallbackError: String,
        authToken: String? = null
    ): Data {
        val response: GraphQLResponse<Data> = httpClient.post("$baseUrl/graphql") {
            contentType(ContentType.Application.Json)
            if (authToken != null) header(HttpHeaders.Authorization, "Bearer $authToken")
            setBody(body)
        }.body()
        return response.data ?: throw GraphQLException(
            response.errors?.joinToString { it.message }?.takeIf { it.isNotBlank() } ?: fallbackError
        )
    }
}
