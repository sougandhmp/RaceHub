package org.gce.racehub.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gce.racehub.theme.AppColorScheme
import org.gce.racehub.theme.DarkAppColors
import org.gce.racehub.theme.LocalAppColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateThreadScreen(
    onCancel: () -> Unit,
    onThreadCreated: () -> Unit,
    viewModel: CreateThreadViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CreateThreadEffect.ThreadCreated -> onThreadCreated()
            }
        }
    }

    CreateThreadContent(
        state = state,
        colors = colors,
        onIntent = viewModel::onIntent,
        onCancel = onCancel
    )
}

@Composable
private fun CreateThreadContent(
    state: CreateThreadState,
    colors: AppColorScheme,
    onIntent: (CreateThreadIntent) -> Unit,
    onCancel: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
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
                    Text(text = "Cancel", color = colors.mutedText, fontSize = 14.sp)
                }
                Text(
                    text = "New Thread",
                    color = colors.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(64.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            FieldLabel("TITLE", colors)
            OutlinedTextField(
                value = state.title,
                onValueChange = { onIntent(CreateThreadIntent.TitleChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What's on your mind?", color = colors.mutedText) },
                singleLine = true,
                enabled = !state.isSubmitting,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = fieldColors(colors)
            )

            Spacer(modifier = Modifier.height(16.dp))
            FieldLabel("TAG", colors)
            CategoryPicker(
                selected = state.category,
                enabled = !state.isSubmitting,
                onSelect = { onIntent(CreateThreadIntent.CategoryChanged(it)) },
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))
            FieldLabel("CONTENT", colors)
            OutlinedTextField(
                value = state.content,
                onValueChange = { onIntent(CreateThreadIntent.ContentChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                placeholder = { Text("Share your thoughts…", color = colors.mutedText) },
                enabled = !state.isSubmitting,
                colors = fieldColors(colors)
            )

            state.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = message, color = colors.racingRed, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onIntent(CreateThreadIntent.Submit) },
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.racingRed,
                    contentColor = Color.White,
                    disabledContainerColor = colors.card,
                    disabledContentColor = colors.mutedText
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

@Preview(showBackground = true)
@Composable
private fun CreateThreadScreenPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkAppColors) {
        CreateThreadContent(
            state = CreateThreadState(
                title = "Ferrari strategy blunder in Monaco?",
                category = "Race Weekends",
                content = "They undercut at the wrong time and ended up behind both McLarens..."
            ),
            colors = DarkAppColors,
            onIntent = {},
            onCancel = {}
        )
    }
}

private val THREAD_TAGS = listOf(
    "General Discussion",
    "Race Weekends",
    "Teams & Drivers",
    "Technical / Cars"
)

@Composable
private fun CategoryPicker(
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
    colors: AppColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        THREAD_TAGS.forEach { tag ->
            TagChip(
                label = tag,
                isSelected = selected == tag,
                enabled = enabled,
                onClick = { onSelect(tag) },
                colors = colors
            )
        }
    }
}

@Composable
private fun TagChip(
    label: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    colors: AppColorScheme
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) colors.racingRed else colors.card)
            .border(
                width = 1.dp,
                color = if (isSelected) colors.racingRed else colors.cardBorder,
                shape = RoundedCornerShape(50)
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else colors.primaryText,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun FieldLabel(text: String, colors: AppColorScheme) {
    Text(
        text = text,
        color = colors.mutedText,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun fieldColors(colors: AppColorScheme) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = colors.primaryText,
    unfocusedTextColor = colors.primaryText,
    focusedContainerColor = colors.card,
    unfocusedContainerColor = colors.card,
    focusedBorderColor = colors.racingRed,
    unfocusedBorderColor = colors.cardBorder,
    cursorColor = colors.racingRed
)
