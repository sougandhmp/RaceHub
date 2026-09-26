package org.gce.racehub.fake

import org.gce.racehub.core.domain.session.SessionStorage
import org.gce.racehub.core.domain.model.User

class FakeSessionStorage(initialUser: User? = null) : SessionStorage {
    private var stored: User? = initialUser

    override fun saveUser(user: User) { stored = user }
    override fun restoreUser(): User? = stored
    override fun clearUser() { stored = null }
}
