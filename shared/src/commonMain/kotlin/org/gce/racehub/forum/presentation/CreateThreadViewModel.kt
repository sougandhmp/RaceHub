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
import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.race.domain.usecase.CreateThreadUseCase

/** Shared MVI ViewModel for the create-thread form (Android and iOS). */
class CreateThreadViewModel(
    private val createThread: CreateThreadUseCase,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(CreateThreadState())

    /** Swift sees `state` and `stateFlow` via KMP-NativeCoroutines. */
    @NativeCoroutinesState
    val state: StateFlow<CreateThreadState> = _state.asStateFlow()

    private val _effects = Channel<CreateThreadEffect>(Channel.BUFFERED)

    /** One-off events. Swift sees `effects` as a native flow. */
    @NativeCoroutines
    val effects: Flow<CreateThreadEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: CreateThreadIntent) {
        when (intent) {
            is CreateThreadIntent.TitleChanged -> mutate(CreateThreadMutation.TitleChanged(intent.title))
            is CreateThreadIntent.CategoryChanged -> mutate(CreateThreadMutation.CategoryChanged(intent.category))
            is CreateThreadIntent.ContentChanged -> mutate(CreateThreadMutation.ContentChanged(intent.content))
            CreateThreadIntent.Submit -> submit()
        }
    }

    private fun mutate(mutation: CreateThreadMutation) = _state.update { CreateThreadReducer.reduce(it, mutation) }

    private fun submit() {
        val form = _state.value
        if (!form.canSubmit) return
        val userId = userSession.userId
        if (userId.isNullOrBlank()) {
            _effects.trySend(CreateThreadEffect.NotSignedIn)
            return
        }
        mutate(CreateThreadMutation.SubmitStarted)
        viewModelScope.launch {
            when (val result = createThread(userId, form.title, form.category, form.content)) {
                is DataResult.Success -> {
                    mutate(CreateThreadMutation.Submitted)
                    _effects.send(CreateThreadEffect.ThreadCreated)
                }
                is DataResult.Failure -> {
                    mutate(CreateThreadMutation.SubmitFailed)
                    _effects.send(CreateThreadEffect.SubmitFailed(result.error))
                }
            }
        }
    }
}
