package org.gce.racehub.di

import org.gce.racehub.core.domain.session.UserSession
import org.koin.mp.KoinPlatform

/** Swift entry point for the app-level session check made at launch. */
object AppSession {
    /** True when a signed-in user was restored from secure storage. */
    val isSignedIn: Boolean get() = KoinPlatform.getKoin().get<UserSession>().currentUser.value != null
}
