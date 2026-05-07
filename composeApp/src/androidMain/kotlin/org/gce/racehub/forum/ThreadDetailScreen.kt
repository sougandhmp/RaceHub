package org.gce.racehub.forum

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
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
import org.koin.compose.viewmodel.koinViewModel

private val DetailRed = Color(0xFFE63946)
private val DetailDarkBg = Color(0xFF0A0A0A)
private val DetailCardBg = Color(0xFF161616)
private val DetailCardBorder = Color(0xFF262626)
private val DetailMuted = Color(0xFF8E8E93)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadDetailScreen(
    thread: Thread,
    onBack: () -> Unit,
    viewModel: ThreadDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val allComments = thread.comments + state.postedComments

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
                    containerColor = DetailCardBg,
                    contentColor = Color.White,
                    actionColor = DetailRed
                )
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "THREAD",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
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
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DetailDarkBg)
            )
        },
        bottomBar = {
            CommentInputBar(
                value = state.commentInput,
                onValueChange = { viewModel.onIntent(ThreadDetailIntent.CommentInputChanged(it)) },
                canSubmit = state.canSubmit,
                isSubmitting = state.isSubmitting,
                onSubmit = { viewModel.onIntent(ThreadDetailIntent.SubmitComment(thread.id)) }
            )
        },
        containerColor = DetailDarkBg
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
                    onLikeClick = { viewModel.onIntent(ThreadDetailIntent.ToggleLike(thread.id)) }
                )
            }

            item {
                Text(
                    text = "COMMENTS · ${allComments.size}",
                    color = DetailMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (allComments.isEmpty()) {
                item {
                    Text(
                        text = "No comments yet. Be the first to reply.",
                        color = DetailMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(items = allComments) { comment ->
                    CommentCard(comment = comment)
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
    onLikeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DetailCardBg)
            .border(1.dp, DetailCardBorder, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuthorBadge(initials = thread.author.avatar.ifBlank { thread.author.username })
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = thread.author.username.ifBlank { "Anonymous" },
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatTimestamp(thread.createdAt),
                    color = DetailMuted,
                    fontSize = 12.sp
                )
            }
            CategoryPill(category = thread.category)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = thread.title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 28.sp
        )

        if (thread.content.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = thread.content,
                color = Color(0xFFE5E5E5),
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
                onClick = onLikeClick
            )
            Spacer(modifier = Modifier.width(16.dp))
            Metric(icon = "💬", value = thread.comments.size.toString())
            Spacer(modifier = Modifier.weight(1f))
            if (thread.bookmarked) {
                Text(
                    text = "★ Saved",
                    color = DetailRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CommentCard(comment: ThreadComment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DetailCardBg)
            .border(1.dp, DetailCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        AuthorBadge(initials = comment.authorUsername, sizeDp = 32)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = comment.authorUsername.ifBlank { "Anonymous" },
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                color = Color(0xFFD0D0D0),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun CommentInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DetailDarkBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            enabled = !isSubmitting,
            placeholder = { Text("Add a comment…", color = DetailMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = DetailCardBg,
                unfocusedContainerColor = DetailCardBg,
                focusedBorderColor = DetailRed,
                unfocusedBorderColor = DetailCardBorder,
                cursorColor = DetailRed
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSubmit,
            enabled = canSubmit
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Post comment",
                tint = if (canSubmit) DetailRed else DetailMuted
            )
        }
    }
}

@Composable
private fun AuthorBadge(initials: String, sizeDp: Int = 40) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(DetailRed),
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
private fun CategoryPill(category: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DetailRed.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = category.uppercase(),
            color = DetailRed,
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
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = !isLiking, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isLiked) "Unlike" else "Like",
            tint = if (isLiked) DetailRed else DetailMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = likes.toString(),
            color = if (isLiked) DetailRed else DetailMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun Metric(icon: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 14.sp, color = DetailMuted)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = value, color = DetailMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatTimestamp(createdAt: String): String {
    val datePart = createdAt.substringBefore('T')
    val timePart = createdAt.substringAfter('T', "").substringBefore('.').take(5)
    return if (timePart.isNotEmpty()) "$datePart · $timePart" else datePart
}
