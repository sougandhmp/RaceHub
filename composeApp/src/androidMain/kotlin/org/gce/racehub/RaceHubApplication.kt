package org.gce.racehub

import android.app.Application
import org.gce.racehub.di.KoinInitializer
import org.gce.racehub.di.appModule

/**
 * Custom [Application] class for RaceHub.
 *
 * Initializes app-wide infrastructure before any Activity or ViewModel starts.
 * Registered in AndroidManifest.xml via `android:name=".RaceHubApplication"`.
 *
 * Initialization order matters:
 * 1. [KoinInitializer.setApplication] — stores the context so `startKoin`
 *    can call `androidContext(...)` inside the shared module.
 * 2. [KoinInitializer.init] — actually starts the Koin container.
 */
class RaceHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        KoinInitializer.setApplication(this)
        KoinInitializer.init(baseUrl = "http://140.245.233.203:30018", appModule)
    }
}
