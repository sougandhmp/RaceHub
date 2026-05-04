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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race

// ── Palette ─────────────────────────────────────────────────────────────────

private val RacingRed  = Color(0xFFE63946)
private val DarkBg     = Color(0xFF0A0A0A)
private val DarkBlue   = Color(0xFF1A1A2E)
private val CardBg     = Color(0xFF161625)
private val CardBorder = Color(0xFF2A2A3E)
private val MutedGray  = Color(0xFF8D99AE)
private val Gold       = Color(0xFFFFD700)
private val Silver     = Color(0xFFC0C0C0)
private val Bronze     = Color(0xFFCD7F32)
private val GreenDone  = Color(0xFF2ECC71)

// ── Entry point ──────────────────────────────────────────────────────────────

/**
 * Root composable for the Home screen.
 * Observes [HomeViewModel.state] and delegates rendering to tab-specific
 * sub-composables based on [HomeState.selectedTab].
 */
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DarkBg, DarkBlue, Color(0xFF16213E))
                )
            )
    ) {
        HomeHeader()

        TabSwitcher(
            selectedTab = state.selectedTab,
            onTabSelected = { viewModel.onIntent(HomeIntent.TabSelected(it)) }
        )

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RacingRed, strokeWidth = 3.dp)
            }
        } else {
            when (state.selectedTab) {
                HomeTab.Schedule  -> RaceScheduleTab(races = state.raceSchedule)
                HomeTab.Standings -> DriverStandingsTab(standings = state.driverStandings)
            }
        }
    }
}

// ── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🏎️", fontSize = 28.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "RaceHub",
                color = RacingRed,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
            Text(
                text = "2025 Season",
                color = MutedGray,
                fontSize = 11.sp
            )
        }
    }
}

// ── Tab switcher ─────────────────────────────────────────────────────────────

@Composable
private fun TabSwitcher(selectedTab: HomeTab, onTabSelected: (HomeTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HomeTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) RacingRed else CardBg)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) RacingRed else CardBorder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.name,
                    color = if (isSelected) Color.White else MutedGray,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ── Schedule tab ─────────────────────────────────────────────────────────────

/** Renders the full race calendar as a scrollable list of [RaceCard]s. */
@Composable
private fun RaceScheduleTab(races: List<Race>) {
    // The first non-completed race is highlighted as "NEXT RACE".
    val nextRaceId = races.firstOrNull { !it.isCompleted }?.id

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        items(races, key = { it.id }) { race ->
            RaceCard(race = race, isNextRace = race.id == nextRaceId)
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

/**
 * Card representing a single race event.
 *
 * @param race       The race data to display.
 * @param isNextRace When true a red "NEXT RACE" badge is shown and the card
 *                   gets a red border to draw the user's eye.
 */
@Composable
private fun RaceCard(race: Race, isNextRace: Boolean) {
    val borderColor = when {
        isNextRace      -> RacingRed
        race.isCompleted -> CardBorder
        else             -> CardBorder
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // Top row: flag + round + status badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = race.countryFlag, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ROUND ${race.round}",
                    color = MutedGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }
            // Status chip
            when {
                isNextRace -> StatusChip(label = "NEXT RACE", background = RacingRed)
                race.isCompleted -> StatusChip(label = "COMPLETED", background = GreenDone.copy(alpha = 0.15f), textColor = GreenDone)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Race name
        Text(
            text = race.name.uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Circuit and date
        Text(text = race.circuit, color = MutedGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = race.date, color = MutedGray.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

// ── Standings tab ─────────────────────────────────────────────────────────────

/** Renders the championship table as a scrollable list of [DriverStandingCard]s. */
@Composable
private fun DriverStandingsTab(standings: List<DriverStanding>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        items(standings, key = { it.position }) { standing ->
            DriverStandingCard(standing = standing)
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

/** Card representing a single driver's championship position. */
@Composable
private fun DriverStandingCard(standing: DriverStanding) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(width = 1.dp, color = CardBorder, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position badge
        PositionBadge(position = standing.position)

        Spacer(modifier = Modifier.width(14.dp))

        // Driver info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = standing.driverName.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = standing.team, color = MutedGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = standing.flag, fontSize = 12.sp)
            }
        }

        // Points
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${standing.points}",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
            Text(
                text = "PTS",
                color = MutedGray,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

// ── Reusable small components ─────────────────────────────────────────────────

/**
 * Circular badge showing a championship position.
 * Top-3 positions use gold, silver, and bronze fills respectively.
 */
@Composable
private fun PositionBadge(position: Int) {
    val bgColor = when (position) {
        1    -> Gold
        2    -> Silver
        3    -> Bronze
        else -> CardBorder
    }
    val textColor = when (position) {
        1, 2, 3 -> Color(0xFF0A0A0A)
        else    -> MutedGray
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$position",
            color = textColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp
        )
    }
}

/**
 * Small pill-shaped status indicator shown on race cards.
 *
 * @param label      Text inside the chip.
 * @param background Fill color of the chip.
 * @param textColor  Text color; defaults to white.
 */
@Composable
private fun StatusChip(
    label: String,
    background: Color,
    textColor: Color = Color.White
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}
