package org.gce.racehub.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gce.racehub.auth.di.createProductionAuthModule
import org.gce.racehub.di.appModule
import org.gce.racehub.forum.ForumScreen
import org.gce.racehub.profile.ProfileScreen
import org.gce.racehub.race.RaceScreen
import org.gce.racehub.race.di.createRaceModule
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.theme.LocalAppColors
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import racehub.composeapp.generated.resources.Res
import racehub.composeapp.generated.resources.contentdesc_menu
import racehub.composeapp.generated.resources.home_title
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

private val HomeTab.icon: ImageVector
    get() = when (this) {
        HomeTab.Race -> Icons.Filled.SportsScore
        HomeTab.Forum -> Icons.AutoMirrored.Filled.Chat
        HomeTab.Profile -> Icons.Filled.Person
    }

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onViewAllSchedule: () -> Unit,
    onViewAllStandings: () -> Unit,
    onCreateThread: () -> Unit = {},
    onThreadClick: (Thread) -> Unit = {},
    onSignedOut: () -> Unit = {},
    onViewRaceDetail: (Race) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    Scaffold(
        topBar = { HomeHeader() },
        bottomBar = {
            HomeBottomNavigation(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.onIntent(HomeIntent.TabSelected(it)) }
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.selectedTab) {
                HomeTab.Race -> RaceScreen(
                    onViewAllSchedule = onViewAllSchedule,
                    onViewAllStandings = onViewAllStandings,
                    onViewRaceDetail = onViewRaceDetail
                )

                HomeTab.Forum -> ForumScreen(
                    onCreateThread = onCreateThread,
                    onThreadClick = onThreadClick
                )

                HomeTab.Profile -> ProfileScreen(onSignedOut = onSignedOut)
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.home_title),
            color = colors.primaryText,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(Res.string.contentdesc_menu),
                tint = colors.primaryText,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun HomeBottomNavigation(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    val colors = LocalAppColors.current
    NavigationBar(
        containerColor = colors.navBar,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        HomeTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.name,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.racingRed,
                    selectedTextColor = colors.racingRed,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = colors.mutedText,
                    unselectedTextColor = colors.mutedText
                )
            )
        }
    }
}

@Composable
@Preview
fun HomeScreenPreview() {
    KoinApplication(configuration = koinConfiguration {
        modules(
            createProductionAuthModule("https://api.example.com"),
            createRaceModule("https://api.example.com"),
            appModule
        )
    }) {
        HomeScreen(
            onViewAllSchedule = {},
            onViewAllStandings = {}
        )
    }
}
