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
import androidx.compose.material3.HorizontalDivider
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
import org.gce.racehub.theme.AppColorTokens
import org.gce.racehub.theme.DarkAppColors
import org.gce.racehub.theme.LocalAppColors
import org.gce.racehub.theme.hexColor
import org.koin.compose.viewmodel.koinViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceScreen(
    viewModel: RaceViewModel = koinViewModel(),
    onViewAllSchedule: () -> Unit,
    onViewAllStandings: () -> Unit
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
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item {
            NextRaceSection(
                race = state.raceSchedule.firstOrNull { !it.isCompleted },
                nextRaceDetail = state.nextRaceDetail,
                colors = colors,
                onViewAllSchedule = onViewAllSchedule
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
    colors: AppColorScheme,
    onViewAllSchedule: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
    ) {
        // Faded circuit image on the right
        val resId = race?.let { circuitDrawable(it.circuit) ?: circuitDrawable(it.name) }
        if (resId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(resId),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(170.dp)
                    .padding(end = 4.dp)
                    .alpha(0.10f),
                contentScale = ContentScale.Fit
            )
        }

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)) {
            // Header: ● UP NEXT + days countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colors.racingRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "UP NEXT",
                        color = colors.mutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                }
                val days = race?.let { daysUntilRace(it.dateTime) }
                if (days != null) {
                    Text(
                        text = "$days DAYS",
                        color = colors.racingRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Race name (leave right 35% clear for circuit image)
            Text(
                text = race?.name ?: "No Upcoming Race",
                color = colors.primaryText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 30.sp,
                modifier = Modifier.fillMaxWidth(0.65f)
            )

            // R9 · Circuit Gilles Villeneuve
            if (race != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "R${race.round} · ${race.circuit}",
                    color = colors.mutedText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats: LIGHTS OUT | LENGTH | LAPS
            val lightsOut = race?.let { formatLightsOut(it.dateTime) } ?: "—"
            val length = nextRaceDetail?.trackFacts?.let { "${it.distanceKm} km" } ?: "—"
            val laps = nextRaceDetail?.trackFacts?.laps?.toString() ?: "—"

            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem("LIGHTS OUT", lightsOut, colors, Modifier.weight(1f))
                StatItem("LENGTH", length, colors, Modifier.weight(1f))
                StatItem("LAPS", laps, colors, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Session strip: FP1 | FP2 | FP3 | QUAL | RACE
            val sessions = if (nextRaceDetail?.sessions?.isNotEmpty() == true)
                sessionsFromDetail(nextRaceDetail.sessions)
            else
                sessionsFromRace(race)
            SessionStrip(sessions = sessions, colors = colors)

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = colors.cardBorder,
                thickness = 0.5.dp
            )

            TextButton(
                onClick = onViewAllSchedule,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 36.dp)
            ) {
                Text(
                    text = "Full schedule",
                    color = colors.racingRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = colors.racingRed,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    colors: AppColorScheme,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = colors.mutedText,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = colors.primaryText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class SessionStripChip(val label: String, val month: String)

@Composable
private fun SessionStrip(sessions: List<RaceSessionChip>, colors: AppColorScheme) {
    val chips = sessions.map { chip ->
        val month = chip.fullDate.split(" ").firstOrNull()?.take(3) ?: "—"
        SessionStripChip(label = stripLabel(chip.label), month = month)
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isRace) colors.racingRed
                        else colors.cardBorder.copy(alpha = 0.4f)
                    )
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = chip.label,
                    color = if (isRace) Color.White else colors.mutedText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                )
                Text(
                    text = chip.month,
                    color = if (isRace) Color.White.copy(alpha = 0.9f) else colors.primaryText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
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
    val tColor = teamColor(standing.team)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp)),
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
                    text = "PTS",
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
    val tColor = teamColor(standing.name)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp)),
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
                    text = "${standing.wins} wins",
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
                    text = "PTS",
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
                text = "FEATURED DISCUSSION",
                color = colors.mutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Forum →",
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
                Text("No trending topics yet", color = colors.mutedText, fontSize = 14.sp)
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
                            text = "TRENDING",
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
                                text = "${thread.likes} likes",
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

private data class RaceSessionChip(
    val label: String,
    val fullDate: String,
    val time: String,
    val timezone: String
)

