package org.gce.racehub.race

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.TrendingThread
import org.gce.racehub.theme.AppColorScheme
import org.gce.racehub.theme.LocalAppColors
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceScreen(
    viewModel: RaceViewModel = koinViewModel(),
    onViewAllSchedule: () -> Unit,
    onViewAllStandings: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) isRefreshing = false
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.onIntent(RaceIntent.Refresh)
        },
        modifier = Modifier.fillMaxSize().background(colors.background)
    ) {
        if (state.isLoading && state.raceSchedule.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.racingRed
                )
            }
        } else {
            RaceTabContent(
                state = state,
                colors = colors,
                onViewAllSchedule = onViewAllSchedule,
                onViewAllStandings = onViewAllStandings
            )
        }
    }
}

@Composable
private fun RaceTabContent(
    state: RaceState,
    colors: AppColorScheme,
    onViewAllSchedule: () -> Unit,
    onViewAllStandings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            NextRaceSection(
                race = state.raceSchedule.firstOrNull { !it.isCompleted },
                colors = colors,
                onViewAll = onViewAllSchedule
            )
        }
        item {
            StandingsSection(
                drivers = state.driverStandings.take(3),
                constructors = state.constructorStandings.take(3),
                colors = colors,
                onViewAll = onViewAllStandings
            )
        }
        item {
            FeaturedSection(
                thread = state.trendingThreads.firstOrNull(),
                colors = colors
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun NextRaceSection(race: Race?, colors: AppColorScheme, onViewAll: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NEXT RACE",
                color = colors.mutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                race?.daysRemaining?.let { days ->
                    Text(
                        text = "RD ${race.round} · $days DAYS",
                        color = colors.racingRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(colors.racingRed.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.card)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = race?.name ?: "Canadian GP",
                    color = colors.primaryText,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = race?.countryFlag ?: "🇨🇦", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = race?.date ?: "Sun May 24 · 8:00 PM UTC · Montreal",
                color = colors.mutedText,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Circuit  ", color = colors.mutedText, fontSize = 13.sp)
                    Text(
                        text = race?.circuit ?: "—",
                        color = colors.primaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                TextButton(
                    onClick = onViewAll,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.heightIn(min = 20.dp)
                ) {
                    Text(
                        text = "See all",
                        color = colors.racingRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private enum class StandingsTab { Drivers, Constructors }

@Composable
private fun StandingsSection(
    drivers: List<DriverStanding>,
    constructors: List<ConstructorStanding>,
    colors: AppColorScheme,
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
                text = "Standings",
                color = colors.primaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = onViewAll,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.heightIn(min = 24.dp)
            ) {
                Text(
                    text = "See all",
                    color = colors.racingRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        StandingsTabPills(selected = selected, onSelected = { selected = it }, colors = colors)
        Spacer(modifier = Modifier.height(12.dp))
        when (selected) {
            StandingsTab.Drivers -> {
                val displayDrivers = drivers.ifEmpty {
                    listOf(
                        DriverStanding(1, "George Russell", "Mercedes", 142, 3),
                        DriverStanding(2, "Max Verstappen", "Red Bull", 134, 2),
                        DriverStanding(3, "Lando Norris", "McLaren", 121, 1)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    displayDrivers.forEach { DriverStandingCard(it, colors) }
                }
            }
            StandingsTab.Constructors -> {
                val displayConstructors = constructors.ifEmpty {
                    listOf(
                        ConstructorStanding(1, "Mercedes", 276, 4),
                        ConstructorStanding(2, "Red Bull", 207, 2),
                        ConstructorStanding(3, "McLaren", 170, 1)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    displayConstructors.forEach { ConstructorStandingCard(it, colors) }
                }
            }
        }
    }
}

@Composable
private fun StandingsTabPills(
    selected: StandingsTab,
    onSelected: (StandingsTab) -> Unit,
    colors: AppColorScheme
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        StandingsTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) colors.racingRed else Color.Transparent)
                    .clickable { onSelected(tab) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tab == StandingsTab.Drivers) "Drivers" else "Constructors",
                    color = if (isSelected) Color.White else colors.mutedText,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun DriverStandingCard(standing: DriverStanding, colors: AppColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (standing.position == 1) colors.racingRed.copy(alpha = 0.12f)
                    else colors.cardBorder.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = standing.position.toString(),
                color = if (standing.position == 1) colors.racingRed else colors.mutedText,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = standing.driverName,
                color = colors.primaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = standing.team,
                color = colors.mutedText,
                fontSize = 12.sp
            )
        }
        Text(
            text = standing.points.toString(),
            color = colors.primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ConstructorStandingCard(standing: ConstructorStanding, colors: AppColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (standing.position == 1) colors.racingRed.copy(alpha = 0.12f)
                    else colors.cardBorder.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = standing.position.toString(),
                color = if (standing.position == 1) colors.racingRed else colors.mutedText,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = standing.name,
                color = colors.primaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${standing.wins} wins",
                color = colors.mutedText,
                fontSize = 12.sp
            )
        }
        Text(
            text = standing.points.toString(),
            color = colors.primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun FeaturedSection(thread: TrendingThread?, colors: AppColorScheme) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FEATURED",
                color = colors.mutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Forum",
                color = colors.racingRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.card)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.racingRed.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "TRENDING",
                        color = colors.racingRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                thread?.createdAt?.let { date ->
                    Text(text = date, color = colors.mutedText, fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = thread?.title ?: "No threads yet — be the first to post.",
                color = colors.primaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "♥ ${thread?.likes ?: 0}",
                color = colors.mutedText,
                fontSize = 12.sp
            )
        }
    }
}
