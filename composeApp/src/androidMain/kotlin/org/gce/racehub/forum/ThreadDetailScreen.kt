package org.gce.racehub.forum

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.race.domain.model.ThreadComment
import org.gce.racehub.theme.AppColorScheme
import org.gce.racehub.theme.LocalAppColors
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadDetailScreen(
    thread: Thread,
    onBack: () -> Unit,
    viewModel: ThreadDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val allComments = thread.comments + state.postedComments
    var showComments by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initLikes(thread.likes)
    }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.onIntent(ThreadDetailIntent.DismissError)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colors.card,
                    contentColor = colors.primaryText,
                    actionColor = colors.racingRed
                )
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "THREAD",
                        color = colors.primaryText,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.primaryText
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, thread.title)
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${thread.title}\n\n${thread.content}\n\n— shared from RaceHub"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share thread"))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = colors.primaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        bottomBar = {
            CommentInputBar(
                value = state.commentInput,
                onValueChange = { viewModel.onIntent(ThreadDetailIntent.CommentInputChanged(it)) },
                canSubmit = state.canSubmit,
                isSubmitting = state.isSubmitting,
                onSubmit = { viewModel.onIntent(ThreadDetailIntent.SubmitComment(thread.id)) },
                colors = colors
            )
        },
        containerColor = colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                ThreadHeader(
                    thread = thread,
                    likes = state.likes,
                    isLiked = state.isLiked,
                    isLiking = state.isLiking,
                    showComments = showComments,
                    colors = colors,
                    onLikeClick = { viewModel.onIntent(ThreadDetailIntent.ToggleLike(thread.id)) },
                    onToggleComments = { showComments = !showComments }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showComments = !showComments }
                        .padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMMENTS · ${allComments.size}",
                        color = colors.mutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (showComments) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (showComments) "Hide comments" else "Show comments",
                        tint = colors.mutedText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = showComments,
                    enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(300)) +
                            fadeIn(animationSpec = tween(300)),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(300)) +
                            fadeOut(animationSpec = tween(300))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (allComments.isEmpty()) {
                            Text(
                                text = "No comments yet. Be the first to reply.",
                                color = colors.mutedText,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            allComments.forEach { comment ->
                                CommentCard(comment = comment, colors = colors)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreadHeader(
    thread: Thread,
    likes: Int,
    isLiked: Boolean,
    isLiking: Boolean,
    showComments: Boolean,
    colors: AppColorScheme,
    onLikeClick: () -> Unit,
    onToggleComments: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuthorBadge(
                initials = thread.author.avatar.ifBlank { thread.author.username },
                colors = colors
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = thread.author.username.ifBlank { "Anonymous" },
                    color = colors.primaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatTimestamp(thread.createdAt),
                    color = colors.mutedText,
                    fontSize = 12.sp
                )
            }
            CategoryPill(category = thread.category, colors = colors)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = thread.title,
            color = colors.primaryText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 28.sp
        )

        if (thread.content.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = thread.content,
                color = colors.primaryText,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LikeButton(
                likes = likes,
                isLiked = isLiked,
                isLiking = isLiking,
                colors = colors,
                onClick = onLikeClick
            )
            Spacer(modifier = Modifier.width(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onToggleComments)
                    .background(if (showComments) colors.racingRed.copy(alpha = 0.10f) else Color.Transparent)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(text = "💬", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = thread.comments.size.toString(),
                    color = if (showComments) colors.racingRed else colors.mutedText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (thread.bookmarked) {
                Text(
                    text = "★ Saved",
                    color = colors.racingRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CommentCard(comment: ThreadComment, colors: AppColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuthorBadge(
                initials = comment.authorUsername.ifBlank { "?" },
                colors = colors,
                sizeDp = 28
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = comment.authorUsername.ifBlank { "Anonymous" },
                color = colors.primaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "just now", // Placeholder for actual time if available
                color = colors.mutedText,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = comment.content,
            color = colors.primaryText,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(start = 38.dp) // Align with text start
        )
    }
}

@Composable
private fun CommentInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
    colors: AppColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            enabled = !isSubmitting,
            placeholder = { Text("Add a comment…", color = colors.mutedText) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.primaryText,
                unfocusedTextColor = colors.primaryText,
                focusedContainerColor = colors.card,
                unfocusedContainerColor = colors.card,
                focusedBorderColor = colors.racingRed,
                unfocusedBorderColor = colors.cardBorder,
                cursorColor = colors.racingRed
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSubmit, enabled = canSubmit) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Post comment",
                tint = if (canSubmit) colors.racingRed else colors.mutedText
            )
        }
    }
}

@Composable
private fun AuthorBadge(initials: String, colors: AppColorScheme, sizeDp: Int = 40) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(colors.racingRed),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.take(2).uppercase(),
            color = Color.White,
            fontSize = (sizeDp / 3).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CategoryPill(category: String, colors: AppColorScheme) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.racingRed.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = category.uppercase(),
            color = colors.racingRed,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun LikeButton(
    likes: Int,
    isLiked: Boolean,
    isLiking: Boolean,
    colors: AppColorScheme,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = !isLiking, onClick = onClick)
            .background(if (isLiked) colors.racingRed.copy(alpha = 0.10f) else Color.Transparent)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isLiked) "Unlike" else "Like",
            tint = if (isLiked) colors.racingRed else colors.mutedText,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = likes.toString(),
            color = if (isLiked) colors.racingRed else colors.mutedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatTimestamp(createdAt: String): String {
    val datePart = createdAt.substringBefore('T')
    val timePart = createdAt.substringAfter('T', "").substringBefore('.').take(5)
    return if (timePart.isNotEmpty()) "$datePart · $timePart" else datePart
}
