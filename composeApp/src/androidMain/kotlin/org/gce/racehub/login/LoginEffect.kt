package org.gce.racehub.login

/**
 * One-time side effects produced by [LoginViewModel].
 *
 * Effects represent events that must not be replayed on recomposition (e.g.
 * navigation triggers). They are delivered via a [kotlinx.coroutines.channels.Channel]
 * so they survive config changes and are consumed exactly once.
 */
sealed class LoginEffect {

    /** Instructs the host composable to navigate away from Login to Home. */
    data object NavigateToHome : LoginEffect()

    /**
     * The credentials were valid but the email is not yet verified. The host
     * composable should route to email verification (carrying [email]) instead
     * of granting access.
     */
    data class NavigateToEmailVerification(val email: String) : LoginEffect()
}
