package org.gce.racehub.core.domain.session

import org.gce.racehub.core.domain.model.User

/**
 * Persists the signed-in user across launches. Declared in the domain so
 * [UserSession] depends on this abstraction; platforms implement it in data
 * (encrypted prefs on Android, Keychain on iOS).
 */
interface SessionStorage {
    fun saveUser(user: User)
    fun restoreUser(): User?
    fun clearUser()
}
