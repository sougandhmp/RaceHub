package org.gce.racehub.core.data

import kotlinx.serialization.Serializable

// GraphQL wire types shared by every feature's network calls.

@Serializable
internal data class GraphQLResponse<T>(
    val data: T? = null,
    val errors: List<GraphQLError>? = null
)

@Serializable
internal data class GraphQLError(
    val message: String
)

@Serializable
internal data class GraphQLRequest(
    val query: String
)
