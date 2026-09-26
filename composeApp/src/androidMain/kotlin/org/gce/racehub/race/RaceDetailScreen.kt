package org.gce.racehub.race

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.gce.racehub.race.domain.model.FastestLap
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.presentation.WeekendSession
import org.gce.racehub.race.presentation.weekendSessions
import org.gce.racehub.race.domain.model.RaceResult
import org.gce.racehub.race.domain.model.RaceSession
import org.gce.racehub.race.domain.model.TrackFacts
import org.gce.racehub.theme.AppColorScheme
import org.gce.racehub.theme.DarkAppColors
import org.gce.racehub.theme.Dimens
import org.gce.racehub.theme.LocalAppColors
import org.jetbrains.compose.resources.stringResource
import racehub.composeapp.generated.resources.Res
import racehub.composeapp.generated.resources.contentdesc_back
import racehub.composeapp.generated.resources.heading_circuit_map
import racehub.composeapp.generated.resources.heading_fastest_lap
import racehub.composeapp.generated.resources.heading_race_info
import racehub.composeapp.generated.resources.heading_race_results
import racehub.composeapp.generated.resources.heading_track_facts
import racehub.composeapp.generated.resources.heading_weekend_schedule
import racehub.composeapp.generated.resources.label_circuit_name
import racehub.composeapp.generated.resources.label_corners
import racehub.composeapp.generated.resources.label_date
import racehub.composeapp.generated.resources.label_distance_km
import racehub.composeapp.generated.resources.label_driver
import racehub.composeapp.generated.resources.label_lap_record
import racehub.composeapp.generated.resources.label_laps
import racehub.composeapp.generated.resources.label_local_time
import racehub.composeapp.generated.resources.label_location
import racehub.composeapp.generated.resources.label_points_abbr
import racehub.composeapp.generated.resources.label_pos
import racehub.composeapp.generated.resources.label_round_number
import racehub.composeapp.generated.resources.label_status
import racehub.composeapp.generated.resources.label_time
import racehub.composeapp.generated.resources.label_weather
import racehub.composeapp.generated.resources.title_race_detail

@Composable
fun RaceDetailScreen(
    race: Race,
    raceDetail: RaceDetail? = null,
    sessions: List<WeekendSession> = weekendSessions(race, raceDetail),
    onBack: () -> Unit = {}
) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        DetailTopBar(onBack = onBack, colors = colors)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 16.dp,
                bottom = 48.dp
            )
        ) {
            item { HeroCard(race = race, colors = colors) }
            item { CircuitMapSection(race = race, colors = colors) }
            val trackFacts = raceDetail?.trackFacts
            if (trackFacts != null) {
                item { TrackFactsSection(facts = trackFacts, colors = colors) }
            }
            item {
                ScheduleSection(sessions = sessions, colors = colors)
            }
            if (raceDetail?.results?.isNotEmpty() == true) {
                item { ResultsSection(results = raceDetail.results, colors = colors) }
            }
            val fastestLap = raceDetail?.fastestLap
            if (fastestLap != null) {
                item { FastestLapSection(fastestLap = fastestLap, colors = colors) }
            }
            item { InfoSection(race = race, colors = colors) }
        }
    }
}

// ── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun DetailTopBar(onBack: () -> Unit, colors: AppColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.contentdesc_back),
                tint = colors.primaryText,
                modifier = Modifier.size(24.dp)
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(Res.string.title_race_detail),
                color = colors.primaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }
        Spacer(modifier = Modifier.size(48.dp))
    }
}

// ── Hero Card ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(race: Race, colors: AppColorScheme) {
    val statusColor = statusColor(race.status, colors)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Dimens.cardShape)
            .background(colors.card)
            .border(1.dp, colors.cardBorder, Dimens.cardShape)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge(text = "R${race.round}", background = colors.racingRed, textColor = Color.White)
                    Badge(
                        text = race.status.lowercase().replaceFirstChar { it.uppercase() },
                        background = statusColor.copy(alpha = 0.12f),
                        textColor = statusColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = race.name,
                    color = colors.primaryText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 28.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = countryFlag(race.country), fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colors.cardBorder)
        Spacer(modifier = Modifier.height(16.dp))

        DetailInfoRow(icon = "📍", label = "${race.city}, ${race.country}", colors = colors)
        Spacer(modifier = Modifier.height(8.dp))
        DetailInfoRow(icon = "🏟", label = race.circuit, colors = colors)
        Spacer(modifier = Modifier.height(8.dp))
        DetailInfoRow(icon = "📅", label = formatDetailDate(race.dateTime), colors = colors)
        Spacer(modifier = Modifier.height(8.dp))
        DetailInfoRow(icon = "⏱", label = "Race starts ${formatRaceTime(race.dateTime)} local", colors = colors)
        val weather = race.weather
        if (!weather.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            DetailInfoRow(icon = weatherIcon(weather), label = weather, colors = colors)
        }
    }
}

