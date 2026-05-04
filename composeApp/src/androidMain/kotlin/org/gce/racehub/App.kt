package org.gce.racehub

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import org.gce.racehub.home.HomeScreen
import org.gce.racehub.login.LoginScreen
import org.gce.racehub.signup.SignUpScreen

/** Top-level navigation destinations for the app. */
private enum class Screen { Login, SignUp, Home }

/**
 * Root composable. Owns the top-level navigation state and routes each
 * [Screen] value to the appropriate screen composable.
 *
 * Navigation is intentionally kept simple (enum + `when`) here; replace
 * with Jetpack Navigation or a dedicated NavController when the app grows.
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        var screen by remember { mutableStateOf(Screen.Login) }
        when (screen) {
            Screen.Login -> LoginScreen(
                onLoginSuccess = { screen = Screen.Home },
                onNavigateToSignUp = { screen = Screen.SignUp }
            )
            Screen.SignUp -> SignUpScreen(
                onSignUpSuccess = { screen = Screen.Home },
                onNavigateToLogin = { screen = Screen.Login }
            )
            Screen.Home -> HomeScreen()
        }
    }
}
