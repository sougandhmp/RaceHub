package org.gce.racehub.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
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
import org.gce.racehub.race.di.raceModule
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

private val DarkBg = Color(0xFF0A0A0A)
private val MutedGray = Color(0xFF8E8E93)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onViewAllSchedule: () -> Unit,
    onViewAllStandings: () -> Unit,
    onCreateThread: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { HomeHeader() },
        bottomBar = {
            HomeBottomNavigation(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.onIntent(HomeIntent.TabSelected(it)) }
            )
        },
        containerColor = DarkBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.selectedTab) {
                HomeTab.Race -> RaceScreen(
                    onViewAllSchedule = onViewAllSchedule,
                    onViewAllStandings = onViewAllStandings
                )

                HomeTab.Forum -> ForumScreen(onCreateThread = onCreateThread)

                HomeTab.Profile -> ProfileScreen()
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Race Hub",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )
        IconButton(onClick = { }) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun HomeBottomNavigation(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF121212),
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        HomeTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(if (isSelected) Color.White else MutedGray, CircleShape)
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
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = Color(0xFF2C2C2C),
                    unselectedIconColor = MutedGray,
                    unselectedTextColor = MutedGray
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
            raceModule,
            appModule
        )
    }) {
        HomeScreen(
            onViewAllSchedule = {},
            onViewAllStandings = {}
        )
    }
}
