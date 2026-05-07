package org.gce.racehub.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.gce.racehub.race.circuitDrawable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.TrendingThread
import org.gce.racehub.theme.AppColorScheme
import org.gce.racehub.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    schedule: List<Race>,
    latestThread: TrendingThread?,
    onBack: () -> Unit
) {
    val colors = LocalAppColors.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "SCHEDULE",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = colors.primaryText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.primaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background
    ) { padding ->
        val nextRace = schedule.firstOrNull { !it.isCompleted }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                if (latestThread != null) {
                    ScheduleLatestThreadCard(thread = latestThread, colors = colors)
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = colors.cardBorder
                    )
                }
            }
            items(schedule) { race ->
                RaceItem(race = race, isNextRace = race == nextRace, colors = colors)
            }
        }
    }
}

@Composable
private fun RaceItem(race: Race, isNextRace: Boolean, colors: AppColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(
                width = if (isNextRace) 2.dp else 1.dp,
                color = if (isNextRace) colors.racingRed else colors.cardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ROUND ${race.round}",
                color = if (race.isCompleted) colors.mutedText else colors.racingRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (race.isCompleted) {
                    Text(
                        text = "COMPLETED",
                        color = colors.mutedText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (isNextRace) {
                    Surface(
                        color = colors.racingRed,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "NEXT RACE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = race.status,
                        color = colors.racingRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = race.name,
            color = colors.primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${race.city}, ${race.country}",
            color = colors.mutedText,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )

        Text(
            text = race.circuit,
            color = colors.mutedText,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = race.dateTime,
                color = colors.mutedText,
                fontSize = 14.sp
            )
            race.weather?.let { weather ->
                Spacer(modifier = Modifier.width(8.dp))
                WeatherChip(weather = weather, colors = colors)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        CircuitImage(circuitName = race.circuit, grandPrixName = race.name, colors = colors)
    }
}

@Composable
private fun CircuitImage(circuitName: String, grandPrixName: String, colors: AppColorScheme) {
    val resId = circuitDrawable(circuitName) ?: circuitDrawable(grandPrixName)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.cardBorder.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        if (resId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(resId),
                contentDescription = "Circuit Diagram",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = "🏁",
                fontSize = 24.sp,
                modifier = Modifier.alpha(0.3f)
            )
        }
    }
}

@Composable
private fun WeatherChip(weather: String, colors: AppColorScheme) {
    val emoji = weatherEmoji(weather)
    val bg = weatherBackground(weather)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, bg.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Text(
                text = weather.replaceFirstChar { it.uppercase() },
                color = bg,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun weatherEmoji(weather: String): String {
    val w = weather.lowercase()
    return when {
        "sun" in w || "clear" in w || "dry" in w || "fine" in w -> "☀️"
        "part" in w && "cloud" in w -> "⛅"
        "overcast" in w || "cloud" in w -> "🌥️"
        "storm" in w || "thunder" in w || "lightning" in w -> "⛈️"
        "rain" in w || "shower" in w || "wet" in w -> "🌧️"
        "snow" in w || "sleet" in w -> "🌨️"
        "fog" in w || "mist" in w || "haz" in w -> "🌫️"
        "wind" in w || "breez" in w -> "💨"
        "hot" in w || "humid" in w -> "🌡️"
        else -> "🌤️"
    }
}

private fun weatherBackground(weather: String): Color {
    val w = weather.lowercase()
    return when {
        "sun" in w || "clear" in w || "dry" in w || "fine" in w -> Color(0xFFF59E0B)
        "part" in w && "cloud" in w -> Color(0xFF94A3B8)
        "overcast" in w || "cloud" in w -> Color(0xFF64748B)
        "storm" in w || "thunder" in w || "lightning" in w -> Color(0xFF7C3AED)
        "rain" in w || "shower" in w || "wet" in w -> Color(0xFF3B82F6)
        "snow" in w || "sleet" in w -> Color(0xFF93C5FD)
        "fog" in w || "mist" in w || "haz" in w -> Color(0xFF9CA3AF)
        "wind" in w || "breez" in w -> Color(0xFF06B6D4)
        "hot" in w || "humid" in w -> Color(0xFFEF4444)
        else -> Color(0xFF60A5FA)
    }
}

@Composable
private fun ScheduleLatestThreadCard(thread: TrendingThread, colors: AppColorScheme) {
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
            Text(
                text = "LATEST DISCUSSION",
                color = colors.racingRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.racingRed),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💬", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = thread.title,
                    color = colors.primaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "❤️", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${thread.likes} likes", color = colors.mutedText, fontSize = 11.sp)
                }
            }
        }
    }
}
