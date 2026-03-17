package com.rulebook.feature.settings

import com.rulebook.core.datastore.CreditPreferencesSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Fake implementation of CreditPreferencesSource for testing.
 */
class FakeCreditPreferencesSource(
    initialBalance: Int = 0
) : CreditPreferencesSource {
    private val _creditBalance = MutableStateFlow(initialBalance)
    override val creditBalance: Flow<Int> = _creditBalance

    fun setCreditBalance(balance: Int) {
        _creditBalance.value = balance
    }

    override suspend fun awardInitialCreditsIfNeeded(amount: Int): Boolean = false
    override suspend fun deductCredit(): Boolean = false
    override suspend fun addCredits(amount: Int): Boolean = false
}

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has expected default values`() = runTest(testDispatcher) {
        val fakeCreditPreferences = FakeCreditPreferencesSource(initialBalance = 5)
        val viewModel = SettingsViewModel(fakeCreditPreferences)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Credit balance should be 5", 5, state.creditBalance)
        assertFalse("Dark theme should be false by default", state.isDarkTheme)
        assertTrue("Haptics should be enabled by default", state.isHapticsEnabled)
        assertEquals("Version should be 1.0.0", "1.0.0", state.appVersion)
    }

    @Test
    fun `uiState exposes immutable state flow`() = runTest(testDispatcher) {
        val fakeCreditPreferences = FakeCreditPreferencesSource()
        val viewModel = SettingsViewModel(fakeCreditPreferences)
        advanceUntilIdle()

        val state1 = viewModel.uiState.value
        val state2 = viewModel.uiState.value
        assertEquals("States should be equal", state1, state2)
    }

    @Test
    fun `credit balance updates when preference changes`() = runTest(testDispatcher) {
        val fakeCreditPreferences = FakeCreditPreferencesSource(initialBalance = 5)
        val viewModel = SettingsViewModel(fakeCreditPreferences)
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals("Initial balance should be 5", 5, state.creditBalance)

        fakeCreditPreferences.setCreditBalance(10)
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals("Balance should update to 10", 10, state.creditBalance)
    }

    @Test
    fun `onThemeToggle updates isDarkTheme state`() = runTest(testDispatcher) {
        val fakeCreditPreferences = FakeCreditPreferencesSource()
        val viewModel = SettingsViewModel(fakeCreditPreferences)
        advanceUntilIdle()

        viewModel.onThemeToggle(true)
        assertTrue("Dark theme should be enabled", viewModel.uiState.value.isDarkTheme)

        viewModel.onThemeToggle(false)
        assertFalse("Dark theme should be disabled", viewModel.uiState.value.isDarkTheme)
    }

    @Test
    fun `onHapticsToggle updates isHapticsEnabled state`() = runTest(testDispatcher) {
        val fakeCreditPreferences = FakeCreditPreferencesSource()
        val viewModel = SettingsViewModel(fakeCreditPreferences)
        advanceUntilIdle()

        viewModel.onHapticsToggle(false)
        assertFalse("Haptics should be disabled", viewModel.uiState.value.isHapticsEnabled)

        viewModel.onHapticsToggle(true)
        assertTrue("Haptics should be enabled", viewModel.uiState.value.isHapticsEnabled)
    }
}
