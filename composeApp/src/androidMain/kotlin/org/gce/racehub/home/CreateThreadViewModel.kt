package org.gce.racehub.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.race.domain.usecase.CreateThreadUseCase

class CreateThreadViewModel(
    private val createThreadUseCase: CreateThreadUseCase,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(CreateThreadState())
    val state: StateFlow<CreateThreadState> = _state.asStateFlow()

    private val _effect = Channel<CreateThreadEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: CreateThreadIntent) {
        when (intent) {
            is CreateThreadIntent.TitleChanged ->
                _state.update { it.copy(title = intent.title) }

            is CreateThreadIntent.CategoryChanged ->
                _state.update { it.copy(category = intent.category) }

            is CreateThreadIntent.ContentChanged ->
                _state.update { it.copy(content = intent.content) }

            is CreateThreadIntent.Submit ->
                submit()

            is CreateThreadIntent.DismissError ->
                _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun submit() {
        val current = _state.value
        if (!current.canSubmit) return

        val userId = userSession.userId
        if (userId.isNullOrBlank()) {
            _state.update { it.copy(errorMessage = "You must be signed in to post.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                createThreadUseCase(
                    userId = userId,
                    title = _state.value.title,
                    category = _state.value.category,
                    content = _state.value.content
                )
                _state.update { it.copy(isSubmitting = false) }
                _effect.send(CreateThreadEffect.ThreadCreated)
            } catch (_: Exception) {
                _state.update { it.copy(isSubmitting = false, errorMessage = "Failed to create thread.") }
            }
        }
    }
}
