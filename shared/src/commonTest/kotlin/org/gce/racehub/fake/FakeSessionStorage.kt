package org.gce.racehub.fake

import org.gce.racehub.auth.data.storage.SessionStorage
import org.gce.racehub.auth.domain.model.User

class FakeSessionStorage(initialUser: User? = null) : SessionStorage {
    private var stored: User? = initialUser

    override fun saveUser(user: User) { stored = user }
    override fun restoreUser(): User? = stored
    override fun clearUser() { stored = null }
}
