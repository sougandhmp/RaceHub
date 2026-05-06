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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding

private val DarkBg = Color(0xFF0A0A0A)
private val CardBg = Color(0xFF161616)
private val CardBorder = Color(0xFF262626)
private val MutedGray = Color(0xFF8E8E93)
private val RacingRed = Color(0xFFE10600)

private val Gold = Color(0xFFFFD700)
private val Silver = Color(0xFFC0C0C0)
private val Bronze = Color(0xFFCD7F32)

private val TeamMercedes = Color(0xFF00D2BE)
private val TeamMcLaren = Color(0xFFFF8700)
private val TeamRedBull = Color(0xFF0600EF)
private val TeamFerrari = Color(0xFFDC0000)
private val TeamAston = Color(0xFF006F62)
private val TeamAlpine = Color(0xFF0090FF)
private val TeamWilliams = Color(0xFF005AFF)
private val TeamRb = Color(0xFF1660AD)
private val TeamHaas = Color(0xFFB6BABD)
private val TeamSauber = Color(0xFF52E252)

private fun teamColorFor(team: String): Color = when {
    team.contains("Mercedes", ignoreCase = true) -> TeamMercedes
    team.contains("McLaren", ignoreCase = true) -> TeamMcLaren
    team.contains("Red Bull", ignoreCase = true) -> TeamRedBull
    team.contains("Ferrari", ignoreCase = true) -> TeamFerrari
    team.contains("Aston", ignoreCase = true) -> TeamAston
    team.contains("Alpine", ignoreCase = true) -> TeamAlpine
    team.contains("Williams", ignoreCase = true) -> TeamWilliams
    team.contains("RB", ignoreCase = true) -> TeamRb
    team.contains("Haas", ignoreCase = true) -> TeamHaas
    team.contains("Sauber", ignoreCase = true) -> TeamSauber
    else -> MutedGray
}

private fun positionAccent(position: Int): Color = when (position) {
    1 -> Gold
    2 -> Silver
    3 -> Bronze
    else -> MutedGray
}

private enum class StandingsCategory { Drivers, Teams }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsScreen(
    drivers: List<DriverStanding>,
    constructors: List<ConstructorStanding>,
    onBack: () -> Unit
) {
    var selected by rememberSaveable { mutableStateOf(StandingsCategory.Drivers) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "STANDINGS",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = Color.White
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBg
                )
            )
        },
        containerColor = DarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            item {
                StandingsToggle(
                    selected = selected,
                    onSelected = { selected = it }
                )
            }
            when (selected) {
                StandingsCategory.Drivers -> itemsIndexed(drivers) { index, standing ->
                    DriverStandingCard(
                        standing = standing,
                        accent = positionAccent(standing.position),
                        teamColor = teamColorFor(standing.team),
                        showLeader = index == 0
                    )
                }

                StandingsCategory.Teams -> items(constructors) { standing ->
                    ConstructorStandingCard(
                        standing = standing,
                        accent = positionAccent(standing.position),
                        teamColor = teamColorFor(standing.name)
                    )
                }
            }
        }
    }
}

@Composable
private fun StandingsToggle(
    selected: StandingsCategory,
    onSelected: (StandingsCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        StandingsCategory.entries.forEach { tab ->
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
                    text = if (tab == StandingsCategory.Drivers) "DRIVERS" else "CONSTRUCTORS",
                    color = if (isSelected) Color.White else MutedGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun DriverStandingCard(
    standing: DriverStanding,
    accent: Color,
    teamColor: Color,
    showLeader: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(
                width = if (showLeader) 1.5.dp else 1.dp,
                color = if (showLeader) accent.copy(alpha = 0.5f) else CardBorder,
                shape = RoundedCornerShape(16.dp)
            ),
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
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PositionBadge(position = standing.position, accent = accent)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = standing.driverName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(teamColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = standing.team,
                        color = MutedGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            PointsBlock(points = standing.points, wins = standing.wins)
        }
    }
}

@Composable
private fun ConstructorStandingCard(
    standing: ConstructorStanding,
    accent: Color,
    teamColor: Color
) {
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
                .width(6.dp)
                .background(teamColor)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PositionBadge(position = standing.position, accent = accent)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = standing.name,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CONSTRUCTOR",
                    color = teamColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            PointsBlock(points = standing.points, wins = standing.wins)
        }
    }
}

@Composable
private fun PositionBadge(position: Int, accent: Color) {
    val isPodium = position in 1..3
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isPodium) accent.copy(alpha = 0.15f) else Color(0xFF1F1F1F))
            .border(
                width = 1.dp,
                color = if (isPodium) accent else CardBorder,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = position.toString(),
            color = if (isPodium) accent else Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun PointsBlock(points: Int, wins: Int) {
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = points.toString(),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "PTS",
                color = MutedGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Text(
            text = if (wins == 1) "1 WIN" else "$wins WINS",
            color = if (wins > 0) RacingRed else MutedGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