// ── Circuit Map ───────────────────────────────────────────────────────────────

@Composable
private fun CircuitMapSection(race: Race, colors: AppColorScheme) {
    SectionCard(title = stringResource(Res.string.heading_circuit_map), colors = colors) {
        val resId = circuitDrawable(race.circuit) ?: circuitDrawable(race.name)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (resId != null) {
                androidx.compose.foundation.Image(
                    painter = painterResource(resId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(text = "🏁", fontSize = 48.sp)
            }
        }
    }
}

// ── Track Facts ───────────────────────────────────────────────────────────────

@Composable
private fun TrackFactsSection(facts: TrackFacts, colors: AppColorScheme) {
    SectionCard(title = stringResource(Res.string.heading_track_facts), colors = colors) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FactCell(label = stringResource(Res.string.label_laps), value = facts.laps.toString(), colors = colors)
            FactDivider(colors = colors)
            FactCell(label = stringResource(Res.string.label_corners), value = facts.corners.toString(), colors = colors)
            FactDivider(colors = colors)
            FactCell(label = stringResource(Res.string.label_distance_km), value = "${facts.distanceKm} km", colors = colors)
        }
        val lapRecord = facts.lapRecord
        if (!lapRecord.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = colors.cardBorder, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.label_lap_record),
                    color = colors.mutedText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = lapRecord,
                    color = colors.primaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Weekend Schedule ──────────────────────────────────────────────────────────

@Composable
private fun ScheduleSection(sessions: List<WeekendSession>, colors: AppColorScheme) {
    Column {
        SectionHeading(title = stringResource(Res.string.heading_weekend_schedule), colors = colors)
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Dimens.cardShape)
                .background(colors.card)
                .border(1.dp, colors.cardBorder, Dimens.cardShape)
        ) {
            sessions.forEachIndexed { index, chip ->
                val isRace = chip.label == "RACE"
                val labelText = chip.shortLabel

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isRace) colors.racingRed.copy(alpha = 0.04f) else Color.Transparent)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isRace) colors.racingRed else colors.surface)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labelText,
                            color = if (isRace) Color.White else colors.mutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.3.sp
                        )
                    }
                    Text(
                        text = chip.date,
                        color = colors.primaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = chip.time,
                        color = if (isRace) colors.racingRed else colors.mutedText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (index < sessions.lastIndex) {
                    HorizontalDivider(
                        color = colors.cardBorder,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

// ── Race Results ──────────────────────────────────────────────────────────────

@Composable
private fun ResultsSection(results: List<RaceResult>, colors: AppColorScheme) {
    SectionCard(title = stringResource(Res.string.heading_race_results), colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(Res.string.label_pos),
                color = colors.mutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp)
            )
            Text(
                text = stringResource(Res.string.label_driver),
                color = colors.mutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(Res.string.label_points_abbr),
                color = colors.mutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp)
            )
        }
        HorizontalDivider(color = colors.cardBorder, modifier = Modifier.padding(horizontal = 16.dp))

        results.forEachIndexed { index, result ->
            ResultRow(result = result, colors = colors)
            if (index < results.lastIndex) {
                HorizontalDivider(color = colors.cardBorder, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun ResultRow(result: RaceResult, colors: AppColorScheme) {
    val isPodium = result.position <= 3
    val posColor = when (result.position) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> colors.mutedText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = result.position.toString(),
            color = if (isPodium) posColor else colors.mutedText,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.width(36.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.driver,
                color = colors.primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = result.team,
                color = colors.mutedText,
                fontSize = 12.sp
            )
        }
        Text(
            text = result.points.toString(),
            color = colors.primaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp)
        )
    }
}

// ── Fastest Lap ───────────────────────────────────────────────────────────────

@Composable
private fun FastestLapSection(fastestLap: FastestLap, colors: AppColorScheme) {
    SectionCard(title = stringResource(Res.string.heading_fastest_lap), colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.label_driver),
                    color = colors.mutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fastestLap.driver,
                    color = colors.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.label_time),
                    color = colors.mutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fastestLap.time,
                    color = colors.racingRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

// ── Race Info Table ───────────────────────────────────────────────────────────

@Composable
private fun InfoSection(race: Race, colors: AppColorScheme) {
    val statusColor = statusColor(race.status, colors)

    Column {
        SectionHeading(title = stringResource(Res.string.heading_race_info), colors = colors)
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Dimens.cardShape)
                .background(colors.card)
                .border(1.dp, colors.cardBorder, Dimens.cardShape)
        ) {
            InfoRow(
                label = stringResource(Res.string.label_status),
                value = race.status.lowercase().replaceFirstChar { it.uppercase() },
                valueColor = statusColor,
                colors = colors
            )
            InfoDivider(colors)
            InfoRow(
                label = stringResource(Res.string.label_round_number),
                value = "#${race.round} of the season",
                colors = colors
            )
            InfoDivider(colors)
            InfoRow(
                label = stringResource(Res.string.label_date),
                value = formatDetailDate(race.dateTime),
                colors = colors
            )
            InfoDivider(colors)
            InfoRow(
                label = stringResource(Res.string.label_local_time),
                value = formatRaceTime(race.dateTime),
                colors = colors
            )
            InfoDivider(colors)
            InfoRow(
                label = stringResource(Res.string.label_circuit_name),
                value = race.circuit,
                colors = colors
            )
            InfoDivider(colors)
            InfoRow(
                label = stringResource(Res.string.label_location),
                value = "${race.city}, ${race.country}",
                colors = colors
            )
            val infoWeather = race.weather
            if (!infoWeather.isNullOrEmpty()) {
                InfoDivider(colors)
                InfoRow(
                    label = stringResource(Res.string.label_weather),
                    value = "${weatherIcon(infoWeather)} $infoWeather",
                    colors = colors
                )
            }
        }
    }
}

