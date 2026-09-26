package org.gce.racehub.forum.presentation

import org.gce.racehub.core.domain.DataError
import org.gce.racehub.forum.domain.model.ForumCategories

// MVI contract for the create-thread form.

/** Immutable snapshot of the create-thread form. Only [CreateThreadReducer] produces new values. */
data class CreateThreadState(
    val title: String = "",
    val category: String = ForumCategories.DEFAULT,
    val content: String = "",
    val isSubmitting: Boolean = false
) {
    val canSubmit: Boolean get() = !isSubmitting && title.isNotBlank() && content.isNotBlank()

    /** Categories the picker offers, in display order. */
    val categories: List<String> get() = ForumCategories.all
}

sealed class CreateThreadIntent {
    data class TitleChanged(val title: String) : CreateThreadIntent()
    data class CategoryChanged(val category: String) : CreateThreadIntent()
    data class ContentChanged(val content: String) : CreateThreadIntent()
    data object Submit : CreateThreadIntent()
}

sealed class CreateThreadEffect {
    /** Posted; the view should close and the forum refresh. */
    data object ThreadCreated : CreateThreadEffect()
    data object NotSignedIn : CreateThreadEffect()
    data class SubmitFailed(val error: DataError) : CreateThreadEffect()
}

internal sealed interface CreateThreadMutation {
    data class TitleChanged(val title: String) : CreateThreadMutation
    data class CategoryChanged(val category: String) : CreateThreadMutation
    data class ContentChanged(val content: String) : CreateThreadMutation
    data object SubmitStarted : CreateThreadMutation
    /** Posted: reset the form so reopening the screen starts blank. */
    data object Submitted : CreateThreadMutation
    data object SubmitFailed : CreateThreadMutation
}
