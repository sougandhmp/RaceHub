package org.gce.racehub.forum.data.dto

import kotlinx.serialization.Serializable

// Forum API payloads (threads, comments, likes).

@Serializable
internal data class GraphQLRequestWithVariables(
    val query: String,
    val variables: ThreadsVariables
)

@Serializable
internal data class ThreadsVariables(
    val sort: String? = null,
    val category: String? = null,
    val userId: String? = null
)

@Serializable
internal data class ThreadsData(
    val threads: List<ThreadDto>
)

@Serializable
internal data class ThreadDto(
    val id: String,
    val title: String,
    val category: String,
    val author: ThreadAuthorDto,
    val excerpt: String? = null,
    val content: String,
    val createdAt: String,
    val likes: Int,
    val bookmarked: Boolean = false,
    val comments: List<ThreadCommentDto> = emptyList()
)

@Serializable
internal data class ThreadAuthorDto(
    val username: String,
    val avatar: String
)

@Serializable
internal data class ThreadCommentDto(
    val content: String,
    val author: ThreadCommentAuthorDto
)

@Serializable
internal data class ThreadCommentAuthorDto(
    val username: String
)

@Serializable
internal data class GraphQLCreateThreadRequest(
    val query: String,
    val variables: CreateThreadVariables
)

@Serializable
internal data class CreateThreadVariables(
    val userId: String,
    val input: CreateThreadInput
)

@Serializable
internal data class CreateThreadInput(
    val title: String,
    val category: String,
    val content: String
)

@Serializable
internal data class CreateThreadData(
    val createThread: CreatedThreadDto
)

@Serializable
internal data class CreatedThreadDto(
    val id: String,
    val title: String,
    val createdAt: String
)

@Serializable
internal data class GraphQLAddCommentRequest(
    val query: String,
    val variables: AddCommentVariables
)

@Serializable
internal data class AddCommentVariables(
    val userId: String,
    val threadId: String,
    val content: String
)

@Serializable
internal data class AddCommentData(
    val addComment: AddedCommentDto
)

@Serializable
internal data class AddedCommentDto(
    val id: String,
    val content: String,
    val createdAt: String
)

@Serializable
internal data class GraphQLLikeThreadRequest(
    val query: String,
    val variables: LikeThreadVariables
)

@Serializable
internal data class LikeThreadVariables(
    val id: String
)

@Serializable
internal data class LikeThreadData(
    val likeThread: LikedThreadDto
)

@Serializable
internal data class LikedThreadDto(
    val id: String,
    val likes: Int
)
