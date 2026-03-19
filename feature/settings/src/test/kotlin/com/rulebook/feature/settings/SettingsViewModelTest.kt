package com.rulebook.feature.settings

import com.rulebook.core.datastore.CreditPreferencesSource
import com.rulebook.core.datastore.HapticsPreferencesSource
import com.rulebook.core.datastore.ResettablePreferences
import com.rulebook.core.datastore.ThemeMode
import com.rulebook.core.datastore.ThemePreferencesSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
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

/**
 * Fake implementation of ThemePreferencesSource for testing.
 */
class FakeThemePreferencesSource(
    initialMode: ThemeMode = ThemeMode.SYSTEM
) : ThemePreferencesSource {
    private val _themeMode = MutableStateFlow(initialMode)
    override val themeMode: Flow<ThemeMode> = _themeMode

    override suspend fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun getCurrentThemeMode(): ThemeMode = _themeMode.value
}

/**
 * Fake implementation of HapticsPreferencesSource for testing.
 */
class FakeHapticsPreferencesSource(
    initialEnabled: Boolean = true
) : HapticsPreferencesSource {
    private val _hapticsEnabled = MutableStateFlow(initialEnabled)
    override val hapticsEnabled: Flow<Boolean> = _hapticsEnabled

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
    }

    fun getCurrentHapticsEnabled(): Boolean = _hapticsEnabled.value
}

/**
 * Fake implementation of ResettablePreferences for testing.
 */
