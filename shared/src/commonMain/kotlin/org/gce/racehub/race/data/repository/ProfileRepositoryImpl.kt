package org.gce.racehub.race.data.repository

import org.gce.racehub.core.DataResult
import org.gce.racehub.core.safeCall
import org.gce.racehub.race.data.dto.GraphQLProfileRequest
import org.gce.racehub.race.data.dto.ProfileData
import org.gce.racehub.race.data.dto.ProfileVariables
import org.gce.racehub.race.data.network.GraphQLClient
import org.gce.racehub.race.domain.model.UserProfile
import org.gce.racehub.race.domain.repository.ProfileRepository

/** [ProfileRepository] backed by the GraphQL API. Sends the auth token as a bearer header. */
class ProfileRepositoryImpl(private val graphQL: GraphQLClient) : ProfileRepository {

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

    override suspend fun getMyProfile(userId: String, token: String): DataResult<UserProfile> = safeCall(TAG) {
        val data: ProfileData = graphQL.execute(
            GraphQLProfileRequest(query = profileQuery, variables = ProfileVariables(userId = userId)),
            "Couldn't load your profile.",
            authToken = token
        )
        val me = data.me
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
