package org.gce.racehub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gce.racehub.auth.di.createProductionAuthModule
import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.di.appModule
import org.gce.racehub.emailverification.EmailVerificationScreen
import org.gce.racehub.forgotpassword.ForgotPasswordScreen
import org.gce.racehub.forum.presentation.ForumIntent
import org.gce.racehub.forum.presentation.ForumViewModel
import org.gce.racehub.forum.ThreadDetailScreen
import org.gce.racehub.home.CreateThreadScreen
import org.gce.racehub.home.HomeScreen
import org.gce.racehub.home.ScheduleScreen
import org.gce.racehub.home.StandingsScreen
import org.gce.racehub.login.LoginScreen
import org.gce.racehub.race.RaceDetailScreen
import org.gce.racehub.race.presentation.RaceIntent
import org.gce.racehub.race.presentation.RaceViewModel
import org.gce.racehub.race.di.createRaceModule
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.forum.domain.model.Thread
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

private enum class Screen { Login, SignUp, EmailVerification, ForgotPassword, Home, Schedule, Standings, CreateThread, ThreadDetail, RaceDetail }

@Composable
fun App() {
    val themeManager: ThemeManager = koinInject()
    val themeMode by themeManager.themeMode.collectAsStateWithLifecycle()
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
            var pendingVerificationEmail by remember { mutableStateOf("") }
            var verificationOrigin by remember { mutableStateOf(Screen.SignUp) }

            val raceViewModel: RaceViewModel = koinViewModel()
            val forumViewModel: ForumViewModel = koinViewModel()
            val raceState by raceViewModel.state.collectAsStateWithLifecycle()
            var selectedRace by remember { mutableStateOf<Race?>(null) }
            var raceDetailOrigin by remember { mutableStateOf(Screen.Home) }

            // Screens are swapped via `screen` state rather than a nav back stack, so
            // system Back must be routed explicitly — otherwise it finishes the
            // activity from any sub-screen. Targets mirror each screen's onBack.
            val backTarget: Screen? = when (screen) {
                Screen.Login, Screen.Home -> null
                Screen.SignUp, Screen.ForgotPassword -> Screen.Login
                Screen.EmailVerification -> verificationOrigin
                Screen.Schedule, Screen.Standings, Screen.CreateThread, Screen.ThreadDetail -> Screen.Home
                Screen.RaceDetail -> raceDetailOrigin
            }
            BackHandler(enabled = backTarget != null) {
                backTarget?.let { screen = it }
            }

            when (screen) {
                Screen.Login -> LoginScreen(
                    onLoginSuccess = {
                        raceViewModel.onIntent(RaceIntent.Refresh)
                        forumViewModel.onIntent(ForumIntent.Refresh)
                        screen = Screen.Home
                    },
                    onNavigateToSignUp = { screen = Screen.SignUp },
                    onNavigateToForgotPassword = { screen = Screen.ForgotPassword },
                    onNavigateToEmailVerification = { email ->
                        pendingVerificationEmail = email
                        verificationOrigin = Screen.Login
                        screen = Screen.EmailVerification
                    }
                )

                Screen.ForgotPassword -> ForgotPasswordScreen(
                    onBack = { screen = Screen.Login },
                    onPasswordResetSuccess = { screen = Screen.Login }
                )

                Screen.SignUp -> SignUpScreen(
                    onSignUpSuccess = { email ->
                        pendingVerificationEmail = email
                        verificationOrigin = Screen.SignUp
                        screen = Screen.EmailVerification
                    },
                    onNavigateToLogin = { screen = Screen.Login }
                )

                Screen.EmailVerification -> EmailVerificationScreen(
                    email = pendingVerificationEmail,
                    onBack = { screen = verificationOrigin },
                    // Email verified, but the account was never signed in. Send the
                    // user to Login to obtain a session with their verified account.
                    onEmailVerified = { screen = Screen.Login }
                )

                Screen.Home -> HomeScreen(
                    onViewAllSchedule = { screen = Screen.Schedule },
                    onViewAllStandings = { screen = Screen.Standings },
                    onCreateThread = { screen = Screen.CreateThread },
                    onThreadClick = { thread ->
                        selectedThread = thread
                        screen = Screen.ThreadDetail
                    },
                    onSignedOut = { screen = Screen.Login },
                    onViewRaceDetail = { race ->
                        selectedRace = race
                        raceDetailOrigin = Screen.Home
                        raceViewModel.onIntent(RaceIntent.SelectRace(race.id))
                        screen = Screen.RaceDetail
                    }
                )

                Screen.Schedule -> ScheduleScreen(
                    schedule = raceState.raceSchedule,
                    onBack = { screen = Screen.Home },
                    onViewRaceDetail = { race ->
                        selectedRace = race
                        raceDetailOrigin = Screen.Schedule
                        raceViewModel.onIntent(RaceIntent.SelectRace(race.id))
                        screen = Screen.RaceDetail
                    }
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

                Screen.RaceDetail -> {
                    val race = selectedRace
                    if (race == null) {
                        screen = Screen.Home
                    } else {
                        RaceDetailScreen(
                            race = race,
                            raceDetail = raceState.selectedRaceDetail,
                            sessions = raceState.selectedRaceSessions,
                            onBack = { screen = raceDetailOrigin }
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
