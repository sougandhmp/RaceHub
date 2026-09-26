package org.gce.racehub.core.di

import org.gce.racehub.core.data.network.HttpClientFactory
import org.gce.racehub.core.domain.session.UserSession
import org.koin.dsl.module

/**
 * Infrastructure every feature shares: the single HTTP client and the
 * signed-in user's session (its storage comes from the platform module).
 * Feature modules depend on this explicitly rather than on each other.
 *
 * @param logNetwork log each request's method, URL and status (debug builds only).
 */
internal fun createCoreModule(logNetwork: Boolean) = module {
    single { HttpClientFactory.create(logNetwork) }
    single { UserSession(get()) }
}
