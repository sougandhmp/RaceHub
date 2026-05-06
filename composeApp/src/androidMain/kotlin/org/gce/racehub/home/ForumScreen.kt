package org.gce.racehub.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.gce.racehub.race.domain.model.Thread

private val ForumRed = Color(0xFFE63946)
private val ForumDarkBg = Color(0xFF0A0A0A)
private val ForumCardBg = Color(0xFF161616)
private val ForumCardBorder = Color(0xFF262626)
private val ForumMuted = Color(0xFF8E8E93)

@Composable
fun ForumScreen(
    threads: List<Thread>,
    onCreateThread: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(ForumDarkBg)) {
        if (threads.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No threads yet",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap + to start the first conversation.",
                    color = ForumMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                items(items = threads, key = { it.id }) { thread ->
                    ThreadCard(thread = thread)
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateThread,
            containerColor = ForumRed,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Create thread")
        }
    }
}

@Composable
private fun ThreadCard(thread: Thread) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ForumCardBg)
            .border(1.dp, ForumCardBorder, RoundedCornerShape(20.dp))
            .clickable { /* open thread detail */ }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuthorAvatar(initials = thread.author.avatar)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = thread.author.username,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatRelative(thread.createdAt),
                    color = ForumMuted,
                    fontSize = 12.sp
                )
            }
            CategoryChip(category = thread.category)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = thread.title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 22.sp
        )

        val preview = thread.excerpt?.takeIf { it.isNotBlank() }
            ?: thread.content.take(160).let { if (thread.content.length > 160) "$it…" else it }
        if (preview.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = preview,
                color = ForumMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 3
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ThreadMetric(icon = "❤", value = thread.likes.toString())
            Spacer(modifier = Modifier.width(16.dp))
            ThreadMetric(icon = "💬", value = thread.comments.size.toString())
            Spacer(modifier = Modifier.weight(1f))
            if (thread.bookmarked) {
                Text(
                    text = "★ Saved",
                    color = ForumRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AuthorAvatar(initials: String) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(ForumRed),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.take(2).uppercase(),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CategoryChip(category: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ForumRed.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = category.uppercase(),
            color = ForumRed,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun ThreadMetric(icon: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 14.sp, color = ForumMuted)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = value, color = ForumMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

// Best-effort formatter for ISO-ish timestamps coming from the GraphQL response.
// Falls back to the date portion if anything goes wrong.
private fun formatRelative(createdAt: String): String {
    val datePart = createdAt.substringBefore('T')
    val timePart = createdAt.substringAfter('T', missingDelimiterValue = "").substringBefore('.').take(5)
    return if (timePart.isNotEmpty()) "$datePart · $timePart" else datePart
}
