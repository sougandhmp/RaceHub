package org.gce.racehub.home

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

private val Red = Color(0xFFE63946)
private val Bg = Color(0xFF0A0A0A)
private val FieldBg = Color(0xFF161616)
private val FieldBorder = Color(0xFF262626)
private val Muted = Color(0xFF8E8E93)

@Composable
fun CreateThreadScreen(
    onCancel: () -> Unit,
    onThreadCreated: () -> Unit,
    viewModel: CreateThreadViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CreateThreadEffect.ThreadCreated -> onThreadCreated()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel, enabled = !state.isSubmitting) {
                    Text(text = "Cancel", color = Muted, fontSize = 14.sp)
                }
                Text(
                    text = "New Thread",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(64.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            FieldLabel("TITLE")
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onIntent(CreateThreadIntent.TitleChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What's on your mind?", color = Muted) },
                singleLine = true,
                enabled = !state.isSubmitting,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = darkFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))
            FieldLabel("CATEGORY")
            OutlinedTextField(
                value = state.category,
                onValueChange = { viewModel.onIntent(CreateThreadIntent.CategoryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isSubmitting,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = darkFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))
            FieldLabel("CONTENT")
            OutlinedTextField(
                value = state.content,
                onValueChange = { viewModel.onIntent(CreateThreadIntent.ContentChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                placeholder = { Text("Share your thoughts…", color = Muted) },
                enabled = !state.isSubmitting,
                colors = darkFieldColors()
            )

            state.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = message, color = Red, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.onIntent(CreateThreadIntent.Submit) },
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Red,
                    contentColor = Color.White,
                    disabledContainerColor = FieldBg,
                    disabledContentColor = Muted
                )
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(text = "Post Thread", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = Muted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = FieldBg,
    unfocusedContainerColor = FieldBg,
    focusedBorderColor = Red,
    unfocusedBorderColor = FieldBorder,
    cursorColor = Red
)
