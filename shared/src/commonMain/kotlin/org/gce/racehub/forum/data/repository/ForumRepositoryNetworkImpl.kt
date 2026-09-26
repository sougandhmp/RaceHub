package org.gce.racehub.forum.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.gce.racehub.core.data.GraphQLResponse
import org.gce.racehub.core.data.safeCall
import org.gce.racehub.core.data.toErrorMessage
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.forum.data.dto.*
import org.gce.racehub.forum.domain.model.Thread
import org.gce.racehub.forum.domain.model.ThreadAuthor
import org.gce.racehub.forum.domain.model.ThreadComment
import org.gce.racehub.forum.domain.model.ThreadSort
import org.gce.racehub.forum.domain.repository.ForumRepository

/** GraphQL implementation of [ForumRepository]. Nothing is cached: the forum is always fetched live. */
internal class ForumRepositoryNetworkImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : ForumRepository {

    private companion object {
        const val TAG = "ForumRepository"
    }

    // GraphQL mutation to add a comment to a forum thread
    private val addCommentMutation = $$"""
        mutation AddComment($userId: ID!, $threadId: ID!, $content: String!) {
            addComment(userId: $userId, threadId: $threadId, content: $content) {
                id
                content
                createdAt
            }
        }
    """.trimIndent()

    // GraphQL mutation to toggle like on a forum thread
    private val likeThreadMutation = $$"""
        mutation UserInteractions($id: ID!) {
            likeThread(id: $id) {
                id
                likes
            }
        }
    """.trimIndent()

    // GraphQL mutation to create a new forum thread
    private val createThreadMutation = $$"""
        mutation CreateThread($userId: ID!, $input: CreateThreadInput!) {
            createThread(userId: $userId, input: $input) {
                id
                title
                createdAt
            }
        }
    """.trimIndent()

    // GraphQL query to fetch full forum threads with author and comments
    private val threadsQuery = $$"""
        query GetThreads($sort: String, $category: String, $userId: ID) {
            threads(sort: $sort, category: $category) {
                id
                title
                category
                author { username avatar }
                excerpt
                content
                createdAt
                likes
                bookmarked(userId: $userId)
                comments {
                    content
                    author { username }
                }
            }
        }
    """.trimIndent()

    /**
     * Fetches forum threads directly from the network. Not cached locally
     * because the schema (author, comments, bookmarks) doesn't fit the
     * existing trending-threads SQLDelight table.
     */
    override suspend fun getThreads(
        sort: ThreadSort?,
        category: String?,
        userId: String?
    ): DataResult<List<Thread>> = safeCall(TAG, "fetch threads") {
        val response: GraphQLResponse<ThreadsData> = httpClient.post("$baseUrl/graphql") {
            contentType(ContentType.Application.Json)
            setBody(
                GraphQLRequestWithVariables(
                    query = threadsQuery,
                    variables = ThreadsVariables(sort = sort?.apiValue(), category = category, userId = userId)
                )
            )
        }.body()

        val data = response.data
            ?: error(response.errors.toErrorMessage("Empty GraphQL response"))

        data.threads.map { dto ->
            Thread(
                id = dto.id,
                title = dto.title,
                category = dto.category,
                author = ThreadAuthor(dto.author.username, dto.author.avatar),
                excerpt = dto.excerpt,
                content = dto.content,
                createdAt = dto.createdAt,
                likes = dto.likes,
                bookmarked = dto.bookmarked,
                comments = dto.comments.map { c ->
                    ThreadComment(content = c.content, authorUsername = c.author.username)
                }
            )
        }
    }

    /**
     * Creates a new forum thread via GraphQL mutation.
     */
    override suspend fun createThread(
        userId: String,
        title: String,
        category: String,
        content: String
    ): DataResult<Thread> = safeCall(TAG, "create thread") {
        val response: GraphQLResponse<CreateThreadData> = httpClient.post("$baseUrl/graphql") {
            contentType(ContentType.Application.Json)
            setBody(
                GraphQLCreateThreadRequest(
                    query = createThreadMutation,
                    variables = CreateThreadVariables(
                        userId = userId,
                        input = CreateThreadInput(title, category, content)
                    )
                )
            )
        }.body()

        val created = response.data?.createThread
            ?: error(response.errors.toErrorMessage("Failed to create thread"))
        Thread(
            id = created.id,
            title = created.title,
            category = category,
            author = ThreadAuthor(username = "", avatar = ""),
            excerpt = null,
            content = content,
            createdAt = created.createdAt,
            likes = 0,
            bookmarked = false,
            comments = emptyList()
        )
    }

    /**
     * Posts a comment via GraphQL mutation.
     */
    override suspend fun addComment(userId: String, threadId: String, content: String): DataResult<ThreadComment> = safeCall(TAG, "add comment") {
        val response: GraphQLResponse<AddCommentData> = httpClient.post("$baseUrl/graphql") {
            contentType(ContentType.Application.Json)
            setBody(
                GraphQLAddCommentRequest(
                    query = addCommentMutation,
                    variables = AddCommentVariables(
                        userId = userId,
                        threadId = threadId,
                        content = content
                    )
                )
            )
        }.body()

        val added = response.data?.addComment
            ?: error(response.errors.toErrorMessage("Failed to add comment"))
        // The mutation does not return author info; leave the username blank so
        // callers don't mistake the raw user id for a display name.
        ThreadComment(content = added.content, authorUsername = "")
    }

    /**
     * Toggles the like on the thread identified by [id] via GraphQL mutation.
     *
     * @return The updated like count returned by the server.
     */
    override suspend fun likeThread(id: String): DataResult<Int> = safeCall(TAG, "like thread") {
        val response: GraphQLResponse<LikeThreadData> = httpClient.post("$baseUrl/graphql") {
            contentType(ContentType.Application.Json)
            setBody(
                GraphQLLikeThreadRequest(
                    query = likeThreadMutation,
                    variables = LikeThreadVariables(id = id)
                )
            )
        }.body()

        val result = response.data?.likeThread
            ?: error(response.errors.toErrorMessage("Failed to like thread"))
        result.likes
    }

    private fun ThreadSort.apiValue(): String = when (this) {
        ThreadSort.Latest -> "latest"
        ThreadSort.Popular -> "top"
        ThreadSort.MostCommented -> "commented"
    }
}
