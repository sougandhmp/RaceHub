package org.gce.racehub

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import org.gce.racehub.auth.di.createProductionAuthModule
import org.gce.racehub.di.appModule
import org.gce.racehub.forum.ForumIntent
import org.gce.racehub.forum.ForumViewModel
import org.gce.racehub.home.CreateThreadScreen
import org.gce.racehub.home.HomeScreen
import org.gce.racehub.home.ScheduleScreen
import org.gce.racehub.home.StandingsScreen
import org.gce.racehub.login.LoginScreen
import org.gce.racehub.race.RaceIntent
import org.gce.racehub.race.RaceViewModel
import org.gce.racehub.race.di.raceModule
import org.gce.racehub.signup.SignUpScreen
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

/** Top-level navigation destinations for the app. */
private enum class Screen { Login, SignUp, Home, Schedule, Standings, CreateThread }

/**
 * Root composable. Owns the top-level navigation state and routes each
 * [Screen] value to the appropriate screen composable.
 *
 * The Race and Forum ViewModels are resolved here so the same instance is
 * shared between the Home tabs and the detail screens that hang off them
 * (Schedule, Standings).
 */
@Composable
fun App() {
    MaterialTheme {
        var screen by remember { mutableStateOf(Screen.Login) }

        val raceViewModel: RaceViewModel = koinViewModel()
        val forumViewModel: ForumViewModel = koinViewModel()
        val raceState by raceViewModel.state.collectAsState()

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
                onCreateThread = { screen = Screen.CreateThread }
            )

            Screen.Schedule -> ScheduleScreen(
                schedule = raceState.raceSchedule,
                latestThread = raceState.trendingThreads.firstOrNull(),
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
        }
    }
}

@Composable
@Preview
fun AppPreview() {
    KoinApplication(configuration = koinConfiguration(declaration = {
        modules(
            createProductionAuthModule("https://api.example.com"),
            raceModule,
            appModule
        )
    }), content = {
        App()
    })
}
