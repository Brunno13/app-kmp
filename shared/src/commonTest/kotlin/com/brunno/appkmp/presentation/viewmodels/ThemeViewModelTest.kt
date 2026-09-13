package com.brunno.appkmp.presentation.viewmodels

import com.brunno.appkmp.domain.enums.ThemeMode
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeViewModelTest {

    @Test
    fun defaultsToAutoWhenNoThemeIsSaved() {
        val settings = MapSettings()

        val viewModel = ThemeViewModel(settings)

        assertEquals(
            ThemeMode.AUTO,
            viewModel.themeMode.value
        )
    }

    @Test
    fun setThemeUpdatesStateAndPersistsSelection() {
        val settings = MapSettings()
        val selectedTheme = ThemeMode.entries.first {
            it != ThemeMode.AUTO
        }

        val viewModel = ThemeViewModel(settings)

        viewModel.setTheme(selectedTheme)

        assertEquals(
            selectedTheme,
            viewModel.themeMode.value
        )

        val recreatedViewModel = ThemeViewModel(settings)

        assertEquals(
            selectedTheme,
            recreatedViewModel.themeMode.value
        )
    }

    @Test
    fun invalidSavedThemeFallsBackToAuto() {
        val settings = MapSettings()

        settings.putString(
            "app_theme_mode",
            "INVALID_THEME"
        )

        val viewModel = ThemeViewModel(settings)

        assertEquals(
            ThemeMode.AUTO,
            viewModel.themeMode.value
        )
    }
}