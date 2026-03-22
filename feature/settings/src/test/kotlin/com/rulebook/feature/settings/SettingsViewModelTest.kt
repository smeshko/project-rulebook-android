package com.rulebook.feature.settings

import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.datastore.CreditPreferencesSource
import com.rulebook.core.datastore.HapticsPreferencesSource
import com.rulebook.core.datastore.ResettablePreferences
import com.rulebook.core.datastore.ThemeMode
import com.rulebook.core.datastore.ThemePreferencesSource
import com.rulebook.core.model.Game
import com.rulebook.core.model.Rules
import com.rulebook.core.model.SortOrder
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

    fun setCreditBalanceSync(balance: Int) {
        _creditBalance.value = balance
    }

    override suspend fun awardInitialCreditsIfNeeded(amount: Int): Boolean = false
    override suspend fun deductCredit(): Boolean = false
    override suspend fun addCredits(amount: Int): Boolean = false
    override suspend fun removeCredits(amount: Int): Int = 0
    override suspend fun setCreditBalance(balance: Int) { _creditBalance.value = balance.coerceAtLeast(0) }
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

/**
 * Fake implementation of AnalyticsManager for testing.
 * Records all tracked events for verification in tests.
 */
class FakeAnalyticsManager(
    private val shouldThrow: Boolean = false
) : AnalyticsManager {
    data class TrackedEvent(val name: String, val properties: Map<String, String>)

    private val _trackedEvents = mutableListOf<TrackedEvent>()
    val trackedEvents: List<TrackedEvent> get() = _trackedEvents.toList()

    override fun trackEvent(name: String, properties: Map<String, String>) {
        if (shouldThrow) throw RuntimeException("Analytics error")
        _trackedEvents.add(TrackedEvent(name, properties))
    }

    override fun trackScreenView(screenName: String) {}
}

/**
 * Fake implementation of GameRepository for testing.
 */
