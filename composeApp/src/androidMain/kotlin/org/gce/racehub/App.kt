package org.gce.racehub

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.gce.racehub.auth.di.createProductionAuthModule
import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.di.appModule
import org.gce.racehub.emailverification.EmailVerificationScreen
import org.gce.racehub.forgotpassword.ForgotPasswordScreen
import org.gce.racehub.forum.ForumIntent
import org.gce.racehub.forum.ForumViewModel
import org.gce.racehub.forum.ThreadDetailScreen
import org.gce.racehub.home.CreateThreadScreen
import org.gce.racehub.home.HomeScreen
import org.gce.racehub.home.ScheduleScreen
import org.gce.racehub.home.ScheduleViewModel
import org.gce.racehub.home.StandingsScreen
import org.gce.racehub.home.StandingsViewModel
import org.gce.racehub.login.LoginScreen
import org.gce.racehub.navigation.CreateThreadRoute
import org.gce.racehub.navigation.EmailVerificationRoute
import org.gce.racehub.navigation.ForgotPasswordRoute
import org.gce.racehub.navigation.HomeRoute
import org.gce.racehub.navigation.LoginRoute
import org.gce.racehub.navigation.RaceDetailRoute
import org.gce.racehub.navigation.ScheduleRoute
import org.gce.racehub.navigation.SignUpRoute
import org.gce.racehub.navigation.StandingsRoute
import org.gce.racehub.navigation.ThreadDetailRoute
import org.gce.racehub.race.RaceDetailScreen
import org.gce.racehub.race.RaceDetailViewModel
import org.gce.racehub.race.di.createRaceModule
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
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinConfiguration

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
            RaceHubNavigation()
        }
    }
}

/**
 * The app's navigation, built on Navigation 3.
 *
 * - The back stack is a saveable list of @Serializable keys (see `navigation/Routes.kt`), so it
 *   survives rotation and process death. Before, `remember` lost it and rotating the phone
 *   sent the user back to Home or Login.
 * - Each entry gets its own ViewModelStore, so `koinViewModel()` inside an entry is scoped to
 *   that screen and cleared when it's popped (a reopened Create Thread screen starts empty).
 * - Signing in or out replaces the whole stack, so Back can't return across the auth boundary.
 *   The Home tabs' ViewModels are only created once Home is shown, not on the Login screen.
 */
@Composable
private fun RaceHubNavigation() {
    val userSession: UserSession = koinInject()
    val backStack = rememberNavBackStack(if (userSession.currentUser.value != null) HomeRoute else LoginRoute)

    // Set when a thread is created; the Home entry refreshes the forum and clears it.
    // Saved, so the refresh isn't lost if the process is recreated in between.
    var forumNeedsRefresh by rememberSaveable { mutableStateOf(false) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<LoginRoute> {
                LoginScreen(
                    onLoginSuccess = { backStack.replaceAll(HomeRoute) },
                    onNavigateToSignUp = { backStack.add(SignUpRoute) },
                    onNavigateToForgotPassword = { backStack.add(ForgotPasswordRoute) },
                    onNavigateToEmailVerification = { email -> backStack.add(EmailVerificationRoute(email)) }
                )
            }
            entry<ForgotPasswordRoute> {
                ForgotPasswordScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onPasswordResetSuccess = { backStack.removeLastOrNull() }
                )
            }
            entry<SignUpRoute> {
                SignUpScreen(
                    onSignUpSuccess = { email -> backStack.add(EmailVerificationRoute(email)) },
                    onNavigateToLogin = { backStack.replaceAll(LoginRoute) }
                )
            }
            entry<EmailVerificationRoute> { key ->
                EmailVerificationScreen(
                    email = key.email,
                    onBack = { backStack.removeLastOrNull() },
                    // Email verified, but the account was never signed in. Send the
                    // user to Login to obtain a session with their verified account.
                    onEmailVerified = { backStack.replaceAll(LoginRoute) }
                )
            }

            entry<HomeRoute> {
                val forumViewModel: ForumViewModel = koinViewModel()
                LaunchedEffect(forumNeedsRefresh) {
                    if (forumNeedsRefresh) {
                        forumViewModel.onIntent(ForumIntent.Refresh)
                        forumNeedsRefresh = false
                    }
                }
                HomeScreen(
                    onViewAllSchedule = { backStack.add(ScheduleRoute) },
                    onViewAllStandings = { backStack.add(StandingsRoute) },
                    onCreateThread = { backStack.add(CreateThreadRoute) },
                    onThreadClick = { thread -> backStack.add(ThreadDetailRoute(thread)) },
                    onSignedOut = { backStack.replaceAll(LoginRoute) },
                    onViewRaceDetail = { race -> backStack.add(RaceDetailRoute(race)) }
                )
            }
            entry<ScheduleRoute> {
                val viewModel: ScheduleViewModel = koinViewModel()
                val schedule by viewModel.schedule.collectAsStateWithLifecycle()
                ScheduleScreen(
                    schedule = schedule,
                    onBack = { backStack.removeLastOrNull() },
                    onViewRaceDetail = { race -> backStack.add(RaceDetailRoute(race)) }
                )
            }
            entry<StandingsRoute> {
                val viewModel: StandingsViewModel = koinViewModel()
                val drivers by viewModel.drivers.collectAsStateWithLifecycle()
                val constructors by viewModel.constructors.collectAsStateWithLifecycle()
                StandingsScreen(
                    drivers = drivers,
                    constructors = constructors,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<CreateThreadRoute> {
                CreateThreadScreen(
                    onCancel = { backStack.removeLastOrNull() },
                    onThreadCreated = {
                        forumNeedsRefresh = true
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<ThreadDetailRoute> { key ->
                ThreadDetailScreen(
                    thread = key.thread,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<RaceDetailRoute> { key ->
                val viewModel: RaceDetailViewModel = koinViewModel { parametersOf(key.race.id) }
                val state by viewModel.state.collectAsStateWithLifecycle()
                RaceDetailScreen(
                    race = key.race,
                    raceDetail = state.detail,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

/** Replaces the whole back stack with [route], e.g. after signing in or out. */
private fun NavBackStack<NavKey>.replaceAll(route: NavKey) {
    clear()
    add(route)
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
