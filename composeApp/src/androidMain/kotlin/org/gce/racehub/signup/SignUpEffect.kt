package org.gce.racehub.signup

/**
 * One-time side effects produced by [SignUpViewModel].
 *
 * Effects represent events that must not be replayed on recomposition (e.g.
 * navigation triggers). They are delivered via a [kotlinx.coroutines.channels.Channel]
 * so they survive config changes and are consumed exactly once.
 */
sealed class SignUpEffect {

    /** Instructs the host composable to navigate away from Sign-Up to Home. */
    data object NavigateToHome : SignUpEffect()
}
