package org.gce.racehub.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.race.domain.model.ThreadComment
import org.gce.racehub.race.domain.usecase.AddCommentUseCase

data class ThreadDetailState(
    val commentInput: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val postedComments: List<ThreadComment> = emptyList()
) {
    val canSubmit: Boolean get() = !isSubmitting && commentInput.isNotBlank()
}

sealed class ThreadDetailIntent {
    data class CommentInputChanged(val text: String) : ThreadDetailIntent()
    data class SubmitComment(val threadId: String) : ThreadDetailIntent()
    data object DismissError : ThreadDetailIntent()
}

class ThreadDetailViewModel(
    private val addCommentUseCase: AddCommentUseCase,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(ThreadDetailState())
    val state: StateFlow<ThreadDetailState> = _state.asStateFlow()

    fun onIntent(intent: ThreadDetailIntent) {
        when (intent) {
            is ThreadDetailIntent.CommentInputChanged ->
                _state.update { it.copy(commentInput = intent.text) }

            is ThreadDetailIntent.SubmitComment ->
                submitComment(intent.threadId)

            is ThreadDetailIntent.DismissError ->
                _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun submitComment(threadId: String) {
        val current = _state.value
        if (!current.canSubmit) return

        val userId = userSession.userId
        if (userId.isNullOrBlank()) {
            _state.update { it.copy(errorMessage = "You must be signed in to comment.") }
            return
        }

        val text = current.commentInput.trim()
        val authorUsername = userSession.username ?: "you"

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                addCommentUseCase(userId = userId, threadId = threadId, content = text)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        commentInput = "",
                        postedComments = it.postedComments + ThreadComment(
                            content = text,
                            authorUsername = authorUsername
                        )
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.message ?: "Failed to post comment."
                    )
                }
            }
        }
    }
}
