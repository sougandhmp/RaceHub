package org.gce.racehub.forum

import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.race.domain.model.ThreadAuthor
import org.gce.racehub.race.domain.model.ThreadComment
import org.gce.racehub.theme.AppColorScheme
import org.gce.racehub.theme.DarkAppColors
import org.gce.racehub.theme.Dimens
import org.gce.racehub.theme.LocalAppColors
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import racehub.composeapp.generated.resources.Res
import racehub.composeapp.generated.resources.action_reply
import racehub.composeapp.generated.resources.action_share
import racehub.composeapp.generated.resources.contentdesc_back
import racehub.composeapp.generated.resources.contentdesc_post_reply
import racehub.composeapp.generated.resources.label_anonymous
import racehub.composeapp.generated.resources.label_just_now
import racehub.composeapp.generated.resources.label_original_poster
import racehub.composeapp.generated.resources.thread_no_replies
import racehub.composeapp.generated.resources.thread_one_reply
import racehub.composeapp.generated.resources.thread_replies
import racehub.composeapp.generated.resources.thread_reply_placeholder
import racehub.composeapp.generated.resources.title_thread_detail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadDetailScreen(
    thread: Thread,
    onBack: () -> Unit,
    viewModel: ThreadDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.initLikes(thread.likes) }

    val onShare = {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, thread.title)
            putExtra(
                Intent.EXTRA_TEXT,
                "${thread.title}\n\n${thread.content}\n\n— shared from RaceHub"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share thread"))
    }

    ThreadDetailContent(
        thread = thread,
        state = state,
        colors = colors,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onShare = onShare
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThreadDetailContent(
    thread: Thread,
    state: ThreadDetailState,
    colors: AppColorScheme,
    onIntent: (ThreadDetailIntent) -> Unit,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val commentFocusRequester = remember { FocusRequester() }
    val allComments = thread.comments + state.postedComments

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            onIntent(ThreadDetailIntent.DismissError)
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
                        text = stringResource(Res.string.title_thread_detail),
                        color = colors.primaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.contentdesc_back),
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
                onValueChange = { onIntent(ThreadDetailIntent.CommentInputChanged(it)) },
                canSubmit = state.canSubmit,
                isSubmitting = state.isSubmitting,
                onSubmit = { onIntent(ThreadDetailIntent.SubmitComment(thread.id)) },
                focusRequester = commentFocusRequester,
                colors = colors
            )
        },
        containerColor = colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                ThreadPostCard(
                    thread = thread,
                    likes = state.likes,
                    isLiked = state.isLiked,
                    isLiking = state.isLiking,
                    colors = colors,
                    onLikeClick = { onIntent(ThreadDetailIntent.ToggleLike(thread.id)) },
                    onReplyClick = { commentFocusRequester.requestFocus() },
                    onShareClick = onShare
                )
            }

            item {
                Text(
                    text = if (allComments.size == 1) stringResource(Res.string.thread_one_reply) else stringResource(Res.string.thread_replies, allComments.size),
                    color = colors.mutedText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            if (allComments.isEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.thread_no_replies),
                        color = colors.mutedText,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(allComments) { comment ->
                    CommentCard(comment = comment, colors = colors)
                }
            }
        }
    }
}

@Composable
private fun ThreadPostCard(
    thread: Thread,
    likes: Int,
    isLiked: Boolean,
    isLiking: Boolean,
    colors: AppColorScheme,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Dimens.cardShape)
            .background(colors.card)
            .border(1.dp, colors.cardBorder, Dimens.cardShape)
            .padding(20.dp)
    ) {
        // Category · date
        Text(
            text = "${thread.category} · ${formatTimestamp(thread.createdAt)}",
            color = colors.mutedText,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Text(
            text = thread.title,
            color = colors.primaryText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Author
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuthorBadge(
                initials = thread.author.avatar.ifBlank { thread.author.username },
                colors = colors
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                val anonymousLabel = stringResource(Res.string.label_anonymous)
                Text(
                    text = thread.author.username.ifBlank { anonymousLabel },
                    color = colors.primaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.label_original_poster),
                    color = colors.mutedText,
                    fontSize = 12.sp
                )
            }
        }

        if (thread.content.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = thread.content,
                color = colors.primaryText,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = colors.cardBorder, thickness = 0.5.dp)

        Spacer(modifier = Modifier.height(12.dp))

        // Action row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionPill(
                icon = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                label = likes.toString(),
                active = isLiked,
                activeColor = colors.racingRed,
                borderColor = colors.cardBorder,
                mutedColor = colors.mutedText,
                enabled = !isLiking,
                onClick = onLikeClick
            )
            ActionPill(
                icon = Icons.AutoMirrored.Filled.Send,
                label = stringResource(Res.string.action_reply),
                active = false,
                activeColor = colors.racingRed,
                borderColor = colors.cardBorder,
                mutedColor = colors.mutedText,
                onClick = onReplyClick
            )
            ActionPill(
                icon = Icons.Filled.Share,
                label = stringResource(Res.string.action_share),
                active = false,
                activeColor = colors.racingRed,
                borderColor = colors.cardBorder,
                mutedColor = colors.mutedText,
                onClick = onShareClick
            )
        }
    }
}

@Composable
private fun ActionPill(
    icon: ImageVector,
    label: String,
    active: Boolean,
    activeColor: Color,
    borderColor: Color,
    mutedColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(Dimens.cardShape)
            .background(if (active) activeColor.copy(alpha = 0.10f) else Color.Transparent)
            .border(
                1.dp,
                if (active) activeColor.copy(alpha = 0.4f) else borderColor,
                Dimens.cardShape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) activeColor else mutedColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            color = if (active) activeColor else mutedColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
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
                sizeDp = 30
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                val anonymousLabel = stringResource(Res.string.label_anonymous)
                Text(
                    text = comment.authorUsername.ifBlank { anonymousLabel },
                    color = colors.primaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(Res.string.label_just_now),
                color = colors.mutedText,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = comment.content,
            color = colors.primaryText,
            fontSize = 14.sp,
            lineHeight = 20.sp
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
    focusRequester: FocusRequester,
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
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            enabled = !isSubmitting,
            placeholder = { Text(stringResource(Res.string.thread_reply_placeholder), color = colors.mutedText) },
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
                contentDescription = stringResource(Res.string.contentdesc_post_reply),
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

private fun formatTimestamp(createdAt: String): String {
    val datePart = createdAt.substringBefore('T')
    val timePart = createdAt.substringAfter('T', "").substringBefore('.').take(5)
    return if (timePart.isNotEmpty()) "$datePart · $timePart" else datePart
}

@Preview(showBackground = true)
@Composable
private fun ThreadDetailScreenPreview() {
    val thread = Thread(
        id = "42",
        title = "Verstappen vs Norris — who was faster in Monaco quali?",
        category = "Race Weekends",
        author = ThreadAuthor(username = "PaddockInsider", avatar = "PI"),
        excerpt = null,
        content = "Both drivers looked incredibly quick in the final sector. Let's break down the telemetry...",
        createdAt = "2025-05-24T10:15:00Z",
        likes = 89,
        bookmarked = false,
        comments = listOf(
            ThreadComment("Norris had better exit speed in Rascasse.", "TechF1"),
            ThreadComment("Max was on older tyres though.", "RedBullFanatic")
        )
    )
    CompositionLocalProvider(LocalAppColors provides DarkAppColors) {
        ThreadDetailContent(
            thread = thread,
            state = ThreadDetailState(likes = 89, commentInput = ""),
            colors = DarkAppColors,
            onIntent = {},
            onBack = {},
            onShare = {}
        )
    }
}
