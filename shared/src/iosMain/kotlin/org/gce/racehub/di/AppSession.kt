package org.gce.racehub.di

import org.gce.racehub.auth.domain.session.UserSession
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/** Swift entry point for the app-level session check made at launch. */
object AppSession : KoinComponent {
    /** True when a signed-in user was restored from secure storage. */
    val isSignedIn: Boolean get() = get<UserSession>().currentUser.value != null
}
