package org.gce.racehub.race

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.shape.CircleShape
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.RaceSession
import org.gce.racehub.race.domain.model.TrackFacts
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

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.onIntent(RaceIntent.Refresh) },
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
                totalRounds = state.raceSchedule.size,
                nextRaceDetail = state.nextRaceDetail,
                isLoadingDetail = state.isLoadingDetail,
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
private fun NextRaceSection(
    race: Race?,
    totalRounds: Int,
    nextRaceDetail: RaceDetail?,
    isLoadingDetail: Boolean,
    colors: AppColorScheme,
    onViewAll: () -> Unit
) {
    val shimmer = shimmerBrush(colors)
    var isExpanded by rememberSaveable { mutableStateOf(false) }

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
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
            TextButton(
                onClick = onViewAll,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.heightIn(min = 24.dp)
            ) {
                Text(
                    text = "See Full Schedule",
                    color = colors.racingRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.card)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                // R9/24 · MAY 24  |  🇨🇦
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildRoundLabel(race, totalRounds),
                        color = colors.mutedText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = countryFlag(race?.country ?: ""),
                        fontSize = 30.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Country name — large title
                Text(
                    text = race?.country ?: "No Upcoming Race",
                    color = colors.primaryText,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 38.sp
                )

                // Circuit name
                Text(
                    text = race?.circuit ?: "—",
                    color = colors.mutedText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Track facts — always visible
                if (isLoadingDetail) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(shimmer)
                            )
                        }
                    }
                } else {
                    nextRaceDetail?.trackFacts?.let { facts ->
                        TrackFactsRow(facts = facts, colors = colors)
                    }
                }

                // Expanded: circuit map + session schedule
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))

                        val resId = circuitDrawable(race?.circuit ?: "") ?: circuitDrawable(race?.name ?: "")
                        if (resId != null) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(resId),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .alpha(0.75f),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isLoadingDetail) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    repeat(2) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(82.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(shimmer)
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    repeat(2) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(82.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(shimmer)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(82.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(shimmer)
                                )
                            }
                        } else {
                            val sessions = if (nextRaceDetail?.sessions?.isNotEmpty() == true)
                                sessionsFromDetail(nextRaceDetail.sessions)
                            else
                                sessionsFromRace(race)
                            SessionGrid(sessions = sessions, colors = colors)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Expand / collapse toggle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isExpanded) "▲  Show less" else "▼  Show schedule",
                        color = colors.mutedText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackFactsRow(facts: TrackFacts, colors: AppColorScheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TrackFactItem("LAPS", facts.laps.toString(), colors, Modifier.weight(1f))
        TrackFactItem("DISTANCE", "${facts.distanceKm} km", colors, Modifier.weight(1f))
        TrackFactItem("CORNERS", facts.corners.toString(), colors, Modifier.weight(1f))
    }
}

@Composable
private fun TrackFactItem(label: String, value: String, colors: AppColorScheme, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.cardBorder.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

@Composable
private fun shimmerBrush(colors: AppColorScheme): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )
    return Brush.linearGradient(
        colors = listOf(
            colors.cardBorder.copy(alpha = 0.25f),
            colors.cardBorder.copy(alpha = 0.6f),
            colors.cardBorder.copy(alpha = 0.25f)
        ),
        start = Offset(progress * 800f - 400f, 0f),
        end = Offset(progress * 800f + 200f, 0f)
    )
}

