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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
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
import org.gce.racehub.theme.DarkAppColors
import org.gce.racehub.theme.LocalAppColors
import org.gce.racehub.theme.ThemeManager
import org.gce.racehub.theme.ThemeMode
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import racehub.composeapp.generated.resources.Res
import racehub.composeapp.generated.resources.action_sign_out
import racehub.composeapp.generated.resources.heading_recent_posts
import racehub.composeapp.generated.resources.label_appearance
import racehub.composeapp.generated.resources.label_country_caps
import racehub.composeapp.generated.resources.label_email
import racehub.composeapp.generated.resources.label_joined
import racehub.composeapp.generated.resources.label_posts
import racehub.composeapp.generated.resources.label_saved
import racehub.composeapp.generated.resources.label_username
import racehub.composeapp.generated.resources.profile_not_signed_in
import racehub.composeapp.generated.resources.theme_dark
import racehub.composeapp.generated.resources.theme_light
import racehub.composeapp.generated.resources.theme_system

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    themeManager: ThemeManager = koinInject(),
    onSignedOut: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val themeMode by themeManager.themeMode.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.SignedOut -> onSignedOut()
            }
        }
    }

    ProfileContent(
        state = state,
        themeMode = themeMode,
        colors = colors,
        onThemeSelect = { themeManager.setThemeMode(it) },
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    state: ProfileState,
    themeMode: ThemeMode,
    colors: AppColorScheme,
    onThemeSelect: (ThemeMode) -> Unit,
    onIntent: (ProfileIntent) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = state.isLoadingProfile,
        onRefresh = { onIntent(ProfileIntent.RefreshProfile) },
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
                    text = stringResource(Res.string.profile_not_signed_in),
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
                        themeMode = themeMode,
                        onSelect = onThemeSelect,
                        colors = colors
                    )
                    state.profile?.let { profile ->
                        if (profile.recentThreadTitles.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            ThreadTitleSection(
                                heading = stringResource(Res.string.heading_recent_posts),
                                titles = profile.recentThreadTitles,
                                colors = colors
                            )
                        }
                        if (profile.savedThreadTitles.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            ThreadTitleSection(
                                heading = stringResource(Res.string.label_saved),
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
                        onClick = { onIntent(ProfileIntent.SignOut) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    val user = User(
        id = "1",
        email = "max@redbull.com",
        name = "Max Verstappen",
        username = "maxverstappen",
        country = "NL",
        role = "member",
        joinedAt = "2023-03-01T00:00:00Z",
        postsCount = 42
    )
    val profile = UserProfile(
        username = "maxverstappen",
        email = "max@redbull.com",
        avatar = "MV",
        postsCount = 42,
        savedCount = 7,
        recentThreadTitles = listOf("Monaco race was a masterclass", "Red Bull PU update thoughts"),
        savedThreadTitles = listOf("Best F1 moments of the decade")
    )
    CompositionLocalProvider(LocalAppColors provides DarkAppColors) {
        ProfileContent(
            state = ProfileState(user = user, profile = profile),
            themeMode = ThemeMode.DARK,
            colors = DarkAppColors,
            onThemeSelect = {},
            onIntent = {}
        )
    }
}

@Composable
private fun ThemeToggle(
    themeMode: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    colors: AppColorScheme
) {
    val options = listOf(
        ThemeMode.SYSTEM to stringResource(Res.string.theme_system),
        ThemeMode.DARK   to stringResource(Res.string.theme_dark),
        ThemeMode.LIGHT  to stringResource(Res.string.theme_light)
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.label_appearance),
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
            options.forEach { (mode, label) ->
                ThemePill(
                    label = label,
                    isSelected = themeMode == mode,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelect(mode) }
                )
            }
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
    val initials = profile?.avatar?.takeIf { it.isNotBlank() }?.take(2)?.uppercase()
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
        StatCell(label = stringResource(Res.string.label_posts), value = (profile?.postsCount ?: user.postsCount).toString(), colors = colors)
        StatCell(label = stringResource(Res.string.label_saved), value = profile?.savedCount?.toString() ?: "—", colors = colors)
        StatCell(label = stringResource(Res.string.label_country_caps), value = user.country?.uppercase() ?: "—", colors = colors)
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
        DetailRow(label = stringResource(Res.string.label_email), value = user.email, colors = colors)
        user.username?.takeIf { it.isNotBlank() }?.let {
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = stringResource(Res.string.label_username), value = it, colors = colors)
        }
        user.joinedAt?.takeIf { it.isNotBlank() }?.let {
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = stringResource(Res.string.label_joined), value = formatJoined(it), colors = colors)
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
            Text(text = stringResource(Res.string.action_sign_out), fontWeight = FontWeight.Bold)
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
