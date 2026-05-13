package org.gce.racehub.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.theme.AppColorScheme
import org.gce.racehub.theme.LocalAppColors
import org.koin.compose.viewmodel.koinViewModel

private val SORT_TABS = listOf(
    "Latest" to "latest",
    "Most popular" to "top",
    "Most commented" to "commented"
)

private val CATEGORY_TABS = listOf(
    "All" to null,
    "General Discussion" to "General Discussion",
    "Race Weekends" to "Race Weekends",
    "Teams & Drivers" to "Teams & Drivers",
    "Technical / Cars" to "Technical / Cars"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    viewModel: ForumViewModel = koinViewModel(),
    onCreateThread: () -> Unit,
    onThreadClick: (Thread) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.onIntent(ForumIntent.Refresh) },
        modifier = Modifier.fillMaxSize().background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FilterRow(
                selectedSort = state.selectedSort,
                selectedCategory = state.selectedCategory,
                onSortSelected = { viewModel.onIntent(ForumIntent.SelectSort(it)) },
                onCategorySelected = { viewModel.onIntent(ForumIntent.SelectCategory(it)) },
                colors = colors
            )

            when {
                state.isLoading && state.threads.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = colors.racingRed
                        )
                    }
                }

                state.threads.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No threads yet",
                            color = colors.primaryText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to start the first conversation.",
                            color = colors.mutedText,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(items = state.threads, key = { it.id }) { thread ->
                            ThreadCard(thread = thread, colors = colors, onClick = { onThreadClick(thread) })
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateThread,
            containerColor = colors.racingRed,
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
private fun FilterRow(
    selectedSort: String,
    selectedCategory: String?,
    onSortSelected: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    colors: AppColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SORT_TABS.forEach { (label, sort) ->
            FilterPill(
                label = label,
                isSelected = selectedSort == sort,
                onClick = { onSortSelected(sort) },
                colors = colors
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        CATEGORY_TABS.forEach { (label, category) ->
            FilterPill(
                label = label,
                isSelected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                colors = colors
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: AppColorScheme
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) colors.racingRed else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else colors.cardBorder,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else colors.primaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ThreadCard(thread: Thread, colors: AppColorScheme, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuthorAvatar(initials = thread.author.avatar, colors = colors)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = thread.author.username,
                    color = colors.primaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatRelative(thread.createdAt),
                    color = colors.mutedText,
                    fontSize = 12.sp
                )
            }
            CategoryChip(category = thread.category, colors = colors)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = thread.title,
            color = colors.primaryText,
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
                color = colors.mutedText,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 3
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ThreadMetric(icon = "❤", value = thread.likes.toString(), colors = colors)
            Spacer(modifier = Modifier.width(16.dp))
            ThreadMetric(icon = "💬", value = thread.comments.size.toString(), colors = colors)
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
private fun AuthorAvatar(initials: String, colors: AppColorScheme) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(colors.racingRed),
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
private fun CategoryChip(category: String, colors: AppColorScheme) {
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
private fun ThreadMetric(icon: String, value: String, colors: AppColorScheme) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 14.sp, color = colors.mutedText)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = value, color = colors.mutedText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatRelative(createdAt: String): String {
    val datePart = createdAt.substringBefore('T')
    val timePart = createdAt.substringAfter('T', missingDelimiterValue = "").substringBefore('.').take(5)
    return if (timePart.isNotEmpty()) "$datePart · $timePart" else datePart
}
