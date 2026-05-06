package org.gce.racehub.home

/**
 * Snapshot of the New Thread form. The View renders from this state and
 * never mutates it directly; all changes go through [CreateThreadIntent].
 */
data class CreateThreadState(
    val title: String = "",
    val category: String = "General Discussion",
    val content: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) {
    val canSubmit: Boolean
        get() = !isSubmitting && title.isNotBlank() && content.isNotBlank()
}

sealed class CreateThreadIntent {
    data class TitleChanged(val title: String) : CreateThreadIntent()
    data class CategoryChanged(val category: String) : CreateThreadIntent()
    data class ContentChanged(val content: String) : CreateThreadIntent()
    data object Submit : CreateThreadIntent()
    data object DismissError : CreateThreadIntent()
}

sealed class CreateThreadEffect {
    data object ThreadCreated : CreateThreadEffect()
}
