package org.gce.racehub.ui

import androidx.compose.runtime.Composable
import org.gce.racehub.auth.domain.model.AuthError
import org.gce.racehub.auth.domain.model.AuthFailure
import org.gce.racehub.core.domain.DataError
import org.jetbrains.compose.resources.stringResource
import racehub.composeapp.generated.resources.Res
import racehub.composeapp.generated.resources.auth_error_email_required
import racehub.composeapp.generated.resources.auth_error_email_password_required
import racehub.composeapp.generated.resources.auth_error_invalid_email
import racehub.composeapp.generated.resources.auth_error_password_too_short
import racehub.composeapp.generated.resources.auth_error_passwords_mismatch
import racehub.composeapp.generated.resources.auth_error_username_required
import racehub.composeapp.generated.resources.auth_error_country_required
import racehub.composeapp.generated.resources.auth_error_code_required
import racehub.composeapp.generated.resources.auth_error_rejected

/** Localized text for an auth failure; a server refusal shows the server's own message. */
@Composable
internal fun AuthFailure.message(): String = when (reason) {
    AuthError.EmailRequired -> stringResource(Res.string.auth_error_email_required)
    AuthError.EmailAndPasswordRequired -> stringResource(Res.string.auth_error_email_password_required)
    AuthError.InvalidEmail -> stringResource(Res.string.auth_error_invalid_email)
    AuthError.PasswordTooShort -> stringResource(Res.string.auth_error_password_too_short)
    AuthError.PasswordsDoNotMatch -> stringResource(Res.string.auth_error_passwords_mismatch)
    AuthError.UsernameRequired -> stringResource(Res.string.auth_error_username_required)
    AuthError.CountryRequired -> stringResource(Res.string.auth_error_country_required)
    AuthError.CodeRequired -> stringResource(Res.string.auth_error_code_required)
    AuthError.Rejected -> serverMessage ?: stringResource(Res.string.auth_error_rejected)
    AuthError.Network -> stringResource(DataError.Network.message())
    AuthError.Server -> stringResource(DataError.Server.message())
    AuthError.Unknown -> stringResource(DataError.Unknown.message())
}
