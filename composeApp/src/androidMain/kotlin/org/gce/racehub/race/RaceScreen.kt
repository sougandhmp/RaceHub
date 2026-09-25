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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.RaceSession
import org.gce.racehub.race.domain.model.TrendingThread
import org.gce.racehub.theme.AppColorScheme
import org.gce.racehub.theme.DarkAppColors
import org.gce.racehub.theme.Dimens
import org.gce.racehub.theme.LocalAppColors
import org.gce.racehub.theme.teamColorOf
import org.gce.racehub.theme.ErrorBanner
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import racehub.composeapp.generated.resources.Res
import racehub.composeapp.generated.resources.action_see_all
import racehub.composeapp.generated.resources.label_featured_discussion
import racehub.composeapp.generated.resources.label_forum_arrow
import racehub.composeapp.generated.resources.label_no_trending
import racehub.composeapp.generated.resources.label_pts
import racehub.composeapp.generated.resources.label_standings
import racehub.composeapp.generated.resources.label_trending
import racehub.composeapp.generated.resources.label_likes
import racehub.composeapp.generated.resources.label_wins
import racehub.composeapp.generated.resources.race_full_calendar
import racehub.composeapp.generated.resources.race_no_upcoming
import racehub.composeapp.generated.resources.race_weekend_detail
import racehub.composeapp.generated.resources.standings_loading
import racehub.composeapp.generated.resources.tab_constructors
import racehub.composeapp.generated.resources.tab_drivers
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceScreen(
    viewModel: RaceViewModel = koinViewModel(),
    onViewAllSchedule: () -> Unit,
    onViewAllStandings: () -> Unit,
    onViewRaceDetail: (Race) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.onIntent(RaceIntent.Refresh) },
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
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
                onViewAllStandings = onViewAllStandings,
                onViewRaceDetail = onViewRaceDetail
            )
        }
        state.errorMessage?.let { message ->
            ErrorBanner(
                message = message,
                onRetry = { viewModel.onIntent(RaceIntent.Refresh) },
                onDismiss = { viewModel.onIntent(RaceIntent.DismissError) },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun RaceTabContent(
    state: RaceState,
    colors: AppColorScheme,
    onViewAllSchedule: () -> Unit,
    onViewAllStandings: () -> Unit,
    onViewRaceDetail: (Race) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item {
            NextRaceSection(
                race = state.raceSchedule.firstOrNull { !it.isCompleted },
                nextRaceDetail = state.nextRaceDetail,
                totalRaces = state.raceSchedule.size,
                colors = colors,
                onViewAllSchedule = onViewAllSchedule,
                onViewRaceDetail = onViewRaceDetail
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
    }
}

// ── Up Next Card ──────────────────────────────────────────────────────────────

@Composable
private fun NextRaceSection(
    race: Race?,
    nextRaceDetail: RaceDetail?,
    totalRaces: Int,
    colors: AppColorScheme,
    onViewAllSchedule: () -> Unit,
    onViewRaceDetail: (Race) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Dimens.cardShape)
            .background(colors.card)
            .border(1.dp, colors.cardBorder, Dimens.cardShape)
            .padding(20.dp)
    ) {
        // R9/24 · SUN MAY 24  +  🇨🇦
        val roundLabel = race?.let { "R${it.round}/$totalRaces" } ?: ""
        val dateLabel = race?.let { formatRaceHeaderDate(it.dateTime) } ?: ""
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = listOf(roundLabel, dateLabel).filter { it.isNotEmpty() }.joinToString("  ·  "),
                color = colors.mutedText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            if (race != null) {
                Text(text = countryFlag(race.country), fontSize = 22.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Race name
        Text(
            text = race?.let { shortRaceName(it.name) } ?: stringResource(Res.string.race_no_upcoming),
            color = colors.primaryText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 30.sp
        )

        // Circuit name
        if (race != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = race.circuit,
                color = colors.mutedText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Circuit image — centered
        val resId = race?.let { circuitDrawable(it.circuit) ?: circuitDrawable(it.name) }
        if (resId != null) {
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.foundation.Image(
                painter = painterResource(resId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .alpha(0.22f),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Session strip: FP1 | FP2 | FP3 | QUAL | RACE
        val sessions = if (nextRaceDetail?.sessions?.isNotEmpty() == true)
            sessionsFromDetail(nextRaceDetail.sessions)
        else
            sessionsFromRace(race)
        SessionStrip(sessions = sessions, colors = colors)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                    .clickable(enabled = race != null) { race?.let(onViewRaceDetail) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.race_weekend_detail),
                    color = if (race != null) colors.primaryText else colors.mutedText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.racingRed)
                    .clickable(onClick = onViewAllSchedule),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.race_full_calendar),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

private data class SessionStripChip(val label: String, val date: String, val time: String)

@Composable
private fun SessionStrip(sessions: List<RaceSessionChip>, colors: AppColorScheme) {
    val chips = sessions.map { chip ->
        SessionStripChip(
            label = stripLabel(chip.label),
            date = chip.fullDate,
            time = chip.time
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        chips.forEach { chip ->
            val isRace = chip.label == "RACE"
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isRace) colors.racingRed
                        else colors.cardBorder.copy(alpha = 0.4f)
                    )
                    .padding(vertical = 10.dp, horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = chip.label,
                    color = if (isRace) Color.White else colors.mutedText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = chip.date,
                    color = if (isRace) Color.White else colors.primaryText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = chip.time,
                    color = if (isRace) Color.White.copy(alpha = 0.85f) else colors.mutedText,
                    fontSize = 9.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

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
                text = stringResource(Res.string.label_standings),
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
                    text = stringResource(Res.string.action_see_all),
                    color = colors.racingRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        StandingsTabPills(selected = selected, onSelected = { selected = it }, colors = colors)
        Spacer(modifier = Modifier.height(12.dp))
        when (selected) {
            StandingsTab.Drivers ->
                if (drivers.isEmpty()) {
                    StandingsLoadingPlaceholder(colors)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        drivers.forEach { DriverStandingCard(it, colors) }
                    }
                }

            StandingsTab.Constructors ->
                if (constructors.isEmpty()) {
                    StandingsLoadingPlaceholder(colors)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        constructors.forEach { ConstructorStandingCard(it, colors) }
                    }
                }
        }
    }
}

@Composable
private fun StandingsLoadingPlaceholder(colors: AppColorScheme) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(Res.string.standings_loading), color = colors.mutedText, fontSize = 13.sp)
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
                    text = if (tab == StandingsTab.Drivers) stringResource(Res.string.tab_drivers) else stringResource(Res.string.tab_constructors),
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
    val tColor = teamColorOf(standing.team)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Dimens.cardShape)
            .background(colors.card)
            .border(1.dp, colors.cardBorder, Dimens.cardShape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(56.dp)
                .background(tColor)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = standing.position.toString(),
                color = colors.mutedText,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.width(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
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
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = standing.points.toString(),
                    color = colors.primaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = stringResource(Res.string.label_pts),
                    color = colors.mutedText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ConstructorStandingCard(standing: ConstructorStanding, colors: AppColorScheme) {
    val tColor = teamColorOf(standing.name)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Dimens.cardShape)
            .background(colors.card)
            .border(1.dp, colors.cardBorder, Dimens.cardShape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(56.dp)
                .background(tColor)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = standing.position.toString(),
                color = colors.mutedText,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.width(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = standing.name,
                    color = colors.primaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.label_wins, standing.wins),
                    color = colors.mutedText,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = standing.points.toString(),
                    color = colors.primaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = stringResource(Res.string.label_pts),
                    color = colors.mutedText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
                text = stringResource(Res.string.label_featured_discussion),
                color = colors.mutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                text = stringResource(Res.string.label_forum_arrow),
                color = colors.racingRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (thread == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.card)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(Res.string.label_no_trending), color = colors.mutedText, fontSize = 14.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.card)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = colors.racingRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.label_trending),
                            color = colors.racingRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(text = thread.createdAt, color = colors.mutedText, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.racingRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💬", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = thread.title,
                            color = colors.primaryText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "❤️", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(Res.string.label_likes, thread.likes),
                                color = colors.mutedText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

internal data class RaceSessionChip(
    val label: String,
    val fullDate: String,
    val time: String,
    val timezone: String
)

internal fun shortRaceName(name: String): String =
    name.replace("Grand Prix", "GP", ignoreCase = true).trim()

internal fun countryFlag(country: String): String = when {
    country.contains("bahrain", true)                                          -> "🇧🇭"
    country.contains("saudi", true)                                            -> "🇸🇦"
    country.contains("australia", true)                                        -> "🇦🇺"
    country.contains("japan", true)                                            -> "🇯🇵"
    country.contains("china", true)                                            -> "🇨🇳"
    country.contains("monaco", true)                                           -> "🇲🇨"
    country.contains("canada", true)                                           -> "🇨🇦"
    country.contains("spain", true)                                            -> "🇪🇸"
    country.contains("austria", true)                                          -> "🇦🇹"
    country.contains("britain", true) || country.contains("kingdom", true)    -> "🇬🇧"
    country.contains("hungary", true)                                          -> "🇭🇺"
    country.contains("belgium", true)                                          -> "🇧🇪"
    country.contains("netherlands", true)                                      -> "🇳🇱"
    country.contains("singapore", true)                                        -> "🇸🇬"
    country.contains("azerbaijan", true)                                       -> "🇦🇿"
    country.contains("qatar", true)                                            -> "🇶🇦"
    country.contains("mexico", true)                                           -> "🇲🇽"
    country.contains("brazil", true)                                           -> "🇧🇷"
    country.contains("abu dhabi", true) || country.contains("emirates", true) -> "🇦🇪"
    country.contains("united states", true) || country.contains("usa", true)  -> "🇺🇸"
    country.contains("italy", true)                                            -> "🇮🇹"
    else                                                                       -> "🏁"
}

private fun formatRaceHeaderDate(dateTime: String): String {
    val date = parseIsoToDate(dateTime) ?: return ""
    return java.text.SimpleDateFormat("EEE MMM d", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        .format(date).uppercase()
}

internal fun stripLabel(full: String): String = when (full.uppercase().trim()) {
    "PRACTICE 1" -> "FP1"
    "PRACTICE 2" -> "FP2"
    "PRACTICE 3" -> "FP3"
    "QUALIFYING" -> "QUAL"
    "RACE" -> "RACE"
    "SPRINT" -> "SPR"
    "SPRINT QUAL" -> "SQ"
    else -> full.take(4)
}


internal fun deviceTimezoneLabel(): String {
    val tz = java.util.TimeZone.getDefault()
    val now = java.util.Date()
    val offset = tz.getOffset(now.time)
    val sign = if (offset >= 0) "+" else "-"
    val absOffset = if (offset < 0) -offset else offset
    val hours = absOffset / 3600000
    val minutes = (absOffset % 3600000) / 60000
    return if (minutes == 0) "GMT$sign$hours"
    else "GMT$sign$hours:${String.format(java.util.Locale.US, "%02d", minutes)}"
}

internal fun formatSessionDate(cal: Calendar): String =
    java.text.SimpleDateFormat("MMM d", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getDefault() }
        .format(cal.time)

internal fun formatSessionTime(cal: Calendar): String =
    java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getDefault() }
        .format(cal.time)

internal fun displayLabel(raw: String): String = when (raw.uppercase().trim()) {
    "FP1", "PRACTICE 1", "P1", "PRACTICE1" -> "PRACTICE 1"
    "FP2", "PRACTICE 2", "P2", "PRACTICE2" -> "PRACTICE 2"
    "FP3", "PRACTICE 3", "P3", "PRACTICE3" -> "PRACTICE 3"
    "QUAL", "QUALIFYING", "Q" -> "QUALIFYING"
    "RACE", "GRAND PRIX" -> "RACE"
    "SPRINT QUALIFYING", "SPRINT QUAL", "SQ" -> "SPRINT QUAL"
    "SPRINT" -> "SPRINT"
    else -> raw.uppercase()
}


internal fun sessionsFromRace(race: Race?): List<RaceSessionChip> {
    if (race == null) return emptyList()
    val raceDate = parseIsoToDate(race.dateTime) ?: return fallbackSessions()
    val tzLabel = deviceTimezoneLabel()

    fun calAt(days: Int, hours: Int = 0): Calendar =
        Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            time = raceDate
            add(Calendar.DATE, days)
            if (hours != 0) add(Calendar.HOUR_OF_DAY, hours)
        }

    return listOf(
        RaceSessionChip(
            "PRACTICE 1",
            formatSessionDate(calAt(-2, -3)),
            formatSessionTime(calAt(-2, -3)),
            tzLabel
        ),
        RaceSessionChip(
            "PRACTICE 2",
            formatSessionDate(calAt(-2)),
            formatSessionTime(calAt(-2)),
            tzLabel
        ),
        RaceSessionChip(
            "PRACTICE 3",
            formatSessionDate(calAt(-1, -3)),
            formatSessionTime(calAt(-1, -3)),
            tzLabel
        ),
        RaceSessionChip(
            "QUALIFYING",
            formatSessionDate(calAt(-1)),
            formatSessionTime(calAt(-1)),
            tzLabel
        ),
        RaceSessionChip("RACE", formatSessionDate(calAt(0)), formatSessionTime(calAt(0)), tzLabel),
    )
}

internal fun fallbackSessions(): List<RaceSessionChip> {
    val tz = deviceTimezoneLabel()
    return listOf(
        RaceSessionChip("PRACTICE 1", "—", "—", tz),
        RaceSessionChip("PRACTICE 2", "—", "—", tz),
        RaceSessionChip("PRACTICE 3", "—", "—", tz),
        RaceSessionChip("QUALIFYING", "—", "—", tz),
        RaceSessionChip("RACE", "—", "—", tz),
    )
}

internal fun sessionsFromDetail(sessions: List<RaceSession>): List<RaceSessionChip> {
    val tzLabel = deviceTimezoneLabel()
    return sessions.map { session ->
        val date = parseIsoToDate(session.dateTime)
        val (fullDate, time) = if (date != null) {
            val cal = Calendar.getInstance(java.util.TimeZone.getDefault())
                .also { it.time = date }
            Pair(formatSessionDate(cal), formatSessionTime(cal))
        } else Pair("—", "—")
        RaceSessionChip(
            label = displayLabel(session.label),
            fullDate = fullDate,
            time = time,
            timezone = tzLabel
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RaceScreenPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkAppColors) {
        RaceTabContent(
            state = RaceState(
                raceSchedule = listOf(
                    Race("1", "Monaco Grand Prix", "Circuit de Monaco", "Monaco", "Monte Carlo", "2025-06-01T13:00:00Z", 9, "UPCOMING", "Sunny")
                ),
                driverStandings = listOf(
                    DriverStanding(1, "Max Verstappen", "Red Bull", 429, 15),
                    DriverStanding(2, "Lando Norris", "McLaren", 349, 3),
                    DriverStanding(3, "Charles Leclerc", "Ferrari", 341, 5),
                ),
                constructorStandings = listOf(
                    ConstructorStanding(1, "Red Bull", 860, 21),
                    ConstructorStanding(2, "McLaren", 556, 3),
                    ConstructorStanding(3, "Ferrari", 652, 5),
                ),
                trendingThreads = listOf(
                    TrendingThread("1", "Was the Monaco race boring? Discuss.", 124, "2025-05-26T12:00:00Z")
                )
            ),
            colors = DarkAppColors,
            onViewAllSchedule = {},
            onViewAllStandings = {}
        )
    }
}
