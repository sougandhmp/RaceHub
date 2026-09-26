package org.gce.racehub.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gce.racehub.core.domain.session.UserSession
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.forum.domain.model.ThreadComment
import org.gce.racehub.forum.domain.usecase.AddCommentUseCase
import org.gce.racehub.forum.domain.usecase.LikeThreadUseCase

/**
 * Shared MVI ViewModel for a thread's detail screen (Android and iOS).
 * Send [ThreadDetailIntent.Open] when the screen shows a thread.
 */
class ThreadDetailViewModel internal constructor(
    private val addComment: AddCommentUseCase,
    private val likeThread: LikeThreadUseCase,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(ThreadDetailState())

    /** Swift sees `state` and `stateFlow` via KMP-NativeCoroutines. */
    @NativeCoroutinesState
    val state: StateFlow<ThreadDetailState> = _state.asStateFlow()

    private val _effects = Channel<ThreadDetailEffect>(Channel.BUFFERED)

    /** One-off events. Swift sees `effects` as a native flow. */
    @NativeCoroutines
    val effects: Flow<ThreadDetailEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: ThreadDetailIntent) {
        when (intent) {
            is ThreadDetailIntent.Open -> mutate(ThreadDetailMutation.Opened(intent.threadId, intent.likes))
            is ThreadDetailIntent.CommentInputChanged -> mutate(ThreadDetailMutation.InputChanged(intent.text))
            ThreadDetailIntent.SubmitComment -> submitComment()
            ThreadDetailIntent.ToggleLike -> toggleLike()
        }
    }

    private fun mutate(mutation: ThreadDetailMutation) = _state.update { ThreadDetailReducer.reduce(it, mutation) }

    private fun toggleLike() {
        val before = _state.value
        if (before.isLiking || before.threadId.isBlank()) return
        mutate(ThreadDetailMutation.LikeToggled)
        viewModelScope.launch {
            when (val result = likeThread(before.threadId)) {
                is DataResult.Success -> mutate(ThreadDetailMutation.LikeConfirmed(result.data))
                is DataResult.Failure -> {
                    mutate(ThreadDetailMutation.LikeReverted(before.isLiked, before.likes))
                    _effects.send(ThreadDetailEffect.LikeFailed(result.error))
                }
            }
        }
    }

    private fun submitComment() {
        val current = _state.value
        if (!current.canSubmit || current.threadId.isBlank()) return
        val userId = userSession.userId
        if (userId.isNullOrBlank()) {
            _effects.trySend(ThreadDetailEffect.NotSignedIn)
            return
        }
        val text = current.commentInput.trim()
        // The API doesn't return the author, so attribute the comment locally.
        val author = userSession.username ?: "you"
        mutate(ThreadDetailMutation.CommentSubmitted)
        viewModelScope.launch {
            when (val result = addComment(userId = userId, threadId = current.threadId, content = text)) {
                is DataResult.Success -> mutate(ThreadDetailMutation.CommentPosted(ThreadComment(text, author)))
                is DataResult.Failure -> {
                    mutate(ThreadDetailMutation.CommentFailed)
                    _effects.send(ThreadDetailEffect.CommentFailed(result.error))
                }
            }
        }
    }
}