private fun daysUntilRace(dateTime: String): Int? {
    val raceDate = parseIsoToDate(dateTime) ?: return null
    val now = java.util.Date()
    if (raceDate.before(now)) return null
    val diffMs = raceDate.time - now.time
    val days = (diffMs / (1000L * 60 * 60 * 24)).toInt()
    return if (days >= 0) days else null
}

private fun formatLightsOut(dateTime: String): String {
    val date = parseIsoToDate(dateTime) ?: return "—"
    return java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        .format(date) + " UTC"
}

private fun stripLabel(full: String): String = when (full.uppercase().trim()) {
    "PRACTICE 1" -> "FP1"
    "PRACTICE 2" -> "FP2"
    "PRACTICE 3" -> "FP3"
    "QUALIFYING" -> "QUAL"
    "RACE" -> "RACE"
    "SPRINT" -> "SPR"
    "SPRINT QUAL" -> "SQ"
    else -> full.take(4)
}

private fun teamColor(team: String): Color {
    val t = team.lowercase()
    return when {
        "mercedes" in t -> hexColor(AppColorTokens.teamMercedes)
        "mclaren" in t -> hexColor(AppColorTokens.teamMcLaren)
        "red bull" in t -> hexColor(AppColorTokens.teamRedBull)
        "ferrari" in t -> hexColor(AppColorTokens.teamFerrari)
        "aston" in t -> hexColor(AppColorTokens.teamAston)
        "alpine" in t -> hexColor(AppColorTokens.teamAlpine)
        "williams" in t -> hexColor(AppColorTokens.teamWilliams)
        "rb" in t || "racing bulls" in t -> hexColor(AppColorTokens.teamRb)
        "haas" in t -> hexColor(AppColorTokens.teamHaas)
        "sauber" in t || "kick" in t -> hexColor(AppColorTokens.teamSauber)
        else -> hexColor(AppColorTokens.teamDefault)
    }
}

private fun deviceTimezoneLabel(): String {
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

private fun formatSessionDate(cal: Calendar): String =
    java.text.SimpleDateFormat("MMM d, yyyy,", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getDefault() }
        .format(cal.time)

private fun formatSessionTime(cal: Calendar): String =
    java.text.SimpleDateFormat("h:mm a", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getDefault() }
        .format(cal.time)

private fun displayLabel(raw: String): String = when (raw.uppercase().trim()) {
    "FP1", "PRACTICE 1", "P1", "PRACTICE1" -> "PRACTICE 1"
    "FP2", "PRACTICE 2", "P2", "PRACTICE2" -> "PRACTICE 2"
    "FP3", "PRACTICE 3", "P3", "PRACTICE3" -> "PRACTICE 3"
    "QUAL", "QUALIFYING", "Q" -> "QUALIFYING"
    "RACE", "GRAND PRIX" -> "RACE"
    "SPRINT QUALIFYING", "SPRINT QUAL", "SQ" -> "SPRINT QUAL"
    "SPRINT" -> "SPRINT"
    else -> raw.uppercase()
}

private fun parseIsoToDate(dateTime: String): java.util.Date? {
    if (dateTime.isBlank()) return null
    val s = dateTime.trim().replace(Regex("([+-]\\d{2}):(\\d{2})$"), "$1$2")
    val utcPatterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
    )
    val tzPatterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ",
    )
    val utc = java.util.TimeZone.getTimeZone("UTC")
    for (fmt in utcPatterns) {
        try {
            val d = java.text.SimpleDateFormat(fmt, java.util.Locale.US).apply { timeZone = utc }
                .parse(s)
            if (d != null) return d
        } catch (_: Exception) {
        }
    }
    for (fmt in tzPatterns) {
        try {
            val d = java.text.SimpleDateFormat(fmt, java.util.Locale.US).parse(s)
            if (d != null) return d
        } catch (_: Exception) {
        }
    }
    return null
}

private fun sessionsFromRace(race: Race?): List<RaceSessionChip> {
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

private fun fallbackSessions(): List<RaceSessionChip> {
    val tz = deviceTimezoneLabel()
    return listOf(
        RaceSessionChip("PRACTICE 1", "—", "—", tz),
        RaceSessionChip("PRACTICE 2", "—", "—", tz),
        RaceSessionChip("PRACTICE 3", "—", "—", tz),
        RaceSessionChip("QUALIFYING", "—", "—", tz),
        RaceSessionChip("RACE", "—", "—", tz),
    )
}

private fun sessionsFromDetail(sessions: List<RaceSession>): List<RaceSessionChip> {
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
