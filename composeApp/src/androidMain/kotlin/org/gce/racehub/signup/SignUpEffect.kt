package org.gce.racehub.signup

/**
 * One-time side effects produced by [SignUpViewModel].
 *
 * Effects represent events that must not be replayed on recomposition (e.g.
 * navigation triggers). They are delivered via a [kotlinx.coroutines.channels.Channel]
 * so they survive config changes and are consumed exactly once.
 */
sealed class SignUpEffect {

    /**
     * Instructs the host composable to navigate to the email-verification step,
     * carrying the [email] the verification code was sent to.
     */
    data class NavigateToEmailVerification(val email: String) : SignUpEffect()
}
