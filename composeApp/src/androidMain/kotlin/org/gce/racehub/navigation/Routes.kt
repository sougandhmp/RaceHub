package org.gce.racehub.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Navigation 3 destinations. Serializable so the back stack survives rotation and
// process death; they carry ids rather than domain objects for the same reason.

@Serializable data object LoginRoute : NavKey
@Serializable data object SignUpRoute : NavKey
@Serializable data object ForgotPasswordRoute : NavKey
@Serializable data class EmailVerificationRoute(val email: String) : NavKey
@Serializable data object HomeRoute : NavKey
@Serializable data object ScheduleRoute : NavKey
@Serializable data object StandingsRoute : NavKey
@Serializable data object CreateThreadRoute : NavKey
@Serializable data class ThreadDetailRoute(val threadId: String) : NavKey
@Serializable data class RaceDetailRoute(val slug: String) : NavKey
