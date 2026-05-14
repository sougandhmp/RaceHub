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
import org.gce.racehub.race.domain.usecase.GetThreadsUseCase

class ForumViewModel(
    private val getThreadsUseCase: GetThreadsUseCase
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
            try {
                val threads = getThreadsUseCase(
                    sort = _state.value.selectedSort,
                    category = _state.value.selectedCategory,
                    userId = null
                )
                _state.update { it.copy(isLoading = false, threads = threads) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load threads. Pull to refresh."
                    )
                }
            }
        }
    }
}
