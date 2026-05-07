package org.gce.racehub.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val RacingRed = Color(0xFFE63946)

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
    background = Color(0xFF0A0A0A),
    card = Color(0xFF161616),
    cardBorder = Color(0xFF262626),
    mutedText = Color(0xFF8E8E93),
    primaryText = Color.White,
    headerBg = Color(0xFF2A1116),
    rowAltBg = Color(0xFF1B0E11),
    navBar = Color(0xFF121212),
    isDark = true
)

val LightAppColors = AppColorScheme(
    background = Color(0xFFF2F2F7),
    card = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE5E5EA),
    mutedText = Color(0xFF6C6C70),
    primaryText = Color(0xFF000000),
    headerBg = Color(0xFFFFECEE),
    rowAltBg = Color(0xFFFFF5F6),
    navBar = Color(0xFFFFFFFF),
    isDark = false
)

val LocalAppColors = compositionLocalOf { DarkAppColors }
