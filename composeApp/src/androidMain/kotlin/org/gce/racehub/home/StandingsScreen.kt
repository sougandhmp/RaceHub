package org.gce.racehub.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
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
import org.gce.racehub.theme.AppColorScheme
import org.gce.racehub.theme.DarkAppColors
import org.gce.racehub.theme.LocalAppColors
import org.gce.racehub.theme.teamColorOf
import org.jetbrains.compose.resources.stringResource
import racehub.composeapp.generated.resources.Res
import racehub.composeapp.generated.resources.contentdesc_back
import racehub.composeapp.generated.resources.label_constructor
import racehub.composeapp.generated.resources.label_one_win
import racehub.composeapp.generated.resources.label_pts
import racehub.composeapp.generated.resources.label_wins_caps
import racehub.composeapp.generated.resources.tab_constructors_caps
import racehub.composeapp.generated.resources.tab_drivers_caps
import racehub.composeapp.generated.resources.title_standings

private val Gold   = Color(0xFFFFD700)
private val Silver = Color(0xFFC0C0C0)
private val Bronze = Color(0xFFCD7F32)

private fun positionAccent(position: Int, fallback: Color): Color = when (position) {
    1 -> Gold
    2 -> Silver
    3 -> Bronze
    else -> fallback
}

private enum class StandingsCategory { Drivers, Teams }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsScreen(
    drivers: List<DriverStanding>,
    constructors: List<ConstructorStanding>,
    onBack: () -> Unit
) {
    val colors = LocalAppColors.current
    var selected by rememberSaveable { mutableStateOf(StandingsCategory.Drivers) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.title_standings),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = colors.primaryText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.contentdesc_back),
                            tint = colors.primaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background
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
                    onSelected = { selected = it },
                    colors = colors
                )
            }
            when (selected) {
                StandingsCategory.Drivers -> itemsIndexed(drivers) { index, standing ->
                    DriverStandingCard(
                        standing = standing,
                        accent = positionAccent(standing.position, colors.mutedText),
                        teamColor = teamColorOf(standing.team),
                        showLeader = index == 0,
                        colors = colors
                    )
                }

                StandingsCategory.Teams -> items(constructors) { standing ->
                    ConstructorStandingCard(
                        standing = standing,
                        accent = positionAccent(standing.position, colors.mutedText),
                        teamColor = teamColorOf(standing.name),
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun StandingsToggle(
    selected: StandingsCategory,
    onSelected: (StandingsCategory) -> Unit,
    colors: AppColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        StandingsCategory.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) colors.racingRed else Color.Transparent)
                    .clickable { onSelected(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tab == StandingsCategory.Drivers) stringResource(Res.string.tab_drivers_caps) else stringResource(Res.string.tab_constructors_caps),
                    color = if (isSelected) Color.White else colors.mutedText,
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
    showLeader: Boolean,
    colors: AppColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(
                width = if (showLeader) 1.5.dp else 1.dp,
                color = if (showLeader) accent.copy(alpha = 0.5f) else colors.cardBorder,
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
            PositionBadge(position = standing.position, accent = accent, colors = colors)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = standing.driverName,
                    color = colors.primaryText,
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
                        color = colors.mutedText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            PointsBlock(points = standing.points, wins = standing.wins, colors = colors)
        }
    }
}

@Composable
private fun ConstructorStandingCard(
    standing: ConstructorStanding,
    accent: Color,
    teamColor: Color,
    colors: AppColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
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
            PositionBadge(position = standing.position, accent = accent, colors = colors)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = standing.name,
                    color = colors.primaryText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.label_constructor),
                    color = teamColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            PointsBlock(points = standing.points, wins = standing.wins, colors = colors)
        }
    }
}

@Composable
private fun PositionBadge(position: Int, accent: Color, colors: AppColorScheme) {
    val isPodium = position in 1..3
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isPodium) accent.copy(alpha = 0.15f) else colors.surface)
            .border(
                width = 1.dp,
                color = if (isPodium) accent else colors.cardBorder,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = position.toString(),
            color = if (isPodium) accent else colors.primaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun PointsBlock(points: Int, wins: Int, colors: AppColorScheme) {
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = points.toString(),
                color = colors.primaryText,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.label_pts),
                color = colors.mutedText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Text(
            text = if (wins == 1) stringResource(Res.string.label_one_win) else stringResource(Res.string.label_wins_caps, wins),
            color = if (wins > 0) colors.racingRed else colors.mutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StandingsScreenPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkAppColors) {
        StandingsScreen(
            drivers = listOf(
                DriverStanding(1, "Max Verstappen", "Red Bull", 429, 15),
                DriverStanding(2, "Lando Norris", "McLaren", 349, 3),
                DriverStanding(3, "Charles Leclerc", "Ferrari", 341, 5),
                DriverStanding(4, "George Russell", "Mercedes", 287, 2),
            ),
            constructors = listOf(
                ConstructorStanding(1, "Red Bull", 860, 21),
                ConstructorStanding(2, "Ferrari", 652, 5),
                ConstructorStanding(3, "McLaren", 556, 3),
            ),
            onBack = {}
        )
    }
}
