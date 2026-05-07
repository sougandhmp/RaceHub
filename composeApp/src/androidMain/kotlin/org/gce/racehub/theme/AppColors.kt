package org.gce.racehub.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

fun hexColor(token: String): Color = Color(0xFF000000L or token.toLong(16))

val RacingRed = hexColor(AppColorTokens.racingRed)

data class AppColorScheme(
    val background: Color,
    val card: Color,
    val cardBorder: Color,
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
    mutedText = hexColor(AppColorTokens.lightMutedText),
    primaryText = Color.Black,
    headerBg = hexColor(AppColorTokens.lightHeaderBg),
    rowAltBg = hexColor(AppColorTokens.lightRowAltBg),
    navBar = hexColor(AppColorTokens.lightNavBar),
    isDark = false
)

val LocalAppColors = compositionLocalOf { DarkAppColors }
