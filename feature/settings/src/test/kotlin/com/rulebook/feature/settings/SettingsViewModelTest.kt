package com.rulebook.feature.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @Test
    fun `initial state has expected default values`() = runTest {
        val viewModel = SettingsViewModel()
        val state = viewModel.uiState.first()

        assertFalse("Dark theme should be false by default", state.isDarkTheme)
        assertTrue("Haptics should be enabled by default", state.isHapticsEnabled)
        assertEquals("Version should be 1.0.0", "1.0.0", state.appVersion)
    }

    @Test
    fun `uiState exposes immutable state flow`() = runTest {
        val viewModel = SettingsViewModel()
        val state1 = viewModel.uiState.first()
        val state2 = viewModel.uiState.first()

        assertEquals("States should be equal", state1, state2)
    }

    @Test
    fun `onThemeToggle updates isDarkTheme state`() = runTest {
        val viewModel = SettingsViewModel()

        viewModel.onThemeToggle(true)
        assertTrue("Dark theme should be enabled", viewModel.uiState.first().isDarkTheme)

        viewModel.onThemeToggle(false)
        assertFalse("Dark theme should be disabled", viewModel.uiState.first().isDarkTheme)
    }

    @Test
    fun `onHapticsToggle updates isHapticsEnabled state`() = runTest {
        val viewModel = SettingsViewModel()

        viewModel.onHapticsToggle(false)
        assertFalse("Haptics should be disabled", viewModel.uiState.first().isHapticsEnabled)

        viewModel.onHapticsToggle(true)
        assertTrue("Haptics should be enabled", viewModel.uiState.first().isHapticsEnabled)
    }
}
