package org.gce.racehub

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import org.gce.racehub.auth.di.createProductionAuthModule
import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.di.appModule
import org.gce.racehub.forum.ForumIntent
import org.gce.racehub.forum.ForumViewModel
import org.gce.racehub.forum.ThreadDetailScreen
import org.gce.racehub.home.CreateThreadScreen
import org.gce.racehub.home.HomeScreen
import org.gce.racehub.home.ScheduleScreen
import org.gce.racehub.home.StandingsScreen
import org.gce.racehub.login.LoginScreen
import org.gce.racehub.race.RaceIntent
import org.gce.racehub.race.RaceViewModel
import org.gce.racehub.race.di.createRaceModule
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.signup.SignUpScreen
import org.gce.racehub.theme.DarkAppColors
import org.gce.racehub.theme.LightAppColors
import org.gce.racehub.theme.LocalAppColors
import org.gce.racehub.theme.RacingRed
import org.gce.racehub.theme.ThemeManager
import org.gce.racehub.theme.ThemeMode
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

private enum class Screen { Login, SignUp, Home, Schedule, Standings, CreateThread, ThreadDetail }

@Composable
fun App() {
    val themeManager: ThemeManager = koinInject()
    val themeMode by themeManager.themeMode.collectAsState()
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemInDark
    }
    val appColors = if (isDark) DarkAppColors else LightAppColors

    val materialColorScheme = if (isDark) {
        darkColorScheme(primary = RacingRed)
    } else {
        lightColorScheme(primary = RacingRed)
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(colorScheme = materialColorScheme) {
            val userSession: UserSession = koinInject()
            var screen by remember { mutableStateOf(if (userSession.currentUser.value != null) Screen.Home else Screen.Login) }
            var selectedThread by remember { mutableStateOf<Thread?>(null) }

            val raceViewModel: RaceViewModel = koinViewModel()
            val forumViewModel: ForumViewModel = koinViewModel()
            val raceState by raceViewModel.state.collectAsState()

            LaunchedEffect(Unit) {
                if (userSession.currentUser.value != null) {
                    raceViewModel.onIntent(RaceIntent.Refresh)
                    forumViewModel.onIntent(ForumIntent.Refresh)
                }
            }

            when (screen) {
                Screen.Login -> LoginScreen(
                    onLoginSuccess = {
                        raceViewModel.onIntent(RaceIntent.Refresh)
                        forumViewModel.onIntent(ForumIntent.Refresh)
                        screen = Screen.Home
                    },
                    onNavigateToSignUp = { screen = Screen.SignUp }
                )

                Screen.SignUp -> SignUpScreen(
                    onSignUpSuccess = {
                        raceViewModel.onIntent(RaceIntent.Refresh)
                        forumViewModel.onIntent(ForumIntent.Refresh)
                        screen = Screen.Home
                    },
                    onNavigateToLogin = { screen = Screen.Login }
                )

                Screen.Home -> HomeScreen(
                    onViewAllSchedule = { screen = Screen.Schedule },
                    onViewAllStandings = { screen = Screen.Standings },
                    onCreateThread = { screen = Screen.CreateThread },
                    onThreadClick = { thread ->
                        selectedThread = thread
                        screen = Screen.ThreadDetail
                    },
                    onSignedOut = { screen = Screen.Login }
                )

                Screen.Schedule -> ScheduleScreen(
                    schedule = raceState.raceSchedule,
                    onBack = { screen = Screen.Home }
                )

                Screen.Standings -> StandingsScreen(
                    drivers = raceState.driverStandings,
                    constructors = raceState.constructorStandings,
                    onBack = { screen = Screen.Home }
                )

                Screen.CreateThread -> CreateThreadScreen(
                    onCancel = { screen = Screen.Home },
                    onThreadCreated = {
                        forumViewModel.onIntent(ForumIntent.Refresh)
                        screen = Screen.Home
                    }
                )

                Screen.ThreadDetail -> {
                    val thread = selectedThread
                    if (thread == null) {
                        screen = Screen.Home
                    } else {
                        ThreadDetailScreen(
                            thread = thread,
                            onBack = { screen = Screen.Home }
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun AppPreview() {
    KoinApplication(configuration = koinConfiguration(declaration = {
        modules(
            createProductionAuthModule("https://api.example.com"),
            createRaceModule("https://api.example.com"),
            appModule
        )
    }), content = {
        App()
    })
}
