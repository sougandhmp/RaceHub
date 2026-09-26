package org.gce.racehub.emailverification

import org.gce.racehub.ui.message
import org.gce.racehub.auth.presentation.EmailVerificationEffect
import org.gce.racehub.auth.presentation.EmailVerificationIntent
import org.gce.racehub.auth.presentation.EmailVerificationState
import org.gce.racehub.auth.presentation.EmailVerificationViewModel
import racehub.composeapp.generated.resources.email_verification_code_resent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gce.racehub.theme.AppColorTokens
import org.gce.racehub.theme.Dimens
import org.gce.racehub.theme.hexColor
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import racehub.composeapp.generated.resources.Res
import racehub.composeapp.generated.resources.action_resend_code
import racehub.composeapp.generated.resources.action_verify_email
import racehub.composeapp.generated.resources.contentdesc_back
import racehub.composeapp.generated.resources.email_verification_subtitle
import racehub.composeapp.generated.resources.email_verification_title
import racehub.composeapp.generated.resources.label_verification_code

private val RacingRed = hexColor(AppColorTokens.racingRed)
private val DarkBg    = hexColor(AppColorTokens.darkBackground)
private val DarkBlue  = hexColor(AppColorTokens.authDarkBlue)
private val DeepBlue  = hexColor(AppColorTokens.authDeepBlue)
private val MutedGray = hexColor(AppColorTokens.authMuted)
private val DimBorder = hexColor(AppColorTokens.authDimBorder)

@Composable
fun EmailVerificationScreen(
    email: String,
    viewModel: EmailVerificationViewModel = koinViewModel(),
    onBack: () -> Unit,
    onEmailVerified: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(email) {
        viewModel.onIntent(EmailVerificationIntent.Open(email))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is EmailVerificationEffect.EmailVerified -> onEmailVerified()
            }
        }
    }

    EmailVerificationContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack
    )
}

@Composable
private fun EmailVerificationContent(
    state: EmailVerificationState,
    onIntent: (EmailVerificationIntent) -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(DarkBg, DarkBlue, DeepBlue)))
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 8.dp, start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.contentdesc_back),
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenGutter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Text(text = "📧", fontSize = 56.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.email_verification_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = RacingRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.email_verification_subtitle, state.email),
                color = MutedGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = state.otp,
                onValueChange = { onIntent(EmailVerificationIntent.OtpChanged(it)) },
                label = { Text(stringResource(Res.string.label_verification_code)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onIntent(EmailVerificationIntent.Verify)
                }),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            if (state.codeResent) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(Res.string.email_verification_code_resent),
                    color = MutedGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = state.error?.message().orEmpty(),
                    color = RacingRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onIntent(EmailVerificationIntent.Verify)
                },
                enabled = !state.isVerifying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RacingRed,
                    disabledContainerColor = RacingRed.copy(alpha = 0.4f)
                )
            ) {
                if (state.isVerifying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.action_verify_email),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { onIntent(EmailVerificationIntent.ResendCode) },
                enabled = !state.isResending
            ) {
                Text(
                    text = stringResource(Res.string.action_resend_code),
                    color = MutedGray,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RacingRed,
    focusedLabelColor = RacingRed,
    cursorColor = RacingRed,
    unfocusedBorderColor = DimBorder,
    unfocusedLabelColor = MutedGray,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)

@Preview(showBackground = true, name = "Email Verification")
@Composable
private fun EmailVerificationPreview() {
    EmailVerificationContent(
        state = EmailVerificationState(email = "driver@racehub.com"),
        onIntent = {},
        onBack = {}
    )
}
