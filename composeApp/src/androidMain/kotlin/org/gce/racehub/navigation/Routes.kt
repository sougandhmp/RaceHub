package org.gce.racehub.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.Thread

/**
 * Navigation 3 destinations. Each key is @Serializable so the back stack (and the
 * arguments it carries) is saved across rotation and process death.
 */

// Signed out
@Serializable data object LoginRoute : NavKey
@Serializable data object SignUpRoute : NavKey
@Serializable data object ForgotPasswordRoute : NavKey
@Serializable data class EmailVerificationRoute(val email: String) : NavKey

// Signed in
@Serializable data object HomeRoute : NavKey
@Serializable data object ScheduleRoute : NavKey
@Serializable data object StandingsRoute : NavKey
@Serializable data object CreateThreadRoute : NavKey
@Serializable data class ThreadDetailRoute(val thread: Thread) : NavKey
@Serializable data class RaceDetailRoute(val race: Race) : NavKey