class FakeResettablePreferences(
    private val shouldThrow: Boolean = false
) : ResettablePreferences {
    var resetCalled = false

    override suspend fun reset() {
        if (shouldThrow) throw RuntimeException("Preferences error")
        resetCalled = true
    }
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

    private fun createViewModel(
        creditPreferences: FakeCreditPreferencesSource = FakeCreditPreferencesSource(),
        themePreferences: FakeThemePreferencesSource = FakeThemePreferencesSource(),
        hapticsPreferences: FakeHapticsPreferencesSource = FakeHapticsPreferencesSource(),
        clearDatabase: suspend () -> Unit = {},
        resettablePreferences: FakeResettablePreferences = FakeResettablePreferences()
    ) = SettingsViewModel(creditPreferences, themePreferences, hapticsPreferences, clearDatabase, resettablePreferences)

    @Test
    fun `initial state has expected default values`() = runTest(testDispatcher) {
        val fakeCreditPreferences = FakeCreditPreferencesSource(initialBalance = 5)
        val fakeThemePreferences = FakeThemePreferencesSource(initialMode = ThemeMode.SYSTEM)
        val fakeHapticsPreferences = FakeHapticsPreferencesSource(initialEnabled = true)
        val viewModel = SettingsViewModel(fakeCreditPreferences, fakeThemePreferences, fakeHapticsPreferences, {}, FakeResettablePreferences())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Credit balance should be 5", 5, state.creditBalance)
        assertEquals("Theme mode should be SYSTEM by default", ThemeMode.SYSTEM, state.themeMode)
        assertTrue("Haptics should be enabled by default", state.isHapticsEnabled)
        assertEquals("Version should be 1.0.0", "1.0.0", state.appVersion)
        assertFalse("showClearConfirmation should be false by default", state.showClearConfirmation)
    }

    @Test
    fun `uiState exposes immutable state flow`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state1 = viewModel.uiState.value
        val state2 = viewModel.uiState.value
        assertEquals("States should be equal", state1, state2)
    }

    @Test
    fun `credit balance updates when preference changes`() = runTest(testDispatcher) {
        val fakeCreditPreferences = FakeCreditPreferencesSource(initialBalance = 5)
        val viewModel = createViewModel(creditPreferences = fakeCreditPreferences)
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals("Initial balance should be 5", 5, state.creditBalance)

        fakeCreditPreferences.setCreditBalance(10)
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals("Balance should update to 10", 10, state.creditBalance)
    }

    @Test
    fun `onThemeSelected updates themeMode state for all three modes`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onThemeSelected(ThemeMode.LIGHT)
        advanceUntilIdle()
        assertEquals("Theme should be LIGHT", ThemeMode.LIGHT, viewModel.uiState.value.themeMode)

        viewModel.onThemeSelected(ThemeMode.DARK)
        advanceUntilIdle()
        assertEquals("Theme should be DARK", ThemeMode.DARK, viewModel.uiState.value.themeMode)

        viewModel.onThemeSelected(ThemeMode.SYSTEM)
        advanceUntilIdle()
        assertEquals("Theme should be SYSTEM", ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `theme mode updates when preference changes externally`() = runTest(testDispatcher) {
        val fakeThemePreferences = FakeThemePreferencesSource(initialMode = ThemeMode.SYSTEM)
        val viewModel = createViewModel(themePreferences = fakeThemePreferences)
        advanceUntilIdle()

        assertEquals("Initial theme should be SYSTEM", ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)

        fakeThemePreferences.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals("Theme should update to DARK", ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `onThemeSelected persists to preferences source`() = runTest(testDispatcher) {
        val fakeThemePreferences = FakeThemePreferencesSource()
        val viewModel = createViewModel(themePreferences = fakeThemePreferences)
        advanceUntilIdle()

        viewModel.onThemeSelected(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals("Preference source should have DARK mode", ThemeMode.DARK, fakeThemePreferences.getCurrentThemeMode())
    }

    @Test
    fun `onHapticsToggle updates isHapticsEnabled state`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onHapticsToggle(false)
        assertFalse("Haptics should be disabled", viewModel.uiState.value.isHapticsEnabled)

        viewModel.onHapticsToggle(true)
        assertTrue("Haptics should be enabled", viewModel.uiState.value.isHapticsEnabled)
    }

    @Test
    fun `onHapticsToggle persists to preferences source`() = runTest(testDispatcher) {
        val fakeHapticsPreferences = FakeHapticsPreferencesSource(initialEnabled = true)
        val viewModel = createViewModel(hapticsPreferences = fakeHapticsPreferences)
        advanceUntilIdle()

        viewModel.onHapticsToggle(false)
        advanceUntilIdle()

        assertFalse("Preference source should have haptics disabled", fakeHapticsPreferences.getCurrentHapticsEnabled())

        viewModel.onHapticsToggle(true)
        advanceUntilIdle()

        assertTrue("Preference source should have haptics enabled", fakeHapticsPreferences.getCurrentHapticsEnabled())
    }

    @Test
    fun `haptics state updates when preference changes externally`() = runTest(testDispatcher) {
        val fakeHapticsPreferences = FakeHapticsPreferencesSource(initialEnabled = true)
        val viewModel = createViewModel(hapticsPreferences = fakeHapticsPreferences)
        advanceUntilIdle()

        assertTrue("Initial haptics should be enabled", viewModel.uiState.value.isHapticsEnabled)

        fakeHapticsPreferences.setHapticsEnabled(false)
        advanceUntilIdle()

        assertFalse("Haptics should update to disabled", viewModel.uiState.value.isHapticsEnabled)
    }

    @Test
    fun `initial haptics state reflects preference source`() = runTest(testDispatcher) {
        val fakeHapticsPreferences = FakeHapticsPreferencesSource(initialEnabled = false)
        val viewModel = createViewModel(hapticsPreferences = fakeHapticsPreferences)
        advanceUntilIdle()

        assertFalse("Initial haptics should be false per preference source", viewModel.uiState.value.isHapticsEnabled)
    }

    @Test
    fun `onContactUs emits ContactSupport event`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onContactUs()
        advanceUntilIdle()

        val event = withTimeout(1000) { viewModel.events.first() }
        assertEquals("onContactUs should emit ContactSupport", SettingsEvent.ContactSupport, event)
    }

    @Test
    fun `onReportBug emits ReportBug event`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onReportBug()
        advanceUntilIdle()

        val event = withTimeout(1000) { viewModel.events.first() }
        assertEquals("onReportBug should emit ReportBug", SettingsEvent.ReportBug, event)
    }

    @Test
    fun `onRateApp emits RateApp event`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRateApp()
        advanceUntilIdle()

        val event = withTimeout(1000) { viewModel.events.first() }
        assertEquals("onRateApp should emit RateApp", SettingsEvent.RateApp, event)
    }

    @Test
    fun `onPrivacyPolicy emits OpenPrivacyPolicy event`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPrivacyPolicy()
        advanceUntilIdle()

        val event = withTimeout(1000) { viewModel.events.first() }
        assertEquals("onPrivacyPolicy should emit OpenPrivacyPolicy", SettingsEvent.OpenPrivacyPolicy, event)
    }

    @Test
    fun `onTermsOfService emits OpenTermsOfService event`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onTermsOfService()
        advanceUntilIdle()

        val event = withTimeout(1000) { viewModel.events.first() }
        assertEquals("onTermsOfService should emit OpenTermsOfService", SettingsEvent.OpenTermsOfService, event)
    }

    @Test
    fun `updateVersionInfo updates both appVersion and appVersionCode`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateVersionInfo("2.5.1", "42")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("appVersion should be updated", "2.5.1", state.appVersion)
        assertEquals("appVersionCode should be updated", "42", state.appVersionCode)
    }

    // =========================================================================
    // Clear Data Tests
    // =========================================================================

    @Test
    fun `onShowClearConfirmation sets showClearConfirmation to true`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse("showClearConfirmation should start false", viewModel.uiState.value.showClearConfirmation)

        viewModel.onShowClearConfirmation()
        advanceUntilIdle()

        assertTrue("showClearConfirmation should be true", viewModel.uiState.value.showClearConfirmation)
    }

    @Test
    fun `onDismissClearConfirmation sets showClearConfirmation to false`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowClearConfirmation()
        advanceUntilIdle()
        assertTrue("showClearConfirmation should be true after show", viewModel.uiState.value.showClearConfirmation)

        viewModel.onDismissClearConfirmation()
        advanceUntilIdle()

        assertFalse("showClearConfirmation should be false after dismiss", viewModel.uiState.value.showClearConfirmation)
    }

    @Test
    fun `onClearData dismisses confirmation dialog immediately`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowClearConfirmation()
        advanceUntilIdle()
        assertTrue("Dialog should be visible", viewModel.uiState.value.showClearConfirmation)

        viewModel.onClearData()
        advanceUntilIdle()

        assertFalse("Dialog should be dismissed after clear", viewModel.uiState.value.showClearConfirmation)
    }

    @Test
    fun `onClearData calls the clearDatabase function`() = runTest(testDispatcher) {
        var clearDatabaseCalled = false
        val viewModel = createViewModel(clearDatabase = { clearDatabaseCalled = true })
        advanceUntilIdle()

        viewModel.onClearData()
        advanceUntilIdle()

        assertTrue("clearDatabase should have been called", clearDatabaseCalled)
    }

    @Test
    fun `onClearData calls reset on preferences`() = runTest(testDispatcher) {
        val fakePreferences = FakeResettablePreferences()
        val viewModel = createViewModel(resettablePreferences = fakePreferences)
        advanceUntilIdle()

        viewModel.onClearData()
        advanceUntilIdle()

        assertTrue("reset should have been called", fakePreferences.resetCalled)
    }

    @Test
    fun `onClearData emits ShowSnackbar then NavigateToOnboarding on success`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val events = mutableListOf<SettingsEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onClearData()
        advanceUntilIdle()

        val snackbarEvent = events.filterIsInstance<SettingsEvent.ShowSnackbar>().firstOrNull()
        val navigateEvent = events.filterIsInstance<SettingsEvent.NavigateToOnboarding>().firstOrNull()

        assertTrue("ShowSnackbar event should be emitted", snackbarEvent != null)
        assertEquals("Snackbar message should be 'All data cleared'", "All data cleared", snackbarEvent?.message)
        assertTrue("NavigateToOnboarding event should be emitted", navigateEvent != null)

        // Verify ordering: snackbar before navigation
        val snackbarIndex = events.indexOfFirst { it is SettingsEvent.ShowSnackbar }
        val navigateIndex = events.indexOfFirst { it is SettingsEvent.NavigateToOnboarding }
        assertTrue("ShowSnackbar should come before NavigateToOnboarding", snackbarIndex < navigateIndex)

        job.cancel()
    }

    @Test
    fun `onClearData emits error snackbar when clearDatabase throws`() = runTest(testDispatcher) {
        val viewModel = createViewModel(clearDatabase = { throw RuntimeException("Database error") })
        advanceUntilIdle()

        val events = mutableListOf<SettingsEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onClearData()
        advanceUntilIdle()

        val snackbarEvent = events.filterIsInstance<SettingsEvent.ShowSnackbar>().firstOrNull()
        assertTrue("Error snackbar should be emitted on failure", snackbarEvent != null)
        assertEquals("Error message should indicate failure", "Failed to clear data", snackbarEvent?.message)
        assertFalse("NavigateToOnboarding should NOT be emitted on error",
            events.any { it is SettingsEvent.NavigateToOnboarding })

        job.cancel()
    }

    @Test
    fun `onClearData does not call preferences reset when clearDatabase throws`() = runTest(testDispatcher) {
        val fakePreferences = FakeResettablePreferences()
        val viewModel = createViewModel(
            clearDatabase = { throw RuntimeException("Database error") },
            resettablePreferences = fakePreferences
        )
        advanceUntilIdle()

        viewModel.onClearData()
        advanceUntilIdle()

        assertFalse("Preferences reset should not be called when database fails", fakePreferences.resetCalled)
    }
}
