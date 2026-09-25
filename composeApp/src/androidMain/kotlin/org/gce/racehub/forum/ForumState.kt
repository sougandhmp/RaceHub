package org.gce.racehub.forum

import org.gce.racehub.race.domain.model.Thread

/**
 * Immutable snapshot of the Forum tab. Produced by [ForumViewModel] on every
 * state change; the View never mutates this object directly.
 */
data class ForumState(

    /** Forum threads shown on the Forum tab. Empty until loaded. */
    val threads: List<Thread> = emptyList(),

    /** True while threads are being fetched; drives the loading indicator. */
    val isLoading: Boolean = false,

    /** Non-null when a data-fetch error should be shown to the user. */
    val errorMessage: String? = null,

    /** Active sort key sent to the API. */
    val selectedSort: String = "latest",

    /** Active category filter; null means all categories. */
    val selectedCategory: String? = null
)
