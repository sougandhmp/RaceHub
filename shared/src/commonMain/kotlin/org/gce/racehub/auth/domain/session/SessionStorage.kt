package org.gce.racehub.auth.domain.session

import org.gce.racehub.auth.domain.model.User

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
