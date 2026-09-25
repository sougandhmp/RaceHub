package org.gce.racehub.race.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ThreadAuthor(
    val username: String,
    val avatar: String
)

@Serializable
data class ThreadComment(
    val content: String,
    val authorUsername: String
)

@Serializable
data class Thread(
    val id: String,
    val title: String,
    val category: String,
    val author: ThreadAuthor,
    val excerpt: String?,
    val content: String,
    val createdAt: String,
    val likes: Int,
    val bookmarked: Boolean,
    val comments: List<ThreadComment>
)
