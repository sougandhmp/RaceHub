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
import org.gce.racehub.home.HomeScreen
import org.gce.racehub.home.HomeViewModel
import org.gce.racehub.home.ScheduleScreen
import org.gce.racehub.home.StandingsScreen
import org.gce.racehub.login.LoginScreen
import org.gce.racehub.race.di.raceModule
import org.gce.racehub.signup.SignUpScreen
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

/** Top-level navigation destinations for the app. */
private enum class Screen { Login, SignUp, Home, Schedule, Standings }

/**
 * Root composable. Owns the top-level navigation state and routes each
 * [Screen] value to the appropriate screen composable.
 *
 * Navigation is intentionally kept simple (enum + `when`) here; replace
 * with Jetpack Navigation or a dedicated NavController when the app grows.
 */
@Composable
fun App() {
    MaterialTheme {
        var screen by remember { mutableStateOf(Screen.Login) }

        // We use a single HomeViewModel to share state between Home, Schedule, and Standings
        val homeViewModel: HomeViewModel = koinViewModel()
        val homeState by homeViewModel.state.collectAsState()

        when (screen) {
            Screen.Login -> LoginScreen(
                onLoginSuccess = { screen = Screen.Home },
                onNavigateToSignUp = { screen = Screen.SignUp }
            )

            Screen.SignUp -> SignUpScreen(
                onSignUpSuccess = { screen = Screen.Home },
                onNavigateToLogin = { screen = Screen.Login }
            )

            Screen.Home -> HomeScreen(
                viewModel = homeViewModel,
                onViewAllSchedule = { screen = Screen.Schedule },
                onViewAllStandings = { screen = Screen.Standings }
            )

            Screen.Schedule -> ScheduleScreen(
                schedule = homeState.raceSchedule,
                latestThread = homeState.trendingThreads.firstOrNull(),
                onBack = { screen = Screen.Home }
            )

            Screen.Standings -> StandingsScreen(
                standings = homeState.driverStandings,
                onBack = { screen = Screen.Home }
            )
        }
    }
}

@Composable
@Preview
fun AppPreview() {
    // Provide a Koin context for the preview to resolve ViewModels.
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
