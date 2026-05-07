package org.gce.racehub.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gce.racehub.auth.domain.model.User
import org.gce.racehub.race.domain.model.UserProfile
import org.gce.racehub.theme.AppColorScheme
import org.gce.racehub.theme.LocalAppColors
import org.gce.racehub.theme.ThemeManager
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    themeManager: ThemeManager = koinInject(),
    onSignedOut: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDark by themeManager.isDarkMode.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoadingProfile) {
        if (!state.isLoadingProfile) isRefreshing = false
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.SignedOut -> onSignedOut()
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.onIntent(ProfileIntent.RefreshProfile)
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            val user = state.user
            if (user == null) {
                Text(
                    text = "Not signed in.",
                    color = colors.mutedText,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileHeader(user = user, profile = state.profile, colors = colors)
                    Spacer(modifier = Modifier.height(24.dp))
                    ProfileStats(user = user, profile = state.profile, colors = colors)
                    Spacer(modifier = Modifier.height(24.dp))
                    ProfileDetails(user = user, colors = colors)
                    Spacer(modifier = Modifier.height(24.dp))
                    ThemeToggle(
                        isDark = isDark,
                        onToggle = { themeManager.setDarkMode(it) },
                        colors = colors
                    )
                    state.profile?.let { profile ->
                        if (profile.recentThreadTitles.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            ThreadTitleSection(
                                heading = "RECENT POSTS",
                                titles = profile.recentThreadTitles,
                                colors = colors
                            )
                        }
                        if (profile.savedThreadTitles.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            ThreadTitleSection(
                                heading = "SAVED",
                                titles = profile.savedThreadTitles,
                                colors = colors
                            )
                        }
                    }
                    if (state.isLoadingProfile) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            color = colors.racingRed,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    state.errorMessage?.let { message ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = message, color = colors.racingRed, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    SignOutButton(
                        isSigningOut = state.isSigningOut,
                        colors = colors,
                        onClick = { viewModel.onIntent(ProfileIntent.SignOut) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ThemeToggle(
    isDark: Boolean,
    onToggle: (Boolean) -> Unit,
    colors: AppColorScheme
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "APPEARANCE",
            color = colors.mutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.card)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            ThemePill(
                label = "Light",
                isSelected = !isDark,
                colors = colors,
                modifier = Modifier.weight(1f),
                onClick = { onToggle(false) }
            )
            ThemePill(
                label = "Dark",
                isSelected = isDark,
                colors = colors,
                modifier = Modifier.weight(1f),
                onClick = { onToggle(true) }
            )
        }
    }
}

@Composable
private fun ThemePill(
    label: String,
    isSelected: Boolean,
    colors: AppColorScheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.racingRed else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else colors.mutedText,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileHeader(user: User, profile: UserProfile?, colors: AppColorScheme) {
    val initials = profile?.avatar?.takeIf { it.isNotBlank() }
        ?.let { it.take(2).uppercase() }
        ?: avatarInitials(user)
    val displayName = profile?.username?.takeIf { it.isNotBlank() } ?: user.name
    val handle = profile?.username?.takeIf { it.isNotBlank() } ?: user.username

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(colors.racingRed),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = displayName,
            color = colors.primaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        handle?.takeIf { it.isNotBlank() }?.let { h ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "@$h", color = colors.mutedText, fontSize = 14.sp)
        }
        user.role?.takeIf { it.isNotBlank() }?.let { role ->
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.racingRed.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = role.uppercase(),
                    color = colors.racingRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileStats(user: User, profile: UserProfile?, colors: AppColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCell(label = "POSTS", value = (profile?.postsCount ?: user.postsCount).toString(), colors = colors)
        StatCell(label = "SAVED", value = profile?.savedCount?.toString() ?: "—", colors = colors)
        StatCell(label = "COUNTRY", value = user.country?.uppercase() ?: "—", colors = colors)
    }
}

@Composable
private fun StatCell(label: String, value: String, colors: AppColorScheme) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = colors.primaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = colors.mutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun ProfileDetails(user: User, colors: AppColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        DetailRow(label = "Email", value = user.email, colors = colors)
        user.username?.takeIf { it.isNotBlank() }?.let {
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = "Username", value = it, colors = colors)
        }
        user.joinedAt?.takeIf { it.isNotBlank() }?.let {
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = "Joined", value = formatJoined(it), colors = colors)
        }
    }
}

@Composable
private fun ThreadTitleSection(heading: String, titles: List<String>, colors: AppColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = heading,
            color = colors.mutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        titles.forEachIndexed { index, title ->
            if (index > 0) Spacer(modifier = Modifier.height(10.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = colors.primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, colors: AppColorScheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.mutedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(96.dp)
        )
        Text(
            text = value,
            color = colors.primaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SignOutButton(isSigningOut: Boolean, colors: AppColorScheme, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isSigningOut,
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
        if (isSigningOut) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(text = "Sign Out", fontWeight = FontWeight.Bold)
        }
    }
}

private fun avatarInitials(user: User): String {
    val source = user.avatar?.takeIf { it.isNotBlank() }
        ?: user.name.takeIf { it.isNotBlank() }
        ?: user.email
    return source.trim().split(" ").take(2).map { it.first() }.joinToString("").uppercase()
}

private fun formatJoined(joinedAt: String?): String {
    if (joinedAt.isNullOrBlank()) return "—"
    val datePart = joinedAt.substringBefore('T')
    val parts = datePart.split('-')
    if (parts.size < 2) return datePart
    val month = parts[1].toIntOrNull() ?: return datePart
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val name = months.getOrNull(month - 1) ?: return datePart
    return "$name ${parts[0]}"
}
