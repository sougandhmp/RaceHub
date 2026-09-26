package org.gce.racehub.signup

import org.gce.racehub.auth.presentation.SignUpEffect
import org.gce.racehub.auth.presentation.SignUpIntent
import org.gce.racehub.auth.presentation.SignUpState
import org.gce.racehub.auth.presentation.SignUpViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gce.racehub.theme.AppColorTokens
import org.gce.racehub.theme.Dimens
import org.gce.racehub.theme.hexColor
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import racehub.composeapp.generated.resources.Res
import racehub.composeapp.generated.resources.action_create_account
import racehub.composeapp.generated.resources.action_hide
import racehub.composeapp.generated.resources.action_show
import racehub.composeapp.generated.resources.action_sign_in
import racehub.composeapp.generated.resources.label_confirm_password
import racehub.composeapp.generated.resources.label_country
import racehub.composeapp.generated.resources.label_email
import racehub.composeapp.generated.resources.label_password
import racehub.composeapp.generated.resources.label_username
import racehub.composeapp.generated.resources.signup_already_have_account
import racehub.composeapp.generated.resources.signup_select_country_hint
import racehub.composeapp.generated.resources.signup_tagline
import racehub.composeapp.generated.resources.signup_title

private val RacingRed = hexColor(AppColorTokens.racingRed)
private val DarkBg    = hexColor(AppColorTokens.darkBackground)
private val DarkBlue  = hexColor(AppColorTokens.authDarkBlue)
private val DeepBlue  = hexColor(AppColorTokens.authDeepBlue)
private val MutedGray = hexColor(AppColorTokens.authMuted)
private val DimBorder = hexColor(AppColorTokens.authDimBorder)

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = koinViewModel(),
    onSignUpSuccess: (email: String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SignUpEffect.NavigateToEmailVerification -> onSignUpSuccess(effect.email)
            }
        }
    }

    SignUpScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateToLogin = onNavigateToLogin
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignUpScreenContent(
    state: SignUpState,
    onIntent: (SignUpIntent) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DarkBg, DarkBlue, DeepBlue)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = Dimens.screenGutter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(text = "🏁", fontSize = 56.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(Res.string.signup_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = RacingRed,
                fontSize = 32.sp
            )

            Text(
                text = stringResource(Res.string.signup_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MutedGray
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Username
            OutlinedTextField(
                value = state.username,
                onValueChange = { onIntent(SignUpIntent.UsernameChanged(it)) },
                label = { Text(stringResource(Res.string.label_username)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Email
            OutlinedTextField(
                value = state.email,
                onValueChange = { onIntent(SignUpIntent.EmailChanged(it)) },
                label = { Text(stringResource(Res.string.label_email)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password
            OutlinedTextField(
                value = state.password,
                onValueChange = { onIntent(SignUpIntent.PasswordChanged(it)) },
                label = { Text(stringResource(Res.string.label_password)) },
                singleLine = true,
                visualTransformation = if (state.isPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    TextButton(onClick = { onIntent(SignUpIntent.TogglePasswordVisibility) }) {
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

            // Confirm Password
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { onIntent(SignUpIntent.ConfirmPasswordChanged(it)) },
                label = { Text(stringResource(Res.string.label_confirm_password)) },
                singleLine = true,
                visualTransformation = if (state.isConfirmPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    TextButton(onClick = { onIntent(SignUpIntent.ToggleConfirmPasswordVisibility) }) {
                        Text(
                            text = if (state.isConfirmPasswordVisible) stringResource(Res.string.action_hide) else stringResource(Res.string.action_show),
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

            // Country
            CountryDropdown(
                selectedCode = state.country,
                onCountrySelected = { onIntent(SignUpIntent.CountryChanged(it)) }
            )

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
                onClick = { onIntent(SignUpIntent.Submit) },
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
                        text = stringResource(Res.string.action_create_account),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.signup_already_have_account),
                    color = MutedGray,
                    fontSize = 14.sp
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = stringResource(Res.string.action_sign_in),
                        color = RacingRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDropdown(
    selectedCode: String,
    onCountrySelected: (String) -> Unit
) {
    val countries = remember {
        Locale.getISOCountries()
            .map { code -> Locale.Builder().setRegion(code).build().displayCountry to code }
            .filter { it.first.isNotEmpty() }
            .sortedBy { it.first }
    }
    var expanded by remember { mutableStateOf(false) }
    val selectedName = remember(selectedCode) {
        countries.find { it.second == selectedCode }?.first ?: ""
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.label_country)) },
            placeholder = { Text(stringResource(Res.string.signup_select_country_hint), color = MutedGray) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DarkBlue)
        ) {
            countries.forEach { (name, code) ->
                DropdownMenuItem(
                    text = { Text(name, color = Color.White) },
                    onClick = {
                        onCountrySelected(code)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
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

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    SignUpScreenContent(
        state = SignUpState(username = "MaxVerstappen", email = "max@redbull.com", country = "NL"),
        onIntent = {},
        onNavigateToLogin = {}
    )
}
