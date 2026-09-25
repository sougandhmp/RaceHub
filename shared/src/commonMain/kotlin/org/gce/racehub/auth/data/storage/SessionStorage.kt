package org.gce.racehub.auth.data.storage

import org.gce.racehub.auth.domain.model.User

interface SessionStorage {
    fun saveUser(user: User)
    fun restoreUser(): User?
    fun clearUser()
}
