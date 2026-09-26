package org.gce.racehub.profile.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.gce.racehub.core.data.GraphQLResponse
import org.gce.racehub.core.data.safeCall
import org.gce.racehub.core.data.toErrorMessage
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.profile.data.dto.*
import org.gce.racehub.profile.domain.model.UserProfile
import org.gce.racehub.profile.domain.repository.ProfileRepository

/** GraphQL implementation of [ProfileRepository]. */
internal class ProfileRepositoryNetworkImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : ProfileRepository {

    private companion object {
        const val TAG = "ProfileRepository"
    }

    // GraphQL query to fetch the signed-in user's profile
    private val profileQuery = $$"""
        query GetMyProfile($userId: String) {
            me(userId: $userId) {
                username
                email
                avatar
                postsCount
                savedCount
                recentThreads { title }
                savedThreads { title }
            }
        }
    """.trimIndent()

    /**
     * Fetches the signed-in user's profile via GraphQL query.
     * Sends the auth token as an `Authorization: Bearer` header.
     */
    override suspend fun getMyProfile(userId: String, token: String): DataResult<UserProfile> = safeCall(TAG, "load profile") {
        val response: GraphQLResponse<ProfileData> = httpClient.post("$baseUrl/graphql") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(
                GraphQLProfileRequest(
                    query = profileQuery,
                    variables = ProfileVariables(userId = userId)
                )
            )
        }.body()

        val me = response.data?.me
            ?: error(response.errors.toErrorMessage("Failed to load profile"))
        UserProfile(
            username = me.username,
            email = me.email,
            avatar = me.avatar,
            postsCount = me.postsCount,
            savedCount = me.savedCount,
            recentThreadTitles = me.recentThreads.map { it.title },
            savedThreadTitles = me.savedThreads.map { it.title }
        )
    }
}
