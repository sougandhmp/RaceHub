package org.gce.racehub.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

fun hexColor(token: String): Color = Color(0xFF000000L or token.toLong(16))

val RacingRed = hexColor(AppColorTokens.racingRed)

data class AppColorScheme(
    val background: Color,
    val card: Color,
    val cardBorder: Color,
    val surface: Color,
    val mutedText: Color,
    val primaryText: Color,
    val headerBg: Color,
    val rowAltBg: Color,
    val navBar: Color,
    val isDark: Boolean
) {
    val racingRed: Color get() = RacingRed
}

val DarkAppColors = AppColorScheme(
    background = hexColor(AppColorTokens.darkBackground),
    card = hexColor(AppColorTokens.darkCard),
    cardBorder = hexColor(AppColorTokens.darkCardBorder),
    surface = hexColor(AppColorTokens.darkSurface),
    mutedText = hexColor(AppColorTokens.darkMutedText),
    primaryText = Color.White,
    headerBg = hexColor(AppColorTokens.darkHeaderBg),
    rowAltBg = hexColor(AppColorTokens.darkRowAltBg),
    navBar = hexColor(AppColorTokens.darkNavBar),
    isDark = true
)

val LightAppColors = AppColorScheme(
    background = hexColor(AppColorTokens.lightBackground),
    card = hexColor(AppColorTokens.lightCard),
    cardBorder = hexColor(AppColorTokens.lightCardBorder),
    surface = hexColor(AppColorTokens.lightSurface),
    mutedText = hexColor(AppColorTokens.lightMutedText),
    primaryText = Color.Black,
    headerBg = hexColor(AppColorTokens.lightHeaderBg),
    rowAltBg = hexColor(AppColorTokens.lightRowAltBg),
    navBar = hexColor(AppColorTokens.lightNavBar),
    isDark = false
)

val LocalAppColors = compositionLocalOf { DarkAppColors }

fun teamColorOf(team: String): Color {
    val t = team.lowercase()
    return when {
        "mercedes" in t -> hexColor(AppColorTokens.teamMercedes)
        "mclaren" in t  -> hexColor(AppColorTokens.teamMcLaren)
        "red bull" in t -> hexColor(AppColorTokens.teamRedBull)
        "ferrari" in t  -> hexColor(AppColorTokens.teamFerrari)
        "aston" in t    -> hexColor(AppColorTokens.teamAston)
        "alpine" in t   -> hexColor(AppColorTokens.teamAlpine)
        "williams" in t -> hexColor(AppColorTokens.teamWilliams)
        "rb" in t || "racing bulls" in t -> hexColor(AppColorTokens.teamRb)
        "haas" in t     -> hexColor(AppColorTokens.teamHaas)
        "sauber" in t || "kick" in t -> hexColor(AppColorTokens.teamSauber)
        else            -> hexColor(AppColorTokens.teamDefault)
    }
}
