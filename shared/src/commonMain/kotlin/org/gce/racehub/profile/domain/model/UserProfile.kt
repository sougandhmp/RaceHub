package org.gce.racehub.profile.domain.model

data class UserProfile(
    val username: String,
    val email: String,
    val avatar: String,
    val postsCount: Int,
    val savedCount: Int,
    val recentThreadTitles: List<String>,
    val savedThreadTitles: List<String>
)
