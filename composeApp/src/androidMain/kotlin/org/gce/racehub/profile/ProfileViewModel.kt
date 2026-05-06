package org.gce.racehub.profile

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
import org.gce.racehub.auth.domain.usecase.LogoutUseCase
import org.gce.racehub.race.domain.usecase.GetMyProfileUseCase

class ProfileViewModel(
    private val userSession: UserSession,
    private val logoutUseCase: LogoutUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            userSession.currentUser.collect { user ->
                _state.update { it.copy(user = user) }
                if (user != null) fetchProfile(user.id, user.token)
            }
        }
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.SignOut -> signOut()
            is ProfileIntent.RefreshProfile -> {
                val user = _state.value.user ?: return
                fetchProfile(user.id, user.token)
            }
            is ProfileIntent.DismissError ->
                _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun fetchProfile(userId: String, token: String?) {
        if (token.isNullOrBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingProfile = true) }
            try {
                val profile = getMyProfileUseCase(userId = userId, token = token)
                _state.update { it.copy(isLoadingProfile = false, profile = profile) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoadingProfile = false, errorMessage = e.message ?: "Failed to load profile.")
                }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(isSigningOut = true, errorMessage = null) }
            val success = logoutUseCase()
            if (success) {
                _state.update { it.copy(isSigningOut = false) }
                _effect.send(ProfileEffect.SignedOut)
            } else {
                _state.update { it.copy(isSigningOut = false, errorMessage = "Sign out failed. Please try again.") }
            }
        }
    }
}
