package org.gce.racehub.auth.domain.model

/**
 * Why an OTP is being sent. The OTP endpoints are shared across flows, so the
 * purpose carries the email [subject] line that distinguishes each use case.
 */
enum class OtpPurpose(val subject: String) {
    EMAIL_VERIFICATION("Email verification OTP"),
    PASSWORD_RESET("Password reset OTP")
}
