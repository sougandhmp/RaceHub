package org.gce.racehub

import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
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
import org.gce.racehub.race.presentation.weekendSessions

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
import androidx.lifecycle.viewmodel.compose.viewModel
import org.gce.racehub.navigation.SignedInScope
import org.gce.racehub.navigation.SignedInScopeHolder
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gce.racehub.auth.di.createProductionAuthModule
import org.gce.racehub.core.domain.session.UserSession
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

/** Replaces the whole back stack with [route] (after email verification). */
private fun NavBackStack<NavKey>.resetTo(route: NavKey) {
    add(route)
    while (size > 1) removeAt(0)
}

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
            val signedInScope = viewModel { SignedInScopeHolder() }
            var isSignedIn by rememberSaveable { mutableStateOf(userSession.currentUser.value != null) }

            // Two flows with separate back stacks: nothing from the signed-in
            // side (and none of its network calls) exists before sign-in, and
            // signing out throws all of it away.
            if (isSignedIn) {
                SignedInScope(signedInScope) {
                    SignedInNavigation(onSignedOut = {
                        signedInScope.clear()
                        isSignedIn = false
                    })
                }
            } else {
                SignedOutNavigation(onSignedIn = { isSignedIn = true })
            }
        }
    }
}

@Composable
private fun SignedOutNavigation(onSignedIn: () -> Unit) {
    val backStack = rememberNavBackStack(LoginRoute)
    fun pop() { backStack.removeLastOrNull() }

    NavDisplay(
        backStack = backStack,
        onBack = ::pop,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<LoginRoute> {
                LoginScreen(
                    onLoginSuccess = onSignedIn,
                    onNavigateToSignUp = { backStack.add(SignUpRoute) },
                    onNavigateToForgotPassword = { backStack.add(ForgotPasswordRoute) },
                    onNavigateToEmailVerification = { email -> backStack.add(EmailVerificationRoute(email)) }
                )
            }
            entry<SignUpRoute> {
                SignUpScreen(
                    onSignUpSuccess = { email -> backStack.add(EmailVerificationRoute(email)) },
                    onNavigateToLogin = ::pop
                )
            }
            entry<ForgotPasswordRoute> {
                ForgotPasswordScreen(onBack = ::pop, onPasswordResetSuccess = ::pop)
            }
            entry<EmailVerificationRoute> { key ->
                EmailVerificationScreen(
                    email = key.email,
                    onBack = ::pop,
                    // Verified but never signed in: sign in with the verified account.
                    onEmailVerified = { backStack.resetTo(LoginRoute) }
                )
            }
        }
    )
}

@Composable
private fun SignedInNavigation(onSignedOut: () -> Unit) {
    val backStack = rememberNavBackStack(HomeRoute)
    fun pop() { backStack.removeLastOrNull() }

    // Race and forum data back several destinations (home tabs, calendar,
    // standings, details), so these two live in the signed-in scope and are
    // shared. Every other screen's ViewModel is scoped to its own entry.
    val raceViewModel: RaceViewModel = koinViewModel()
    val forumViewModel: ForumViewModel = koinViewModel()
    val raceState by raceViewModel.state.collectAsStateWithLifecycle()
    val forumState by forumViewModel.state.collectAsStateWithLifecycle()

    NavDisplay(
        backStack = backStack,
        onBack = ::pop,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen(
                    raceViewModel = raceViewModel,
                    forumViewModel = forumViewModel,
                    onViewAllSchedule = { backStack.add(ScheduleRoute) },
                    onViewAllStandings = { backStack.add(StandingsRoute) },
                    onCreateThread = { backStack.add(CreateThreadRoute) },
                    onThreadClick = { thread -> backStack.add(ThreadDetailRoute(thread.id)) },
                    onSignedOut = onSignedOut,
                    onViewRaceDetail = { race -> backStack.add(RaceDetailRoute(race.id)) }
                )
            }
            entry<ScheduleRoute> {
                ScheduleScreen(
                    schedule = raceState.raceSchedule,
                    onBack = ::pop,
                    onViewRaceDetail = { race -> backStack.add(RaceDetailRoute(race.id)) }
                )
            }
            entry<StandingsRoute> {
                StandingsScreen(
                    drivers = raceState.driverStandings,
                    constructors = raceState.constructorStandings,
                    onBack = ::pop
                )
            }
            entry<CreateThreadRoute> {
                CreateThreadScreen(
                    onCancel = ::pop,
                    onThreadCreated = {
                        forumViewModel.onIntent(ForumIntent.Refresh)
                        pop()
                    }
                )
            }
            entry<ThreadDetailRoute> { key ->
                // After process death the list reloads; show nothing until it does.
                forumState.threads.firstOrNull { it.id == key.threadId }?.let { thread ->
                    ThreadDetailScreen(thread = thread, onBack = ::pop)
                }
            }
            entry<RaceDetailRoute> { key ->
                LaunchedEffect(key.slug) { raceViewModel.onIntent(RaceIntent.SelectRace(key.slug)) }
                raceState.raceSchedule.firstOrNull { it.id == key.slug }?.let { race ->
                    val isSelected = raceState.selectedRace?.id == key.slug
                    RaceDetailScreen(
                        race = race,
                        raceDetail = raceState.selectedRaceDetail.takeIf { isSelected },
                        sessions = if (isSelected) raceState.selectedRaceSessions else weekendSessions(race, null),
                        onBack = ::pop
                    )
                }
            }
        }
    )
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