class FakeGameRepository(
    private val games: List<Game> = emptyList(),
    private val shouldFailGetGames: Boolean = false
) : GameRepository {
    override suspend fun getGames(): Result<List<Game>> =
        if (shouldFailGetGames) Result.Error("error") else Result.Success(games)

    override suspend fun getGameById(id: String): Result<Game> = Result.Error("not implemented")
    override suspend fun saveGame(game: Game): Result<Unit> = Result.Error("not implemented")
    override suspend fun deleteGame(id: String): Result<Unit> = Result.Error("not implemented")
    override suspend fun getRulesForGame(gameId: String): Result<Rules> = Result.Error("not implemented")
    override suspend fun saveGameWithRules(game: Game, rules: Rules, rawJson: String): Result<String> = Result.Error("not implemented")
    override suspend fun updateLastAccessed(gameId: String): Result<Unit> = Result.Error("not implemented")
    override fun getGamesSorted(sortOrder: SortOrder): Flow<List<Game>> = MutableStateFlow(emptyList())
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
        resettablePreferences: FakeResettablePreferences = FakeResettablePreferences(),
        gameRepository: FakeGameRepository = FakeGameRepository(),
        analyticsManager: FakeAnalyticsManager = FakeAnalyticsManager()
    ) = SettingsViewModel(
        creditPreferences,
        themePreferences,
        hapticsPreferences,
        clearDatabase,
        resettablePreferences,
        gameRepository,
        analyticsManager
    )

    @Test
    fun `initial state has expected default values`() = runTest(testDispatcher) {
        val fakeCreditPreferences = FakeCreditPreferencesSource(initialBalance = 5)
        val fakeThemePreferences = FakeThemePreferencesSource(initialMode = ThemeMode.SYSTEM)
        val fakeHapticsPreferences = FakeHapticsPreferencesSource(initialEnabled = true)
        val viewModel = SettingsViewModel(fakeCreditPreferences, fakeThemePreferences, fakeHapticsPreferences, {}, FakeResettablePreferences(), FakeGameRepository(), FakeAnalyticsManager())
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

        fakeCreditPreferences.setCreditBalanceSync(10)
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

    @Test
    fun `onClearData still navigates to onboarding when preferences reset throws`() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            resettablePreferences = FakeResettablePreferences(shouldThrow = true)
        )
        advanceUntilIdle()

        val events = mutableListOf<SettingsEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onClearData()
        advanceUntilIdle()

        val snackbarEvent = events.filterIsInstance<SettingsEvent.ShowSnackbar>().firstOrNull()
        assertTrue("ShowSnackbar event should be emitted", snackbarEvent != null)
        assertEquals("Snackbar message should be 'All data cleared'", "All data cleared", snackbarEvent?.message)
        assertTrue("NavigateToOnboarding should still be emitted",
            events.any { it is SettingsEvent.NavigateToOnboarding })

        job.cancel()
    }

    // =========================================================================
    // Analytics Tests
    // =========================================================================

    @Test
    fun `onThemeSelected tracks settings_theme_changed with correct theme and previous_theme`() = runTest(testDispatcher) {
        val fakeAnalytics = FakeAnalyticsManager()
        val fakeTheme = FakeThemePreferencesSource(initialMode = ThemeMode.SYSTEM)
        val viewModel = createViewModel(themePreferences = fakeTheme, analyticsManager = fakeAnalytics)
        advanceUntilIdle()

        viewModel.onThemeSelected(ThemeMode.DARK)
        advanceUntilIdle()

        val event = fakeAnalytics.trackedEvents.firstOrNull { it.name == "settings_theme_changed" }
        assertTrue("settings_theme_changed should be tracked", event != null)
        assertEquals("theme property should be 'dark'", "dark", event?.properties?.get("theme"))
        assertEquals("previous_theme property should be 'system'", "system", event?.properties?.get("previous_theme"))
    }

    @Test
    fun `onHapticsToggle tracks settings_haptics_changed with enabled value`() = runTest(testDispatcher) {
        val fakeAnalytics = FakeAnalyticsManager()
        val viewModel = createViewModel(analyticsManager = fakeAnalytics)
        advanceUntilIdle()

        viewModel.onHapticsToggle(false)
        advanceUntilIdle()

        val event = fakeAnalytics.trackedEvents.firstOrNull { it.name == "settings_haptics_changed" }
        assertTrue("settings_haptics_changed should be tracked", event != null)
        assertEquals("enabled property should be 'false'", "false", event?.properties?.get("enabled"))
    }

    @Test
    fun `onHapticsToggle tracks enabled true when toggled on`() = runTest(testDispatcher) {
        val fakeAnalytics = FakeAnalyticsManager()
        val fakeHaptics = FakeHapticsPreferencesSource(initialEnabled = false)
        val viewModel = createViewModel(hapticsPreferences = fakeHaptics, analyticsManager = fakeAnalytics)
        advanceUntilIdle()

        viewModel.onHapticsToggle(true)
        advanceUntilIdle()

        val event = fakeAnalytics.trackedEvents.firstOrNull { it.name == "settings_haptics_changed" }
        assertTrue("settings_haptics_changed should be tracked", event != null)
        assertEquals("enabled property should be 'true'", "true", event?.properties?.get("enabled"))
    }

    @Test
    fun `onClearData tracks settings_data_cleared with games_count`() = runTest(testDispatcher) {
        val fakeAnalytics = FakeAnalyticsManager()
        val fakeGames = listOf(
            Game(id = "1", title = "Chess", thumbnailUrl = null, createdAt = 0L, lastAccessedAt = 0L),
            Game(id = "2", title = "Checkers", thumbnailUrl = null, createdAt = 0L, lastAccessedAt = 0L),
            Game(id = "3", title = "Go", thumbnailUrl = null, createdAt = 0L, lastAccessedAt = 0L)
        )
        val viewModel = createViewModel(
            gameRepository = FakeGameRepository(games = fakeGames),
            analyticsManager = fakeAnalytics
        )
        advanceUntilIdle()

        viewModel.onClearData()
        advanceUntilIdle()

        val event = fakeAnalytics.trackedEvents.firstOrNull { it.name == "settings_data_cleared" }
        assertTrue("settings_data_cleared should be tracked", event != null)
        assertEquals("games_count property should be '3'", "3", event?.properties?.get("games_count"))
    }

    @Test
    fun `onClearData tracks games_count as 0 when gameRepository fails`() = runTest(testDispatcher) {
        val fakeAnalytics = FakeAnalyticsManager()
        val viewModel = createViewModel(
            gameRepository = FakeGameRepository(shouldFailGetGames = true),
            analyticsManager = fakeAnalytics
        )
        advanceUntilIdle()

        viewModel.onClearData()
        advanceUntilIdle()

        val event = fakeAnalytics.trackedEvents.firstOrNull { it.name == "settings_data_cleared" }
        assertTrue("settings_data_cleared should be tracked", event != null)
        assertEquals("games_count should default to '0' on error", "0", event?.properties?.get("games_count"))
    }

    @Test
    fun `onClearData does not track analytics when clearDatabase throws`() = runTest(testDispatcher) {
        val fakeAnalytics = FakeAnalyticsManager()
        val viewModel = createViewModel(
            clearDatabase = { throw RuntimeException("Database error") },
            analyticsManager = fakeAnalytics
        )
        advanceUntilIdle()

        viewModel.onClearData()
        advanceUntilIdle()

        assertFalse(
            "settings_data_cleared should NOT be tracked on clearDatabase failure",
            fakeAnalytics.trackedEvents.any { it.name == "settings_data_cleared" }
        )
    }

    @Test
    fun `onContactUs tracks settings_support_tapped with link contact`() = runTest(testDispatcher) {
        val fakeAnalytics = FakeAnalyticsManager()
        val viewModel = createViewModel(analyticsManager = fakeAnalytics)
        advanceUntilIdle()

        viewModel.onContactUs()
        advanceUntilIdle()

        val event = fakeAnalytics.trackedEvents.firstOrNull { it.name == "settings_support_tapped" }
        assertTrue("settings_support_tapped should be tracked", event != null)
        assertEquals("link property should be 'contact'", "contact", event?.properties?.get("link"))
    }

    @Test
    fun `onReportBug tracks settings_support_tapped with link bug`() = runTest(testDispatcher) {
        val fakeAnalytics = FakeAnalyticsManager()
        val viewModel = createViewModel(analyticsManager = fakeAnalytics)
        advanceUntilIdle()

        viewModel.onReportBug()
        advanceUntilIdle()

        val event = fakeAnalytics.trackedEvents.firstOrNull { it.name == "settings_support_tapped" }
        assertTrue("settings_support_tapped should be tracked", event != null)
        assertEquals("link property should be 'bug'", "bug", event?.properties?.get("link"))
    }

    @Test
    fun `onRateApp tracks settings_support_tapped with link rate`() = runTest(testDispatcher) {
        val fakeAnalytics = FakeAnalyticsManager()
        val viewModel = createViewModel(analyticsManager = fakeAnalytics)
        advanceUntilIdle()

        viewModel.onRateApp()
        advanceUntilIdle()

        val event = fakeAnalytics.trackedEvents.firstOrNull { it.name == "settings_support_tapped" }
        assertTrue("settings_support_tapped should be tracked", event != null)
        assertEquals("link property should be 'rate'", "rate", event?.properties?.get("link"))
    }

    @Test
    fun `onPrivacyPolicy tracks settings_support_tapped with link privacy`() = runTest(testDispatcher) {
        val fakeAnalytics = FakeAnalyticsManager()
        val viewModel = createViewModel(analyticsManager = fakeAnalytics)
        advanceUntilIdle()

        viewModel.onPrivacyPolicy()
        advanceUntilIdle()

        val event = fakeAnalytics.trackedEvents.firstOrNull { it.name == "settings_support_tapped" }
        assertTrue("settings_support_tapped should be tracked", event != null)
        assertEquals("link property should be 'privacy'", "privacy", event?.properties?.get("link"))
    }

    @Test
    fun `onTermsOfService tracks settings_support_tapped with link terms`() = runTest(testDispatcher) {
        val fakeAnalytics = FakeAnalyticsManager()
        val viewModel = createViewModel(analyticsManager = fakeAnalytics)
        advanceUntilIdle()

        viewModel.onTermsOfService()
        advanceUntilIdle()

        val event = fakeAnalytics.trackedEvents.firstOrNull { it.name == "settings_support_tapped" }
        assertTrue("settings_support_tapped should be tracked", event != null)
        assertEquals("link property should be 'terms'", "terms", event?.properties?.get("link"))
    }

    // =========================================================================
    // Analytics Failure Resilience Tests
    // =========================================================================

    @Test
    fun `analytics failure does not block theme change`() = runTest(testDispatcher) {
        val throwingAnalytics = FakeAnalyticsManager(shouldThrow = true)
        val viewModel = createViewModel(analyticsManager = throwingAnalytics)
        advanceUntilIdle()

        viewModel.onThemeSelected(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals("Theme should still update to DARK", ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `analytics failure does not block haptics toggle`() = runTest(testDispatcher) {
        val throwingAnalytics = FakeAnalyticsManager(shouldThrow = true)
        val viewModel = createViewModel(analyticsManager = throwingAnalytics)
        advanceUntilIdle()

        viewModel.onHapticsToggle(false)
        advanceUntilIdle()

        assertFalse("Haptics should still be disabled", viewModel.uiState.value.isHapticsEnabled)
    }

    @Test
    fun `analytics failure does not block support events`() = runTest(testDispatcher) {
        val throwingAnalytics = FakeAnalyticsManager(shouldThrow = true)
        val viewModel = createViewModel(analyticsManager = throwingAnalytics)
        advanceUntilIdle()

        viewModel.onContactUs()
        advanceUntilIdle()

        val event = withTimeout(1000) { viewModel.events.first() }
        assertEquals("ContactSupport event should still be emitted", SettingsEvent.ContactSupport, event)
    }

    @Test
    fun `analytics failure does not block clear data flow`() = runTest(testDispatcher) {
        val throwingAnalytics = FakeAnalyticsManager(shouldThrow = true)
        val viewModel = createViewModel(analyticsManager = throwingAnalytics)
        advanceUntilIdle()

        val events = mutableListOf<SettingsEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onClearData()
        advanceUntilIdle()

        assertTrue("ShowSnackbar should still be emitted",
            events.any { it is SettingsEvent.ShowSnackbar })
        assertTrue("NavigateToOnboarding should still be emitted",
            events.any { it is SettingsEvent.NavigateToOnboarding })

        job.cancel()
    }
}
