package org.gce.racehub.forum.domain.model

/** How the forum orders threads. The data layer maps these to API values. */
enum class ThreadSort { Latest, Popular, MostCommented }

/** The forum's fixed categories, in display order. */
object ForumCategories {
    const val DEFAULT = "General Discussion"

    val all: List<String> = listOf(
        DEFAULT,
        "Race Weekends",
        "Teams & Drivers",
        "Technical / Cars",
    )
}