@Composable
private fun SessionGrid(sessions: List<RaceSessionChip>, colors: AppColorScheme) {
    val pairs = sessions.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pairs.forEach { pair ->
            if (pair.size == 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SessionGridCard(session = pair[0], colors = colors, modifier = Modifier.weight(1f))
                    SessionGridCard(session = pair[1], colors = colors, modifier = Modifier.weight(1f))
                }
            } else {
                SessionGridCard(session = pair[0], colors = colors, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun SessionGridCard(session: RaceSessionChip, colors: AppColorScheme, modifier: Modifier = Modifier) {
    val isRace = session.label == "RACE"
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isRace) colors.racingRed.copy(alpha = 0.08f) else colors.cardBorder.copy(alpha = 0.3f))
            .then(
                if (isRace) Modifier.border(1.dp, colors.racingRed.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = session.label,
            color = colors.racingRed,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = session.fullDate,
            color = colors.primaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 18.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = session.time,
                color = colors.primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = " ${session.timezone}",
                color = colors.mutedText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


// ── Next-race card helpers ────────────────────────────────────────────────────

private data class RaceSessionChip(
    val label: String,    // e.g. "PRACTICE 1", "QUALIFYING", "RACE"
    val fullDate: String, // e.g. "May 22, 2026,"
    val time: String,     // e.g. "10:00 PM"
    val timezone: String  // e.g. "GMT+5:30"
)

private fun countryFlag(country: String): String {
    val c = country.lowercase()
    return when {
        "australia" in c -> "🇦🇺"
        "bahrain" in c -> "🇧🇭"
        "saudi" in c -> "🇸🇦"
        "japan" in c -> "🇯🇵"
        "china" in c -> "🇨🇳"
        "usa" in c || "united states" in c || "america" in c -> "🇺🇸"
        "italy" in c -> "🇮🇹"
        "monaco" in c -> "🇲🇨"
        "spain" in c -> "🇪🇸"
        "canada" in c -> "🇨🇦"
        "austria" in c -> "🇦🇹"
        "britain" in c || "united kingdom" in c -> "🇬🇧"
        "hungary" in c -> "🇭🇺"
        "belgium" in c -> "🇧🇪"
        "netherlands" in c -> "🇳🇱"
        "azerbaijan" in c -> "🇦🇿"
        "singapore" in c -> "🇸🇬"
        "mexico" in c -> "🇲🇽"
        "brazil" in c -> "🇧🇷"
        "qatar" in c -> "🇶🇦"
        "abu dhabi" in c || "uae" in c -> "🇦🇪"
        else -> "🏁"
    }
}

private fun buildRoundLabel(race: Race?, totalRounds: Int): String {
    if (race == null) return "— · —"
    val total = if (totalRounds > 0) totalRounds.toString() else "—"
    return "R${race.round}/$total · ${formatRaceHeaderDate(race.dateTime)}"
}

private fun formatRaceHeaderDate(dateTime: String): String {
    val date = parseIsoToDate(dateTime) ?: return dateTime
    return java.text.SimpleDateFormat("MMM dd", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        .format(date).uppercase()
}

private fun deviceTimezoneLabel(): String {
    val tz = java.util.TimeZone.getDefault()
    val now = java.util.Date()
    val offset = tz.getOffset(now.time)
    val sign = if (offset >= 0) "+" else "-"
    val absOffset = java.lang.Math.abs(offset)
    val hours = absOffset / 3600000
    val minutes = (absOffset % 3600000) / 60000
    return if (minutes == 0) "GMT$sign$hours"
    else "GMT$sign$hours:${String.format(java.util.Locale.US, "%02d", minutes)}"
}

private fun formatSessionDate(cal: java.util.Calendar): String =
    java.text.SimpleDateFormat("MMM d, yyyy,", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getDefault() }
        .format(cal.time)

private fun formatSessionTime(cal: java.util.Calendar): String =
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

// Parses any common ISO 8601 variant: Z, .sssZ, +HH:MM, .sss+HH:MM, no-tz, date-only.
private fun parseIsoToDate(dateTime: String): java.util.Date? {
    if (dateTime.isBlank()) return null
    // Convert colon-offset "+05:30" → "+0530" so SimpleDateFormat's 'Z' token accepts it
    val s = dateTime.trim().replace(Regex("([+-]\\d{2}):(\\d{2})$"), "$1$2")
    // Patterns where the string has no explicit offset (treat as UTC)
    val utcPatterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
    )
    // Patterns where the string carries its own offset
    val tzPatterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ",
    )
    val utc = java.util.TimeZone.getTimeZone("UTC")
    for (fmt in utcPatterns) {
        try {
            val d = java.text.SimpleDateFormat(fmt, java.util.Locale.US).apply { timeZone = utc }.parse(s)
            if (d != null) return d
        } catch (_: Exception) {}
    }
    for (fmt in tzPatterns) {
        try {
            val d = java.text.SimpleDateFormat(fmt, java.util.Locale.US).parse(s)
            if (d != null) return d
        } catch (_: Exception) {}
    }
    return null
}

private fun sessionsFromRace(race: Race?): List<RaceSessionChip> {
    if (race == null) return emptyList()
    val raceDate = parseIsoToDate(race.dateTime) ?: return fallbackSessions(race)
    val tzLabel = deviceTimezoneLabel()

    fun calAt(days: Int, hours: Int = 0): java.util.Calendar =
        java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            time = raceDate
            add(java.util.Calendar.DATE, days)
            if (hours != 0) add(java.util.Calendar.HOUR_OF_DAY, hours)
        }

    return listOf(
        RaceSessionChip("PRACTICE 1", formatSessionDate(calAt(-2, -3)), formatSessionTime(calAt(-2, -3)), tzLabel),
        RaceSessionChip("PRACTICE 2", formatSessionDate(calAt(-2)),     formatSessionTime(calAt(-2)),     tzLabel),
        RaceSessionChip("PRACTICE 3", formatSessionDate(calAt(-1, -3)), formatSessionTime(calAt(-1, -3)), tzLabel),
        RaceSessionChip("QUALIFYING", formatSessionDate(calAt(-1)),     formatSessionTime(calAt(-1)),     tzLabel),
        RaceSessionChip("RACE",       formatSessionDate(calAt(0)),      formatSessionTime(calAt(0)),      tzLabel),
    )
}

private fun fallbackSessions(race: Race): List<RaceSessionChip> {
    val tz = deviceTimezoneLabel()
    return listOf(
        RaceSessionChip("PRACTICE 1", "—", "—", tz),
        RaceSessionChip("PRACTICE 2", "—", "—", tz),
        RaceSessionChip("PRACTICE 3", "—", "—", tz),
        RaceSessionChip("QUALIFYING", "—", "—", tz),
        RaceSessionChip("RACE",       "—", "—", tz),
    )
}

private fun sessionsFromDetail(sessions: List<RaceSession>): List<RaceSessionChip> {
    val tzLabel = deviceTimezoneLabel()
    return sessions.map { session ->
        val date = parseIsoToDate(session.dateTime)
        val (fullDate, time) = if (date != null) {
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).also { it.time = date }
            Pair(formatSessionDate(cal), formatSessionTime(cal))
        } else Pair("—", "—")
        RaceSessionChip(label = displayLabel(session.label), fullDate = fullDate, time = time, timezone = tzLabel)
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
