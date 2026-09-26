package org.gce.racehub.core.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.gce.racehub.core.domain.model.User

/**
 * Process-wide holder for the currently authenticated [User].
 *
 * Backed by [SessionStorage] so the session survives process restarts.
 * On construction the last-saved user is restored from storage automatically.
 * Cleared (both in-memory and on disk) when [clear] is called.
 *
 * Registered as a Koin `single` so all consumers see the same instance.
 */
class UserSession(private val sessionStorage: SessionStorage) {

    private val _currentUser = MutableStateFlow<User?>(sessionStorage.restoreUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun setUser(user: User) {
        _currentUser.value = user
        sessionStorage.saveUser(user)
    }

    fun clear() {
        _currentUser.value = null
        sessionStorage.clearUser()
    }

    /** Convenience accessor for the current user id, or `null` if signed out. */
    val userId: String? get() = _currentUser.value?.id

    /** Convenience accessor for the current user's display name, or `null` if signed out. */
    val username: String? get() = _currentUser.value?.username ?: _currentUser.value?.name
}
