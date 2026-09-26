package org.gce.racehub.forgotpassword

import org.gce.racehub.auth.presentation.ForgotPasswordEffect
import org.gce.racehub.auth.presentation.ForgotPasswordIntent
import org.gce.racehub.auth.presentation.ForgotPasswordState
import org.gce.racehub.auth.presentation.ForgotPasswordStep
import org.gce.racehub.auth.presentation.ForgotPasswordViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import racehub.composeapp.generated.resources.action_hide
import racehub.composeapp.generated.resources.action_send_reset_code
import racehub.composeapp.generated.resources.action_show
import racehub.composeapp.generated.resources.contentdesc_back
import racehub.composeapp.generated.resources.forgot_password_confirm_subtitle
import racehub.composeapp.generated.resources.forgot_password_request_subtitle
import racehub.composeapp.generated.resources.forgot_password_request_title
import racehub.composeapp.generated.resources.forgot_password_reset_title
import racehub.composeapp.generated.resources.label_confirm_new_password
import racehub.composeapp.generated.resources.label_email
import racehub.composeapp.generated.resources.label_new_password
import racehub.composeapp.generated.resources.label_reset_code

private val RacingRed = hexColor(AppColorTokens.racingRed)
private val DarkBg    = hexColor(AppColorTokens.darkBackground)
private val DarkBlue  = hexColor(AppColorTokens.authDarkBlue)
private val DeepBlue  = hexColor(AppColorTokens.authDeepBlue)
private val MutedGray = hexColor(AppColorTokens.authMuted)
private val DimBorder = hexColor(AppColorTokens.authDimBorder)

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = koinViewModel(),
    onBack: () -> Unit,
    onPasswordResetSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ForgotPasswordEffect.PasswordResetSuccess -> onPasswordResetSuccess()
            }
        }
    }

    ForgotPasswordContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack
    )
}

@Composable
private fun ForgotPasswordContent(
    state: ForgotPasswordState,
    onIntent: (ForgotPasswordIntent) -> Unit,
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

            Text(text = "🔑", fontSize = 56.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (state.step == ForgotPasswordStep.Request)
                    stringResource(Res.string.forgot_password_request_title)
                else
                    stringResource(Res.string.forgot_password_reset_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = RacingRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (state.step == ForgotPasswordStep.Request)
                    stringResource(Res.string.forgot_password_request_subtitle)
                else
                    stringResource(Res.string.forgot_password_confirm_subtitle, state.email),
                color = MutedGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (state.step == ForgotPasswordStep.Request) {
                RequestStep(state = state, onIntent = onIntent, focusManager = focusManager)
            } else {
                ConfirmStep(state = state, onIntent = onIntent, focusManager = focusManager)
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = state.errorMessage.orEmpty(),
                    color = RacingRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (state.step == ForgotPasswordStep.Request) onIntent(ForgotPasswordIntent.RequestReset)
                    else onIntent(ForgotPasswordIntent.ConfirmReset)
                },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RacingRed,
                    disabledContainerColor = RacingRed.copy(alpha = 0.4f)
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (state.step == ForgotPasswordStep.Request)
                            stringResource(Res.string.action_send_reset_code)
                        else
                            stringResource(Res.string.forgot_password_reset_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RequestStep(
    state: ForgotPasswordState,
    onIntent: (ForgotPasswordIntent) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    OutlinedTextField(
        value = state.email,
        onValueChange = { onIntent(ForgotPasswordIntent.EmailChanged(it)) },
        label = { Text(stringResource(Res.string.label_email)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            onIntent(ForgotPasswordIntent.RequestReset)
        }),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = textFieldColors()
    )
}

@Composable
private fun ConfirmStep(
    state: ForgotPasswordState,
    onIntent: (ForgotPasswordIntent) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    OutlinedTextField(
        value = state.otp,
        onValueChange = { onIntent(ForgotPasswordIntent.OtpChanged(it)) },
        label = { Text(stringResource(Res.string.label_reset_code)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = textFieldColors()
    )

    Spacer(modifier = Modifier.height(14.dp))

    OutlinedTextField(
        value = state.newPassword,
        onValueChange = { onIntent(ForgotPasswordIntent.NewPasswordChanged(it)) },
        label = { Text(stringResource(Res.string.label_new_password)) },
        singleLine = true,
        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        trailingIcon = {
            TextButton(onClick = { onIntent(ForgotPasswordIntent.TogglePasswordVisibility) }) {
                Text(
                    text = if (state.isPasswordVisible) stringResource(Res.string.action_hide) else stringResource(Res.string.action_show),
                    color = MutedGray,
                    fontSize = 12.sp
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = textFieldColors()
    )

    Spacer(modifier = Modifier.height(14.dp))

    OutlinedTextField(
        value = state.confirmPassword,
        onValueChange = { onIntent(ForgotPasswordIntent.ConfirmPasswordChanged(it)) },
        label = { Text(stringResource(Res.string.label_confirm_new_password)) },
        singleLine = true,
        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            onIntent(ForgotPasswordIntent.ConfirmReset)
        }),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = textFieldColors()
    )
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

@Preview(showBackground = true, name = "Step 1 – Request")
@Composable
private fun ForgotPasswordRequestPreview() {
    ForgotPasswordContent(
        state = ForgotPasswordState(),
        onIntent = {},
        onBack = {}
    )
}

@Preview(showBackground = true, name = "Step 2 – Confirm")
@Composable
private fun ForgotPasswordConfirmPreview() {
    ForgotPasswordContent(
        state = ForgotPasswordState(step = ForgotPasswordStep.Confirm, email = "driver@racehub.com"),
        onIntent = {},
        onBack = {}
    )
}
