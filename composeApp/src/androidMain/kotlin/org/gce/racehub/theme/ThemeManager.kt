package org.gce.racehub.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

enum class ThemeMode { SYSTEM, DARK, LIGHT }

class ThemeManager(context: Context) {
    private val prefs = context.getSharedPreferences("racehub_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadInitialMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString("theme_mode", mode.name) }
        _themeMode.value = mode
    }

    private fun loadInitialMode(): ThemeMode {
        val stored = prefs.getString("theme_mode", null)
        if (stored != null) return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.SYSTEM)
        // migrate from old boolean key
        return if (prefs.contains("is_dark_mode")) {
            if (prefs.getBoolean("is_dark_mode", true)) ThemeMode.DARK else ThemeMode.LIGHT
        } else {
            ThemeMode.SYSTEM
        }
    }
}
