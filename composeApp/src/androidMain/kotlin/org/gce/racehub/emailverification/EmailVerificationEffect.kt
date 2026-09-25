package org.gce.racehub.emailverification

sealed class EmailVerificationEffect {
    data object EmailVerified : EmailVerificationEffect()
}
