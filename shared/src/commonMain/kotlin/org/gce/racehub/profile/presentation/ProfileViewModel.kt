package org.gce.racehub.profile.presentation

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
import org.gce.racehub.auth.domain.usecase.LogoutUseCase
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.race.domain.usecase.GetMyProfileUseCase

/**
 * Shared MVI ViewModel for the Profile tab (Android and iOS). Follows the
 * session: whenever the signed-in user changes it shows them and loads their profile.
 */
class ProfileViewModel(
    private val userSession: UserSession,
    private val logout: LogoutUseCase,
    private val getMyProfile: GetMyProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())

    /** Swift sees `state` and `stateFlow` via KMP-NativeCoroutines. */
    @NativeCoroutinesState
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)

    /** One-off events. Swift sees `effects` as a native flow. */
    @NativeCoroutines
    val effects: Flow<ProfileEffect> = _effects.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            userSession.currentUser.collect { user ->
                mutate(ProfileMutation.UserChanged(user))
                loadProfile()
            }
        }
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Refresh -> loadProfile()
            ProfileIntent.SignOut -> signOut()
        }
    }

    private fun mutate(mutation: ProfileMutation) = _state.update { ProfileReducer.reduce(it, mutation) }

    private fun loadProfile() {
        loadJob?.cancel()
        val user = _state.value.user ?: return
        val token = user.token
        if (token.isNullOrBlank()) return
        loadJob = viewModelScope.launch {
            mutate(ProfileMutation.LoadStarted)
            when (val result = getMyProfile(userId = user.id, token = token)) {
                is DataResult.Success -> mutate(ProfileMutation.Loaded(result.data))
                is DataResult.Failure -> {
                    mutate(ProfileMutation.LoadFailed)
                    _effects.send(ProfileEffect.ShowLoadError(result.error))
                }
            }
        }
    }

    private fun signOut() {
        if (_state.value.isSigningOut) return
        loadJob?.cancel()
        mutate(ProfileMutation.SignOutStarted)
        viewModelScope.launch {
            // Always signs out locally; the server revoke is best effort.
            logout()
            mutate(ProfileMutation.SignedOut)
            _effects.send(ProfileEffect.SignedOut)
        }
    }
}
