package com.brunno.appkmp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.brunno.appkmp.domain.enums.ThemeMode
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(
    private val settings: Settings
) : ViewModel() {

    companion object {
        private const val PREF_THEME_MODE = "app_theme_mode"
    }

    private val _themeMode = MutableStateFlow(getSavedTheme())
    val themeMode = _themeMode.asStateFlow()

    fun setTheme(mode: ThemeMode) {
        settings.putString(PREF_THEME_MODE, mode.name)
        _themeMode.value = mode
    }

    private fun getSavedTheme(): ThemeMode {
        val saved = settings.getString(PREF_THEME_MODE, ThemeMode.AUTO.name)
        return try {
            ThemeMode.valueOf(saved)
        } catch (e: Exception) {
            ThemeMode.AUTO
        }
    }
}