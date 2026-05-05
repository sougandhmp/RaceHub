package org.gce.racehub.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import org.koin.androidx.compose.koinViewModel

private val RacingRed = Color(0xFFE63946)
private val DarkBg = Color(0xFF0A0A0A)
private val DarkBlue = Color(0xFF1A1A2E)
private val DeepBlue = Color(0xFF16213E)
private val MutedGray = Color(0xFF8D99AE)
private val DimBorder = Color(0xFF444444)

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = koinViewModel(),
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SignUpEffect.NavigateToHome -> onSignUpSuccess()
            }
        }
    }

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
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(text = "🏁", fontSize = 56.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Join RaceHub",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = RacingRed,
                fontSize = 32.sp
            )

            Text(
                text = "Start your racing journey",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedGray
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Name
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onIntent(SignUpIntent.NameChanged(it)) },
                label = { Text("Full Name") },
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
                onValueChange = { viewModel.onIntent(SignUpIntent.EmailChanged(it)) },
                label = { Text("Email") },
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
                onValueChange = { viewModel.onIntent(SignUpIntent.PasswordChanged(it)) },
                label = { Text("Password") },
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
                    TextButton(onClick = { viewModel.onIntent(SignUpIntent.TogglePasswordVisibility) }) {
                        Text(
                            text = if (state.isPasswordVisible) "Hide" else "Show",
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
                onValueChange = { viewModel.onIntent(SignUpIntent.ConfirmPasswordChanged(it)) },
                label = { Text("Confirm Password") },
                singleLine = true,
                visualTransformation = if (state.isConfirmPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.onIntent(SignUpIntent.SignUp)
                    }
                ),
                trailingIcon = {
                    TextButton(onClick = { viewModel.onIntent(SignUpIntent.ToggleConfirmPasswordVisibility) }) {
                        Text(
                            text = if (state.isConfirmPasswordVisible) "Hide" else "Show",
                            color = MutedGray,
                            fontSize = 12.sp
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = state.errorMessage!!,
                    color = RacingRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.onIntent(SignUpIntent.SignUp) },
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
                        text = "Create Account",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Already have an account?",
                    color = MutedGray,
                    fontSize = 14.sp
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Sign In",
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