// ── Shared sub-components ─────────────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, colors: AppColorScheme, content: @Composable () -> Unit) {
    Column {
        SectionHeading(title = title, colors = colors)
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Dimens.cardShape)
                .background(colors.card)
                .border(1.dp, colors.cardBorder, Dimens.cardShape)
                .padding(vertical = 16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SectionHeading(title: String, colors: AppColorScheme) {
    Text(
        text = title,
        color = colors.mutedText,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp
    )
}

@Composable
private fun Badge(text: String, background: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(background)
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun DetailInfoRow(icon: String, label: String, colors: AppColorScheme) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 14.sp, modifier = Modifier.width(22.dp))
        Text(
            text = label,
            color = colors.mutedText,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun FactCell(label: String, value: String, colors: AppColorScheme) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = value,
            color = colors.primaryText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = colors.mutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun FactDivider(colors: AppColorScheme) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .width(1.dp)
            .background(colors.cardBorder)
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color? = null,
    colors: AppColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = colors.mutedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(96.dp)
        )
        Text(
            text = value,
            color = valueColor ?: colors.primaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun InfoDivider(colors: AppColorScheme) {
    HorizontalDivider(
        color = colors.cardBorder,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

// ── Pure helpers ──────────────────────────────────────────────────────────────

private fun statusColor(status: String, colors: AppColorScheme): Color = when (status.lowercase()) {
    "live" -> Color(0xFF22C55E)
    "completed" -> colors.mutedText
    else -> colors.racingRed
}

private fun formatDetailDate(dateTime: String): String {
    val date = parseIsoToDate(dateTime) ?: return dateTime
    // Local zone, matching formatRaceTime, so "May 25 … starts 06:00 local" agree.
    return java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getDefault() }
        .format(date)
}

private fun formatRaceTime(dateTime: String): String {
    val date = parseIsoToDate(dateTime) ?: return ""
    return java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getDefault() }
        .format(date)
}

private fun weatherIcon(weather: String): String {
    val w = weather.lowercase()
    return when {
        w.contains("sun") || w.contains("clear") -> "☀️"
        w.contains("cloud") -> "⛅"
        w.contains("rain") || w.contains("wet") -> "🌧️"
        w.contains("wind") -> "💨"
        else -> "🌡️"
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun RaceDetailScreenPreview() {
    CompositionLocalProvider(LocalAppColors provides DarkAppColors) {
        RaceDetailScreen(
            race = Race(
                id = "australia-2025",
                name = "Australian Grand Prix",
                circuit = "Albert Park Circuit",
                country = "Australia",
                city = "Melbourne",
                dateTime = "2025-03-16T05:00:00Z",
                round = 3,
                status = "COMPLETED",
                weather = "Sunny"
            ),
            raceDetail = RaceDetail(
                grandPrix = "Australian Grand Prix",
                circuit = "Albert Park Circuit",
                overview = null,
                trackFacts = TrackFacts(laps = 58, lapRecord = "1:20.235", distanceKm = 5.278, corners = 16),
                sessions = listOf(
                    RaceSession("Practice 1", "2025-03-14T01:30:00Z"),
                    RaceSession("Practice 2", "2025-03-14T05:00:00Z"),
                    RaceSession("Practice 3", "2025-03-15T01:30:00Z"),
                    RaceSession("Qualifying", "2025-03-15T05:00:00Z"),
                    RaceSession("Race", "2025-03-16T05:00:00Z")
                ),
                results = listOf(
                    RaceResult(1, "Lando Norris", "McLaren", 25, "1:30:33.433"),
                    RaceResult(2, "Charles Leclerc", "Ferrari", 18, "+2.366s"),
                    RaceResult(3, "Carlos Sainz", "Ferrari", 15, "+6.061s")
                ),
                fastestLap = FastestLap("Lando Norris", "1:18.994")
            ),
            onBack = {}
        )
    }
}
