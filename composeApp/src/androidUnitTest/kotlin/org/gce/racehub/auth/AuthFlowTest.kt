package org.gce.racehub.auth

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.gce.racehub.App
import org.gce.racehub.core.domain.model.User
import org.gce.racehub.core.domain.session.SessionStorage
import org.gce.racehub.di.KoinInitializer
import org.gce.racehub.di.appModule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * The signed-out flow end to end: real screens, real shared ViewModels and use
 * cases from the production Koin graph. Nothing here reaches a server: every
 * step either navigates or fails validation before a request would be made,
 * and the base URL points nowhere in case one ever is.
 */
@RunWith(RobolectricTestRunner::class)
// A phone-sized screen: Robolectric's default is so small the auth links fall off it.
@Config(application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
class AuthFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val loginScreen = hasText("Your Racing Universe")
    private val signInButton = hasText("Sign In") and hasClickAction()

    @Before
    fun setUp() {
        KoinInitializer.setApplication(RuntimeEnvironment.getApplication())
        // The encrypted session store needs the Android Keystore, which Robolectric
        // lacks; later modules override earlier ones, so swap in an empty one.
        KoinInitializer.init(
            baseUrl = "http://127.0.0.1:9",
            appModule,
            module { single<SessionStorage> { InMemorySessionStorage() } }
        )
        compose.setContent { App() }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun signedOutUsersStartOnLogin() {
        compose.onNode(loginScreen).assertExists()
        compose.onNode(signInButton).assertExists()
    }

    @Test
    fun submittingAnEmptyFormShowsTheRequiredFieldsError() {
        compose.onNode(signInButton).performClick()

        compose.onNode(hasText("Enter your email and password.")).assertExists()
        compose.onNode(loginScreen).assertExists()
    }

    @Test
    fun anInvalidEmailIsRejectedBeforeAnyRequest() {
        compose.onNode(hasSetTextAction() and hasText("Email")).performTextInput("not-an-email")
        compose.onNode(hasSetTextAction() and hasText("Password")).performTextInput("secret1")
        compose.onNode(signInButton).performClick()

        compose.onNode(hasText("Enter a valid email address.")).assertExists()
    }

    @Test
    fun signUpOpensAndBackReturnsToLogin() {
        compose.onNode(hasText("Sign Up") and hasClickAction()).performClick()
        compose.onNode(hasText("Join RaceHub")).assertExists()

        pressBack()

        compose.onNode(loginScreen).assertExists()
    }

    @Test
    fun forgotPasswordOpensAndBackReturnsToLogin() {
        compose.onNode(hasText("Forgot Password?") and hasClickAction()).performClick()
        compose.onNode(hasText("Enter your email and we'll send you a reset code.")).assertExists()

        pressBack()

        compose.onNode(loginScreen).assertExists()
    }

    private fun pressBack() {
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
    }
}

private class InMemorySessionStorage : SessionStorage {
    private var user: User? = null
    override fun saveUser(user: User) { this.user = user }
    override fun restoreUser(): User? = user
    override fun clearUser() { user = null }
}
