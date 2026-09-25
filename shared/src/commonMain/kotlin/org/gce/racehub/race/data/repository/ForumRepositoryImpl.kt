package org.gce.racehub.race.data.repository

import org.gce.racehub.core.DataResult
import org.gce.racehub.core.safeCall
import org.gce.racehub.race.data.dto.*
import org.gce.racehub.race.data.network.GraphQLClient
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.race.domain.model.ThreadAuthor
import org.gce.racehub.race.domain.model.ThreadComment
import org.gce.racehub.race.domain.repository.ForumRepository

/** [ForumRepository] backed by the GraphQL API. */
class ForumRepositoryImpl(private val graphQL: GraphQLClient) : ForumRepository {

    private companion object {
        const val TAG = "ForumRepository"
    }

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

    override suspend fun getThreads(sort: String?, category: String?, userId: String?): DataResult<List<Thread>> =
        safeCall(TAG) {
            val data: ThreadsData = graphQL.execute(
                GraphQLRequestWithVariables(
                    query = threadsQuery,
                    variables = ThreadsVariables(sort = sort, category = category, userId = userId)
                ),
                "Couldn't load threads."
            )
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

    override suspend fun createThread(userId: String, title: String, category: String, content: String): DataResult<Thread> =
        safeCall(TAG) {
            val data: CreateThreadData = graphQL.execute(
                GraphQLCreateThreadRequest(
                    query = createThreadMutation,
                    variables = CreateThreadVariables(userId = userId, input = CreateThreadInput(title, category, content))
                ),
                "Couldn't create the thread."
            )
            val created = data.createThread
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

    override suspend fun addComment(userId: String, threadId: String, content: String): DataResult<ThreadComment> =
        safeCall(TAG) {
            val data: AddCommentData = graphQL.execute(
                GraphQLAddCommentRequest(
                    query = addCommentMutation,
                    variables = AddCommentVariables(userId = userId, threadId = threadId, content = content)
                ),
                "Couldn't post the comment."
            )
            // The mutation doesn't return author info; leave the username blank so
            // callers don't mistake the raw user id for a display name.
            ThreadComment(content = data.addComment.content, authorUsername = "")
        }

    override suspend fun likeThread(threadId: String): DataResult<Int> = safeCall(TAG) {
        val data: LikeThreadData = graphQL.execute(
            GraphQLLikeThreadRequest(query = likeThreadMutation, variables = LikeThreadVariables(id = threadId)),
            "Couldn't update the like."
        )
        data.likeThread.likes
    }
}
