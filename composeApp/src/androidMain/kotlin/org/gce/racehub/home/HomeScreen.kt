package org.gce.racehub.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gce.racehub.auth.di.createProductionAuthModule
import org.gce.racehub.di.appModule
import org.gce.racehub.race.di.raceModule
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.TrendingThread
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

// ── Palette ─────────────────────────────────────────────────────────────────

private val RacingRed = Color(0xFFE63946)
private val DarkBg = Color(0xFF0A0A0A)
private val CardBg = Color(0xFF161616)
private val CardBorder = Color(0xFF262626)
private val MutedGray = Color(0xFF8E8E93)
private val TeamGreen = Color(0xFF00D2BE)
private val TeamOrange = Color(0xFFFF8700)
private val TeamBlue = Color(0xFF0600EF)

// ── Entry point ──────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onViewAllSchedule: () -> Unit,
    onViewAllStandings: () -> Unit
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
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = RacingRed
                )
            } else {
                when (state.selectedTab) {
                    HomeTab.Race -> RaceTabContent(
                        state = state,
                        onViewAllSchedule = onViewAllSchedule,
                        onViewAllStandings = onViewAllStandings
                    )

                    HomeTab.Forum -> PlaceholderScreen("Forum")
                    HomeTab.Profile -> PlaceholderScreen("Profile")
                }
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
private fun RaceTabContent(
    state: HomeState,
    onViewAllSchedule: () -> Unit,
    onViewAllStandings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            NextRaceSection(
                race = state.raceSchedule.firstOrNull { !it.isCompleted },
                onViewAll = onViewAllSchedule
            )
        }
        item {
            StandingsTabSection(
                drivers = state.driverStandings.take(5),
                constructors = state.constructorStandings.take(5),
                onViewAll = onViewAllStandings
            )
        }
        item {
            LatestResultsSection(
                results = state.raceSchedule.filter { it.isCompleted }.takeLast(3).reversed()
            )
        }
        item {
            LatestThreadSection(thread = state.trendingThreads.firstOrNull())
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun NextRaceSection(race: Race?, onViewAll: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NEXT RACE",
                color = MutedGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                race?.daysRemaining?.let { days ->
                    Text(
                        text = "R${race.round} · $days DAYS",
                        color = RacingRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
                TextButton(
                    onClick = onViewAll,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.heightIn(min = 24.dp)
                ) {
                    Text(
                        text = "VIEW ALL",
                        color = RacingRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBg)
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = race?.name ?: "Canadian GP",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = race?.countryFlag ?: "🇨🇦", fontSize = 24.sp)
            }
            Text(
                text = race?.date ?: "Sun May 24 · 8:00 PM UTC · Montreal",
                color = MutedGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row {
                Text(text = "Forecast ", color = MutedGray, fontSize = 14.sp)
                Text(text = "22° · 30% rain ", color = Color.White, fontSize = 14.sp)
                Text(text = "Length ", color = MutedGray, fontSize = 14.sp)
                Text(text = "4.361 km", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

private enum class StandingsTab { Drivers, Teams }

@Composable
private fun StandingsTabSection(
    drivers: List<DriverStanding>,
    constructors: List<ConstructorStanding>,
    onViewAll: () -> Unit
) {
    var selected by rememberSaveable { mutableStateOf(StandingsTab.Drivers) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selected == StandingsTab.Drivers) "Driver Standings" else "Constructor Standings",
                color = MutedGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(
                onClick = onViewAll,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.heightIn(min = 24.dp)
            ) {
                Text(
                    text = "VIEW ALL",
                    color = RacingRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        StandingsSegmentedControl(
            selected = selected,
            onSelected = { selected = it }
        )
        Spacer(modifier = Modifier.height(12.dp))
        when (selected) {
            StandingsTab.Drivers -> DriverStandingsTable(
                drivers = drivers.ifEmpty {
                    listOf(
                        DriverStanding(1, "Kimi Antonelli", "Mercedes", 72, 2),
                        DriverStanding(2, "George Russell", "Mercedes", 63, 1),
                        DriverStanding(3, "Charles Leclerc", "Ferrari", 49, 0)
                    )
                }
            )

            StandingsTab.Teams -> ConstructorStandingsTable(
                constructors = constructors.ifEmpty {
                    listOf(
                        ConstructorStanding(1, "Mercedes", 135, 3),
                        ConstructorStanding(2, "Ferrari", 90, 0),
                        ConstructorStanding(3, "McLaren", 46, 0)
                    )
                }
            )
        }
    }
}

@Composable
private fun StandingsSegmentedControl(
    selected: StandingsTab,
    onSelected: (StandingsTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        StandingsTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) RacingRed else Color.Transparent)
                    .clickable { onSelected(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tab == StandingsTab.Drivers) "Driver Standings" else "Constructor Standings",
                    color = if (isSelected) Color.White else MutedGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DriverStandingsTable(drivers: List<DriverStanding>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
    ) {
        StandingsTableHeader(columns = listOf("POS", "NAME", "TEAM", "POINTS", "WINS"))
        drivers.forEachIndexed { index, standing ->
            StandingsTableRow(
                cells = listOf(
                    standing.position.toString(),
                    standing.driverName,
                    standing.team,
                    standing.points.toString(),
                    standing.wins.toString()
                ),
                isAlternate = index % 2 == 1
            )
        }
    }
}

@Composable
private fun ConstructorStandingsTable(constructors: List<ConstructorStanding>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
    ) {
        StandingsTableHeader(columns = listOf("POS", "TEAM", "POINTS", "WINS"))
        constructors.forEachIndexed { index, standing ->
            StandingsTableRow(
                cells = listOf(
                    standing.position.toString(),
                    standing.name,
                    standing.points.toString(),
                    standing.wins.toString()
                ),
                isAlternate = index % 2 == 1
            )
        }
    }
}

private val HeaderBg = Color(0xFF2A1116)
private val RowAltBg = Color(0xFF1B0E11)

@Composable
private fun StandingsTableHeader(columns: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        columns.forEachIndexed { index, label ->
            Text(
                text = label,
                color = MutedGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(columnWeight(columns.size, index))
            )
        }
    }
}

@Composable
private fun StandingsTableRow(cells: List<String>, isAlternate: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isAlternate) RowAltBg else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        cells.forEachIndexed { index, value ->
            val isPos = index == 0
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = if (isPos) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.weight(columnWeight(cells.size, index))
            )
        }
    }
}

private fun columnWeight(totalColumns: Int, index: Int): Float {
    // POS narrow, NAME/TEAM wider, numeric columns medium.
    return when (totalColumns) {
        5 -> when (index) {
            0 -> 0.6f; 1 -> 1.6f; 2 -> 1.4f; 3 -> 0.9f; else -> 0.7f
        }

        4 -> when (index) {
            0 -> 0.6f; 1 -> 1.8f; 2 -> 0.9f; else -> 0.7f
        }

        else -> 1f
    }
}

@Composable
internal fun StandingItem(standing: DriverStanding, teamColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(teamColor)
        )
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262626)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = standing.position.toString(),
                    color = MutedGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = standing.team,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = standing.driverName,
                    color = MutedGray,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = standing.points.toString(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "PTS",
                    color = MutedGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
private fun LatestResultsSection(results: List<Race>) {
    Column {
        Text(
            text = "Latest Results",
            color = MutedGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        if (results.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "No completed races yet this season.",
                    color = MutedGray,
                    fontSize = 14.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                results.forEach { race -> LatestResultItem(race) }
            }
        }
    }
}

@Composable
private fun LatestResultItem(race: Race) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ROUND ${race.round}",
                color = MutedGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "FINAL",
                color = RacingRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(RacingRed.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = race.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = race.countryFlag, fontSize = 18.sp)
        }
        Text(
            text = race.date,
            color = MutedGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun LatestThreadSection(thread: TrendingThread?) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Latest Thread",
                color = MutedGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "FORUM",
                color = RacingRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBg)
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(RacingRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RH",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Race Hub",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "PINNED",
                    color = RacingRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(RacingRed.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = thread?.title ?: "No threads yet — be the first to post.",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = thread?.let { "❤️ ${it.likes}" } ?: "❤️ 0",
                color = MutedGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, color = Color.White, fontSize = 24.sp)
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
