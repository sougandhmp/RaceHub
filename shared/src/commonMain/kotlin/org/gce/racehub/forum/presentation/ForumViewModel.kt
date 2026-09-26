package org.gce.racehub.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.race.domain.usecase.GetThreadsUseCase

/**
 * Shared MVI ViewModel for the Forum tab (Android and iOS). Loads once on
 * creation; filter changes and [ForumIntent.Refresh] reload.
 */
class ForumViewModel(
    private val getThreads: GetThreadsUseCase,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(ForumState())

    /** Swift sees `state` and `stateFlow` via KMP-NativeCoroutines. */
    @NativeCoroutinesState
    val state: StateFlow<ForumState> = _state.asStateFlow()

    private val _effects = Channel<ForumEffect>(Channel.BUFFERED)

    /** One-off events. Swift sees `effects` as a native flow. */
    @NativeCoroutines
    val effects: Flow<ForumEffect> = _effects.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onIntent(intent: ForumIntent) {
        when (intent) {
            ForumIntent.Refresh -> load()
            is ForumIntent.SelectSort -> {
                mutate(ForumMutation.FiltersChanged(intent.sort, _state.value.selectedCategory))
                load()
            }
            is ForumIntent.SelectCategory -> {
                mutate(ForumMutation.FiltersChanged(_state.value.selectedSort, intent.category))
                load()
            }
        }
    }

    private fun mutate(mutation: ForumMutation) = _state.update { ForumReducer.reduce(it, mutation) }

    private fun load() {
        // Switching filters quickly must not let an older, slower response win.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            mutate(ForumMutation.LoadStarted)
            val current = _state.value
            when (val result = getThreads(current.selectedSort, current.selectedCategory, userSession.userId)) {
                is DataResult.Success -> mutate(ForumMutation.Loaded(result.data))
                is DataResult.Failure -> {
                    mutate(ForumMutation.LoadFailed)
                    _effects.send(ForumEffect.ShowLoadError(result.error))
                }
            }
        }
    }
}
