package org.gce.racehub.forum

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
import org.gce.racehub.race.domain.usecase.GetThreadsUseCase

class ForumViewModel(
    private val getThreadsUseCase: GetThreadsUseCase,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(ForumState())
    val state: StateFlow<ForumState> = _state.asStateFlow()

    private val _effect = Channel<ForumEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadThreads()
    }

    fun onIntent(intent: ForumIntent) {
        when (intent) {
            is ForumIntent.Refresh -> loadThreads()
            is ForumIntent.DismissError -> _state.update { it.copy(errorMessage = null) }
            is ForumIntent.SelectSort -> {
                _state.update { it.copy(selectedSort = intent.sort) }
                loadThreads()
            }
            is ForumIntent.SelectCategory -> {
                _state.update { it.copy(selectedCategory = intent.category) }
                loadThreads()
            }
        }
    }

    private fun loadThreads() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = getThreadsUseCase(
                sort = _state.value.selectedSort,
                category = _state.value.selectedCategory,
                userId = userSession.userId
            )
            // On failure, keep the threads already on screen and show why the refresh failed.
            _state.update {
                it.copy(
                    isLoading = false,
                    threads = result.data ?: it.threads,
                    errorMessage = result.error?.message
                )
            }
        }
    }
}
