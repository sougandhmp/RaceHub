package org.gce.racehub.auth.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.gce.racehub.auth.domain.model.User

/**
 * Process-wide holder for the currently authenticated [User].
 *
 * Populated by login/sign-up flows on success and read by feature ViewModels
 * that need the caller's identity (e.g. forum threads with per-user
 * `bookmarked` flags). Cleared on sign-out.
 *
 * Registered as a Koin `single` so all consumers see the same instance.
 */
class UserSession {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun setUser(user: User) {
        _currentUser.value = user
    }

    fun clear() {
        _currentUser.value = null
    }

    /** Convenience accessor for the current user id, or `null` if signed out. */
    val userId: String? get() = _currentUser.value?.id
}
