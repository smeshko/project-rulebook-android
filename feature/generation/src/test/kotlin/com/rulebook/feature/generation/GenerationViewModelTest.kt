package com.rulebook.feature.generation

import androidx.lifecycle.SavedStateHandle
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.ScanRepository
import com.rulebook.core.model.RuleSection
import com.rulebook.core.model.Rules
import com.rulebook.core.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GenerationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeAnalyticsManager: FakeAnalyticsManager
    private lateinit var fakeScanRepository: FakeScanRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAnalyticsManager = FakeAnalyticsManager()
        fakeScanRepository = FakeScanRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(imageUri: String = "file:///test/image.jpg"): GenerationViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to imageUri))
        return GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager,
            scanRepository = fakeScanRepository,
            gameRepository = FakeGameRepository(),
            creditRepository = FakeCreditRepository(),
        )
    }

    // =========================================================================
    // Initial State Tests
    // =========================================================================

    @Test
    fun `successful analysis with high confidence auto-proceeds to SAVING_RULES phase`() = runTest {
        // Default fake returns 0.95 confidence (>= 0.80 threshold) → auto-proceed → generate rules → SAVING_RULES
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.SAVING_RULES, state.currentPhase)
    }

    @Test
    fun `initial state extracts imageUri from SavedStateHandle`() = runTest {
        val viewModel = createViewModel(imageUri = "content://test/photo.jpg")
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals("content://test/photo.jpg", state.imageUri)
    }

    @Test
    fun `successful analysis with high confidence completes with 100 percent progress`() = runTest {
        // Default fake returns 0.95 confidence → auto-proceed → generate rules → save → complete (1.0)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(1.0f, state.overallProgress)
    }

    @Test
    fun `initial state is not cancelling`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.isCancelling)
    }

    @Test
    fun `initial state has showError set to false`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.showError)
    }

    // =========================================================================
    // Story 5.9: Error Screen Tests
    // =========================================================================

    @Test
    fun `non-retryable error shows error screen instead of navigating back`() = runTest {
        fakeScanRepository.shouldReturnNoInternetError = true
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertTrue(state.showError)
        assertNotNull(state.error)
        assertEquals("no_internet", state.errorType)
    }

    @Test
    fun `onRetry clears error state and emits RetryFromCamera event`() = runTest {
        fakeScanRepository.shouldReturnNoInternetError = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Collect events
        val events = mutableListOf<GenerationEvent>()
        val eventJob = launch {
            viewModel.events.toList(events)
        }

        viewModel.onRetry()
        advanceUntilIdle()
        eventJob.cancel()

        assertFalse(viewModel.uiState.first().showError)
        assertNull(viewModel.uiState.first().errorType)
        assertTrue(events.any { it is GenerationEvent.RetryFromCamera })

        // Verify analytics tracks the correct error type (not "unknown")
        val retryEvent = fakeAnalyticsManager.trackedEvents.firstOrNull { it.name == "scan_retry_from_error" }
        assertNotNull(retryEvent)
        assertEquals("no_internet", retryEvent.properties["error_type"])
    }

    @Test
    fun `onErrorManualEntry clears error state and shows manual entry`() = runTest {
        fakeScanRepository.shouldReturnNoInternetError = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onErrorManualEntry()
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertFalse(state.showError)
        assertNull(state.errorType)
        assertTrue(state.showManualEntry)

        // Verify analytics tracks the correct error type (not "unknown")
        val manualEntryEvent = fakeAnalyticsManager.trackedEvents.firstOrNull { it.name == "scan_manual_entry_from_error" }
        assertNotNull(manualEntryEvent)
        assertEquals("no_internet", manualEntryEvent.properties["error_type"])
    }

    @Test
    fun `rules generation error shows error screen in Story 5_9`() = runTest {
        fakeScanRepository.shouldFailRulesGeneration = true
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertTrue(state.showError)
        assertNotNull(state.error)
    }

    @Test
    fun `save error shows error screen in Story 5_9`() = runTest {
        val fakeGameRepo = FakeGameRepository()
        fakeGameRepo.shouldFailSave = true
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to "file:///test/image.jpg"))
        val viewModel = GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager,
            scanRepository = fakeScanRepository,
            gameRepository = fakeGameRepo,
            creditRepository = FakeCreditRepository(),
        )
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertTrue(state.showError)
        assertNotNull(state.error)
    }

    @Test
    fun `generic exception shows error screen with friendly message`() = runTest {
        fakeScanRepository.shouldThrowGenericException = true
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertTrue(state.showError)
        assertEquals("Something went wrong. Please try again.", state.error)
    }

    @Test
    fun `successful analysis has no error`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertNull(state.error)
    }

    @Test
    fun `tracks scan_started analytics event on init with valid imageUri`() = runTest {
        createViewModel()
        advanceUntilIdle()

        val events = fakeAnalyticsManager.trackedEvents
        assertTrue(events.any { it.name == "scan_started" })
    }

    @Test
    fun `does not start generation with blank imageUri`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to ""))
        GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager,
            scanRepository = fakeScanRepository,
            gameRepository = FakeGameRepository(),
            creditRepository = FakeCreditRepository(),
        )
        advanceUntilIdle()

        assertTrue(fakeAnalyticsManager.trackedEvents.none { it.name == "scan_started" })
    }

    @Test
    fun `blank imageUri sets error state`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to ""))
        val viewModel = GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager,
            scanRepository = fakeScanRepository,
            gameRepository = FakeGameRepository(),
            creditRepository = FakeCreditRepository(),
        )
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals("No image provided.", state.error)
    }

    @Test
    fun `blank imageUri emits Error event`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to ""))
        val events = mutableListOf<GenerationEvent>()

        val viewModel = GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager,
            scanRepository = fakeScanRepository,
            gameRepository = FakeGameRepository(),
            creditRepository = FakeCreditRepository(),
        )
        val job = launch { viewModel.events.toList(events) }
        advanceUntilIdle()

        assertEquals(1, events.size)
        assertTrue(events[0] is GenerationEvent.Error)

        job.cancel()
    }

    @Test
    fun `does not start generation with missing imageUri`() = runTest {
        val savedStateHandle = SavedStateHandle()
        GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager,
            scanRepository = fakeScanRepository,
            gameRepository = FakeGameRepository(),
            creditRepository = FakeCreditRepository(),
        )
        advanceUntilIdle()

        assertTrue(fakeAnalyticsManager.trackedEvents.none { it.name == "scan_started" })
    }

    @Test
    fun `missing imageUri sets error state`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager,
            scanRepository = fakeScanRepository,
            gameRepository = FakeGameRepository(),
            creditRepository = FakeCreditRepository(),
        )
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals("No image provided.", state.error)
    }

    // =========================================================================
    // Phase Update Tests
    // =========================================================================

    @Test
    fun `updatePhase changes current phase`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updatePhase(ScanPhase.ANALYZING_IMAGE)
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.ANALYZING_IMAGE, state.currentPhase)
    }

    @Test
    fun `updatePhase sets progress to phase start`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updatePhase(ScanPhase.IDENTIFYING_GAME)
        val state = viewModel.uiState.first()

        assertEquals(0.40f, state.overallProgress)
    }

    @Test
    fun `updatePhase to GENERATING_RULES sets progress to 0_60`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updatePhase(ScanPhase.GENERATING_RULES)
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.GENERATING_RULES, state.currentPhase)
        assertEquals(0.60f, state.overallProgress)
    }

    @Test
    fun `updatePhase to SAVING_RULES sets progress to 0_90`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updatePhase(ScanPhase.SAVING_RULES)
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.SAVING_RULES, state.currentPhase)
        assertEquals(0.90f, state.overallProgress)
    }

    // =========================================================================
    // Progress Update Tests
    // =========================================================================

    @Test
    fun `updateProgress updates overall progress`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // After successful analysis with high confidence, phase is SAVING_RULES (90-100%)
        viewModel.updateProgress(0.95f)
        val state = viewModel.uiState.first()

        assertEquals(0.95f, state.overallProgress)
    }

    @Test
    fun `updateProgress clamps to current phase max`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updatePhase(ScanPhase.PROCESSING_IMAGE) // 0-15%
        viewModel.updateProgress(0.50f) // Try to go beyond 15%
        val state = viewModel.uiState.first()

        assertEquals(0.15f, state.overallProgress) // Clamped to 15%
    }

    @Test
    fun `updateProgress clamps to current phase min`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updatePhase(ScanPhase.ANALYZING_IMAGE) // 15-40%
        viewModel.updateProgress(0.05f) // Try to go below 15%
        val state = viewModel.uiState.first()

        assertEquals(0.15f, state.overallProgress) // Clamped to 15%
    }

    @Test
    fun `updateProgress within phase range works correctly`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updatePhase(ScanPhase.GENERATING_RULES) // 60-90%
        viewModel.updateProgress(0.75f)
        val state = viewModel.uiState.first()

        assertEquals(0.75f, state.overallProgress)
    }

    // =========================================================================
    // Cancel Tests
    // =========================================================================

    @Test
    fun `cancel sets isCancelling to true`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.cancel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertTrue(state.isCancelling)
    }

    @Test
    fun `cancel emits Cancelled event`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val events = mutableListOf<GenerationEvent>()
        val job = launch { viewModel.events.toList(events) }

        viewModel.cancel()
        advanceUntilIdle()

        assertTrue(events.any { it is GenerationEvent.Cancelled })

        job.cancel()
    }

    @Test
    fun `cancel tracks scan_cancelled analytics event`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        fakeAnalyticsManager.clear()

        viewModel.cancel()
        advanceUntilIdle()

        val analyticsEvents = fakeAnalyticsManager.trackedEvents
        assertTrue(analyticsEvents.any { it.name == "scan_cancelled" })
    }

    @Test
    fun `cancel tracks current phase in analytics`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updatePhase(ScanPhase.GENERATING_RULES)
        fakeAnalyticsManager.clear()

        viewModel.cancel()
        advanceUntilIdle()

        val cancelEvent = fakeAnalyticsManager.trackedEvents.first { it.name == "scan_cancelled" }
        assertEquals("GENERATING_RULES", cancelEvent.properties["phase"])
    }

    @Test
    fun `cancel tracks current progress in analytics`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updatePhase(ScanPhase.ANALYZING_IMAGE)
        viewModel.updateProgress(0.30f)
        fakeAnalyticsManager.clear()

        viewModel.cancel()
        advanceUntilIdle()

        val cancelEvent = fakeAnalyticsManager.trackedEvents.first { it.name == "scan_cancelled" }
        assertEquals("0.3", cancelEvent.properties["progress"])
    }

    @Test
    fun `double cancel is ignored`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val events = mutableListOf<GenerationEvent>()
        val job = launch { viewModel.events.toList(events) }

        viewModel.cancel()
        advanceUntilIdle()
        viewModel.cancel() // Second cancel should be ignored
        advanceUntilIdle()

        // Only one Cancelled event (other events may exist from auto-proceed)
        val cancelledEvents = events.filterIsInstance<GenerationEvent.Cancelled>()
        assertEquals(1, cancelledEvents.size)

        job.cancel()
    }

    // =========================================================================
    // Analytics Error Handling Tests
    // =========================================================================

    @Test
    fun `cancel still emits event when analytics fails`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        fakeAnalyticsManager.setThrowOnTrackEvent(true)

        val events = mutableListOf<GenerationEvent>()
        val job = launch { viewModel.events.toList(events) }

        viewModel.cancel()
        advanceUntilIdle()

        assertTrue(events.any { it is GenerationEvent.Cancelled })

        job.cancel()
    }

    @Test
    fun `scan_started analytics failure does not prevent generation`() = runTest {
        fakeAnalyticsManager.setThrowOnTrackEvent(true)

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        // ViewModel should still be in valid state despite analytics failure
        // Pipeline still runs — scan repo is called after analytics tracking
        assertNull(state.error)
    }

    // =========================================================================
    // Image Analysis Tests (Story 5.3)
    // =========================================================================

    @Test
    fun `analyzeImage success stores scanResult in state`() = runTest {
        val expectedResult = ScanResult(
            gameTitle = "Catan",
            confidence = 0.92f,
            thumbnailUrl = "https://example.com/catan.jpg"
        )
        fakeScanRepository.analyzeResult = Result.Success(expectedResult)

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertNotNull(state.scanResult)
        assertEquals("Catan", state.scanResult?.gameTitle)
        assertEquals(0.92f, state.scanResult?.confidence)
        assertEquals("https://example.com/catan.jpg", state.scanResult?.thumbnailUrl)
    }

    @Test
    fun `analyzeImage success with high confidence advances past IDENTIFYING_GAME`() = runTest {
        // Default fake returns 0.95 confidence → auto-proceed → generate rules → SAVING_RULES
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.SAVING_RULES, state.currentPhase)
    }

    @Test
    fun `analyzeImage calls repository with correct imageUri`() = runTest {
        createViewModel(imageUri = "content://media/photo_123.jpg")
        advanceUntilIdle()

        assertEquals(1, fakeScanRepository.analyzeCallCount)
        assertEquals("content://media/photo_123.jpg", fakeScanRepository.lastAnalyzeUri)
    }

    @Test
    fun `analyzeImage error sets error state`() = runTest {
        fakeScanRepository.analyzeResult = Result.Error(
            message = "The analysis took too long. Please try again."
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals("The analysis took too long. Please try again.", state.error)
    }

    @Test
    fun `analyzeImage error shows error screen`() = runTest {
        fakeScanRepository.analyzeResult = Result.Error(
            message = "No internet connection. Please check your network.",
            cause = java.net.UnknownHostException("api.example.com")
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        // Story 5.9: Non-retryable error shows error screen
        assertTrue(state.showError)
        assertEquals("No internet connection. Please check your network.", state.error)
    }

    @Test
    fun `analyzeImage error does not advance phase`() = runTest {
        fakeScanRepository.analyzeResult = Result.Error(message = "Server error.")

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        // Phase should remain at ANALYZING_IMAGE (where the error occurred)
        assertEquals(ScanPhase.ANALYZING_IMAGE, state.currentPhase)
    }

    @Test
    fun `analyzeImage error does not set scanResult`() = runTest {
        fakeScanRepository.analyzeResult = Result.Error(message = "Server error.")

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertNull(state.scanResult)
    }

    @Test
    fun `analyzeImage success with null thumbnailUrl stores scanResult`() = runTest {
        val expectedResult = ScanResult(
            gameTitle = "Chess",
            confidence = 0.99f,
            thumbnailUrl = null
        )
        fakeScanRepository.analyzeResult = Result.Success(expectedResult)

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertNotNull(state.scanResult)
        assertEquals("Chess", state.scanResult?.gameTitle)
        assertNull(state.scanResult?.thumbnailUrl)
    }

    @Test
    fun `blank imageUri does not call scanRepository`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to ""))
        GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager,
            scanRepository = fakeScanRepository,
            gameRepository = FakeGameRepository(),
            creditRepository = FakeCreditRepository(),
        )
        advanceUntilIdle()

        assertEquals(0, fakeScanRepository.analyzeCallCount)
    }

    // =========================================================================
    // Confidence Logic Tests (Story 5.4)
    // =========================================================================

    @Test
    fun `high confidence does not set showConfirmation`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.92f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.showConfirmation)
    }

    @Test
    fun `high confidence auto-advances past IDENTIFYING_GAME`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.95f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.SAVING_RULES, state.currentPhase)
    }

    @Test
    fun `high confidence tracks scan_analysis_complete with auto_proceed true`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.92f, thumbnailUrl = null)
        )
        createViewModel()
        advanceUntilIdle()

        val event = fakeAnalyticsManager.trackedEvents.first { it.name == "scan_analysis_complete" }
        assertEquals("0.92", event.properties["confidence"])
        assertEquals("true", event.properties["auto_proceed"])
    }

    @Test
    fun `high confidence sets gameTitleDisplay`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.92f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals("Catan", state.gameTitleDisplay)
    }

    @Test
    fun `high confidence emits AutoProceeding event`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.92f, thumbnailUrl = null)
        )
        val events = mutableListOf<GenerationEvent>()
        val viewModel = createViewModel()
        val job = launch { viewModel.events.toList(events) }
        advanceUntilIdle()

        val autoProceedingEvents = events.filterIsInstance<GenerationEvent.AutoProceeding>()
        assertEquals(1, autoProceedingEvents.size)
        assertEquals("Catan", autoProceedingEvents[0].gameName)

        job.cancel()
    }

    @Test
    fun `low confidence sets showConfirmation to true`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertTrue(state.showConfirmation)
    }

    @Test
    fun `low confidence stays at IDENTIFYING_GAME phase`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.IDENTIFYING_GAME, state.currentPhase)
    }

    @Test
    fun `low confidence tracks scan_analysis_complete with auto_proceed false`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        createViewModel()
        advanceUntilIdle()

        val event = fakeAnalyticsManager.trackedEvents.first { it.name == "scan_analysis_complete" }
        assertEquals("0.65", event.properties["confidence"])
        assertEquals("false", event.properties["auto_proceed"])
    }

    @Test
    fun `boundary confidence at exactly 0_80 auto-proceeds`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.80f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        // 0.80 >= 0.80 → auto-proceed → generate rules → SAVING_RULES
        assertEquals(ScanPhase.SAVING_RULES, state.currentPhase)
        assertFalse(state.showConfirmation)
    }

    @Test
    fun `confidence just below threshold shows confirmation`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.799f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertTrue(state.showConfirmation)
        assertEquals(ScanPhase.IDENTIFYING_GAME, state.currentPhase)
    }

    @Test
    fun `confidence just above threshold auto-proceeds`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.801f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.showConfirmation)
        assertEquals(ScanPhase.SAVING_RULES, state.currentPhase)
    }

    @Test
    fun `confidence of 0_0 triggers fallback`() = runTest {
        // Story 5.8: Confidence 0.0 is below FALLBACK_CONFIDENCE_THRESHOLD (0.15),
        // so fallback is triggered instead of showing confirmation directly.
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Unknown", confidence = 0.0f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Verify fallback was triggered (not confirmation)
        assertEquals(1, fakeScanRepository.analyzeFallbackCallCount)
    }

    @Test
    fun `confidence of 1_0 auto-proceeds`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 1.0f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.showConfirmation)
        assertEquals(ScanPhase.SAVING_RULES, state.currentPhase)
    }

    // =========================================================================
    // Confirm/Reject Game Tests (Story 5.4)
    // =========================================================================

    @Test
    fun `onConfirmGame clears showConfirmation`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.first().showConfirmation)

        viewModel.onConfirmGame()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.showConfirmation)
    }

    @Test
    fun `onConfirmGame advances to SAVING_RULES`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onConfirmGame()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.SAVING_RULES, state.currentPhase)
    }

    @Test
    fun `onConfirmGame tracks scan_confirmed analytics`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        fakeAnalyticsManager.clear()

        viewModel.onConfirmGame()
        advanceUntilIdle()

        val event = fakeAnalyticsManager.trackedEvents.first { it.name == "scan_confirmed" }
        assertEquals("0.65", event.properties["confidence"])
    }

    @Test
    fun `onRejectGame sets showManualEntry to true`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRejectGame()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertTrue(state.showManualEntry)
    }

    @Test
    fun `onRejectGame clears showConfirmation`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.first().showConfirmation)

        viewModel.onRejectGame()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.showConfirmation)
    }

    @Test
    fun `onRejectGame tracks scan_manual_entry analytics`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        fakeAnalyticsManager.clear()

        viewModel.onRejectGame()
        advanceUntilIdle()

        val event = fakeAnalyticsManager.trackedEvents.first { it.name == "scan_manual_entry" }
        assertEquals("0.65", event.properties["confidence"])
    }

    @Test
    fun `confidence at 0_50 shows confirmation with yellow badge range`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Game", confidence = 0.50f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertTrue(state.showConfirmation)
        assertEquals(ScanPhase.IDENTIFYING_GAME, state.currentPhase)
    }

    // =========================================================================
    // Manual Entry Tests (Story 5.5)
    // =========================================================================

    @Test
    fun `showManualEntry initially false`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.showManualEntry)
    }

    @Test
    fun `manualGameName initially empty`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals("", state.manualGameName)
    }

    @Test
    fun `onManualGameNameChanged updates manualGameName state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onManualGameNameChanged("Settlers of Catan")
        val state = viewModel.uiState.first()

        assertEquals("Settlers of Catan", state.manualGameName)
    }

    @Test
    fun `onManualGameNameSubmitted with valid name advances to SAVING_RULES`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRejectGame()
        advanceUntilIdle()

        viewModel.onManualGameNameChanged("Monopoly")
        viewModel.onManualGameNameSubmitted()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.SAVING_RULES, state.currentPhase)
    }

    @Test
    fun `onManualGameNameSubmitted sets gameTitleDisplay to manual name`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRejectGame()
        advanceUntilIdle()

        viewModel.onManualGameNameChanged("Monopoly")
        viewModel.onManualGameNameSubmitted()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals("Monopoly", state.gameTitleDisplay)
    }

    @Test
    fun `onManualGameNameSubmitted clears showManualEntry`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRejectGame()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.first().showManualEntry)

        viewModel.onManualGameNameChanged("Monopoly")
        viewModel.onManualGameNameSubmitted()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.showManualEntry)
    }

    @Test
    fun `onManualGameNameSubmitted with blank name does not advance`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRejectGame()
        advanceUntilIdle()

        viewModel.onManualGameNameChanged("   ")
        viewModel.onManualGameNameSubmitted()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertTrue(state.showManualEntry)
        assertEquals(ScanPhase.IDENTIFYING_GAME, state.currentPhase)
    }

    @Test
    fun `onManualGameNameSubmitted tracks scan_manual_name_submitted analytics`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRejectGame()
        advanceUntilIdle()
        fakeAnalyticsManager.clear()

        viewModel.onManualGameNameChanged("Monopoly")
        viewModel.onManualGameNameSubmitted()
        advanceUntilIdle()

        val event = fakeAnalyticsManager.trackedEvents.first { it.name == "scan_manual_name_submitted" }
        assertEquals("Monopoly", event.properties["game_name"])
    }

    @Test
    fun `onManualGameNameSubmitted trims whitespace from game name`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRejectGame()
        advanceUntilIdle()

        viewModel.onManualGameNameChanged("  Monopoly  ")
        viewModel.onManualGameNameSubmitted()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals("Monopoly", state.gameTitleDisplay)
    }

    // =========================================================================
    // Rules Generation Tests (Story 5.6)
    // =========================================================================

    @Test
    fun `rules generation on auto-proceed stores rules in state`() = runTest {
        val expectedRules = Rules(
            gameId = "Catan",
            overview = RuleSection(title = "Overview", content = "Catan overview", items = null),
            setup = RuleSection(title = "Setup", content = "Catan setup", items = null),
            firstRound = RuleSection(title = "First Round", content = "Catan first round", items = null),
            advanced = RuleSection(title = "Advanced", content = "Catan advanced", items = null)
        )
        fakeScanRepository.generateResult = Result.Success(expectedRules)
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.95f, thumbnailUrl = null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertNotNull(state.rules)
        assertEquals("Catan", state.rules?.gameId)
        assertEquals("Catan overview", state.rules?.overview?.content)
    }

    @Test
    fun `rules generation on auto-proceed advances to SAVING_RULES`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.95f, thumbnailUrl = null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.SAVING_RULES, state.currentPhase)
    }

    @Test
    fun `rules generation on auto-proceed calls repository with correct game title`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Ticket to Ride", confidence = 0.95f, thumbnailUrl = null)
        )

        createViewModel()
        advanceUntilIdle()

        assertEquals(1, fakeScanRepository.generateCallCount)
        assertEquals("Ticket to Ride", fakeScanRepository.lastGenerateGameTitle)
    }

    @Test
    fun `rules generation on auto-proceed forwards thumbnailUrl to repository`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.95f, thumbnailUrl = "https://example.com/catan.jpg")
        )

        createViewModel()
        advanceUntilIdle()

        assertEquals("https://example.com/catan.jpg", fakeScanRepository.lastGenerateThumbnailUrl)
    }

    @Test
    fun `rules generation on confirm forwards thumbnailUrl to repository`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = "https://example.com/catan.jpg")
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onConfirmGame()
        advanceUntilIdle()

        assertEquals("https://example.com/catan.jpg", fakeScanRepository.lastGenerateThumbnailUrl)
    }

    @Test
    fun `rules generation on manual entry forwards thumbnailUrl from scan result`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = "https://example.com/catan.jpg")
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRejectGame()
        advanceUntilIdle()

        viewModel.onManualGameNameChanged("Monopoly")
        viewModel.onManualGameNameSubmitted()
        advanceUntilIdle()

        assertEquals("https://example.com/catan.jpg", fakeScanRepository.lastGenerateThumbnailUrl)
    }

    @Test
    fun `rules generation on confirm calls repository with correct game title`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onConfirmGame()
        advanceUntilIdle()

        assertEquals(1, fakeScanRepository.generateCallCount)
        assertEquals("Catan", fakeScanRepository.lastGenerateGameTitle)
    }

    @Test
    fun `rules generation on manual entry calls repository with manual name`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRejectGame()
        advanceUntilIdle()

        viewModel.onManualGameNameChanged("Monopoly")
        viewModel.onManualGameNameSubmitted()
        advanceUntilIdle()

        assertEquals(1, fakeScanRepository.generateCallCount)
        assertEquals("Monopoly", fakeScanRepository.lastGenerateGameTitle)
    }

    @Test
    fun `rules generation error sets error state`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.95f, thumbnailUrl = null)
        )
        fakeScanRepository.generateResult = Result.Error(
            message = "The analysis took too long. Please try again."
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals("The analysis took too long. Please try again.", state.error)
    }

    @Test
    fun `rules generation error shows error screen`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.95f, thumbnailUrl = null)
        )
        fakeScanRepository.generateResult = Result.Error(
            message = "Server error. Please try again later."
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertTrue(state.showError)
        assertEquals("Server error. Please try again later.", state.error)
    }

    @Test
    fun `rules generation error does not advance phase past GENERATING_RULES`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.95f, thumbnailUrl = null)
        )
        fakeScanRepository.generateResult = Result.Error(message = "Server error.")

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.GENERATING_RULES, state.currentPhase)
    }

    @Test
    fun `rules generation tracks scan_generation_complete on success`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.95f, thumbnailUrl = null)
        )

        createViewModel()
        advanceUntilIdle()

        val event = fakeAnalyticsManager.trackedEvents.first { it.name == "scan_generation_complete" }
        // The analytics tracks the original game title from scanResult (which is "Test Game" in the default fake)
        // but the actual generateRules call uses "Catan" from the updated analyzeResult above
        assertEquals("Catan", event.properties["game_name"])
        assertNotNull(event.properties["duration_ms"])
    }

    @Test
    fun `rules generation tracks scan_failed on error`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.95f, thumbnailUrl = null)
        )
        fakeScanRepository.generateResult = Result.Error(
            message = "Server error.",
            cause = java.net.SocketTimeoutException()
        )

        createViewModel()
        advanceUntilIdle()

        val event = fakeAnalyticsManager.trackedEvents.first { it.name == "scan_failed" }
        assertEquals("timeout", event.properties["error_type"])
    }

    @Test
    fun `rules generation stores rules with all sections`() = runTest {
        val expectedRules = Rules(
            gameId = "Pandemic",
            overview = RuleSection(title = "Overview", content = "Overview content", items = null),
            setup = RuleSection(title = "Setup", content = "Setup content", items = null),
            firstRound = RuleSection(title = "First Round", content = "First round content", items = null),
            advanced = RuleSection(title = "Advanced", content = "Advanced content", items = null)
        )
        fakeScanRepository.generateResult = Result.Success(expectedRules)
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Pandemic", confidence = 0.95f, thumbnailUrl = null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertNotNull(state.rules)
        assertEquals("Overview content", state.rules?.overview?.content)
        assertEquals("Setup content", state.rules?.setup?.content)
        assertEquals("First round content", state.rules?.firstRound?.content)
        assertEquals("Advanced content", state.rules?.advanced?.content)
    }

    // =========================================================================
    // Story 5.7: Save Rules Tests
    // =========================================================================

    @Test
    fun `successful save navigates to rules with correct gameId`() = runTest {
        val fakeGameRepository = FakeGameRepository()
        val fakeCreditRepository = FakeCreditRepository()

        val viewModel = createViewModelWithRepositories(
            gameRepository = fakeGameRepository,
            creditRepository = fakeCreditRepository
        )
        advanceUntilIdle()

        // Should emit NavigateToRules event with the generated game ID
        val events = mutableListOf<GenerationEvent>()
        val job = launch {
            viewModel.events.toList(events)
        }

        advanceUntilIdle()
        job.cancel()

        val navigateEvent = events.filterIsInstance<GenerationEvent.NavigateToRules>().firstOrNull()
        assertNotNull(navigateEvent)
        assertTrue(navigateEvent.gameId.isNotEmpty())
    }

    @Test
    fun `successful save deducts credit`() = runTest {
        val fakeGameRepository = FakeGameRepository()
        val fakeCreditRepository = FakeCreditRepository()
        fakeCreditRepository.deductResult = true

        val viewModel = createViewModelWithRepositories(
            gameRepository = fakeGameRepository,
            creditRepository = fakeCreditRepository
        )
        advanceUntilIdle()

        assertEquals(1, fakeCreditRepository.deductCallCount)
    }

    @Test
    fun `save error sets error state and does not deduct credit`() = runTest {
        val fakeGameRepository = FakeGameRepository()
        fakeGameRepository.saveGameWithRulesResult = Result.Error("Database error")
        val fakeCreditRepository = FakeCreditRepository()

        val viewModel = createViewModelWithRepositories(
            gameRepository = fakeGameRepository,
            creditRepository = fakeCreditRepository
        )
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state.error)
        assertEquals(0, fakeCreditRepository.deductCallCount)
    }

    @Test
    fun `save error shows error screen`() = runTest {
        val fakeGameRepository = FakeGameRepository()
        fakeGameRepository.saveGameWithRulesResult = Result.Error("Database error")
        val fakeCreditRepository = FakeCreditRepository()

        val viewModel = createViewModelWithRepositories(
            gameRepository = fakeGameRepository,
            creditRepository = fakeCreditRepository
        )

        advanceUntilIdle()
        val state = viewModel.uiState.first()

        // Story 5.9: Save error shows error screen
        assertTrue(state.showError)
        assertTrue(state.error?.contains("Database error") ?: false)
    }

    @Test
    fun `credit deduction failure after save still navigates (defensive)`() = runTest {
        val fakeGameRepository = FakeGameRepository()
        val fakeCreditRepository = FakeCreditRepository()
        fakeCreditRepository.deductResult = false // Balance already 0

        val viewModel = createViewModelWithRepositories(
            gameRepository = fakeGameRepository,
            creditRepository = fakeCreditRepository
        )

        val events = mutableListOf<GenerationEvent>()
        val job = launch {
            viewModel.events.toList(events)
        }

        advanceUntilIdle()
        job.cancel()

        // Still navigates even if credit deduction fails
        val navigateEvent = events.filterIsInstance<GenerationEvent.NavigateToRules>().firstOrNull()
        assertNotNull(navigateEvent)
    }

    @Test
    fun `credit deduction exception after save still navigates`() = runTest {
        val fakeGameRepository = FakeGameRepository()
        val fakeCreditRepository = FakeCreditRepository()
        fakeCreditRepository.shouldThrowOnDeduct = true // Simulate DataStore IO error

        val viewModel = createViewModelWithRepositories(
            gameRepository = fakeGameRepository,
            creditRepository = fakeCreditRepository
        )

        val events = mutableListOf<GenerationEvent>()
        val job = launch {
            viewModel.events.toList(events)
        }

        advanceUntilIdle()
        job.cancel()

        // Still navigates even if credit deduction throws
        val navigateEvent = events.filterIsInstance<GenerationEvent.NavigateToRules>().firstOrNull()
        assertNotNull(navigateEvent)
    }

    @Test
    fun `successful save tracks credit_deducted analytics with correct balance`() = runTest {
        val fakeGameRepository = FakeGameRepository()
        val fakeCreditRepository = FakeCreditRepository()
        fakeCreditRepository.deductResult = true

        val viewModel = createViewModelWithRepositories(
            gameRepository = fakeGameRepository,
            creditRepository = fakeCreditRepository
        )
        advanceUntilIdle()

        // Verify credit_deducted analytics was tracked
        val creditDeductedEvents = fakeAnalyticsManager.trackedEvents.filter { it.name == "credit_deducted" }
        assertEquals(1, creditDeductedEvents.size)
        assertEquals("3", creditDeductedEvents[0].properties["new_balance"])
        assertEquals("test-game-id-123", creditDeductedEvents[0].properties["game_id"])
    }

    @Test
    fun `successful save tracks scan_completed analytics with gameId and gameName`() = runTest {
        val fakeGameRepository = FakeGameRepository()
        val fakeCreditRepository = FakeCreditRepository()

        val viewModel = createViewModelWithRepositories(
            gameRepository = fakeGameRepository,
            creditRepository = fakeCreditRepository
        )
        advanceUntilIdle()

        // Verify scan_completed analytics was tracked
        val scanCompletedEvents = fakeAnalyticsManager.trackedEvents.filter { it.name == "scan_completed" }
        assertEquals(1, scanCompletedEvents.size)
        assertEquals("test-game-id-123", scanCompletedEvents[0].properties["game_id"])
        assertEquals("Test Game", scanCompletedEvents[0].properties["game_name"])
        assertEquals("3", scanCompletedEvents[0].properties["new_credit_balance"])
    }

    @Test
    fun `analytics failure after credit deduction does not block navigation`() = runTest {
        val fakeGameRepository = FakeGameRepository()
        val fakeCreditRepository = FakeCreditRepository()
        fakeAnalyticsManager.setThrowOnTrackEvent(true)

        val viewModel = createViewModelWithRepositories(
            gameRepository = fakeGameRepository,
            creditRepository = fakeCreditRepository
        )

        val events = mutableListOf<GenerationEvent>()
        val job = launch {
            viewModel.events.toList(events)
        }

        advanceUntilIdle()
        job.cancel()

        // Still navigates even if analytics fails
        val navigateEvent = events.filterIsInstance<GenerationEvent.NavigateToRules>().firstOrNull()
        assertNotNull(navigateEvent)
    }

    @Test
    fun `save error does not track credit_deducted analytics`() = runTest {
        val fakeGameRepository = FakeGameRepository()
        fakeGameRepository.saveGameWithRulesResult = Result.Error("Database error")
        val fakeCreditRepository = FakeCreditRepository()

        val viewModel = createViewModelWithRepositories(
            gameRepository = fakeGameRepository,
            creditRepository = fakeCreditRepository
        )
        advanceUntilIdle()

        // Verify no credit_deducted analytics was tracked
        val creditDeductedEvents = fakeAnalyticsManager.trackedEvents.filter { it.name == "credit_deducted" }
        assertEquals(0, creditDeductedEvents.size)

        // Verify no scan_completed analytics was tracked
        val scanCompletedEvents = fakeAnalyticsManager.trackedEvents.filter { it.name == "scan_completed" }
        assertEquals(0, scanCompletedEvents.size)
    }

    @Test
    fun `confirm path sets gameTitleDisplay and navigates to rules after save`() = runTest {
        // Low confidence → shows confirmation screen
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Low Conf Game", confidence = 0.50f, thumbnailUrl = null)
        )
        val fakeGameRepository = FakeGameRepository()
        val fakeCreditRepository = FakeCreditRepository()

        val viewModel = createViewModelWithRepositories(
            gameRepository = fakeGameRepository,
            creditRepository = fakeCreditRepository
        )
        advanceUntilIdle()

        // Should be showing confirmation
        val confirmState = viewModel.uiState.first()
        assertTrue(confirmState.showConfirmation)

        // User confirms the game
        val events = mutableListOf<GenerationEvent>()
        val job = launch { viewModel.events.toList(events) }

        viewModel.onConfirmGame()
        advanceUntilIdle()
        job.cancel()

        // Verify gameTitleDisplay was set and navigation happened
        val state = viewModel.uiState.first()
        assertEquals("Low Conf Game", state.gameTitleDisplay)
        val navigateEvent = events.filterIsInstance<GenerationEvent.NavigateToRules>().firstOrNull()
        assertNotNull(navigateEvent)
    }

    private fun createViewModelWithRepositories(
        imageUri: String = "file:///test/image.jpg",
        gameRepository: FakeGameRepository = FakeGameRepository(),
        creditRepository: FakeCreditRepository = FakeCreditRepository()
    ): GenerationViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to imageUri))
        return GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager,
            scanRepository = fakeScanRepository,
            gameRepository = gameRepository,
            creditRepository = creditRepository
        )
    }

    // =========================================================================
    // Story 5.8: Fallback AI Model Tests
    // =========================================================================

    @Test
    fun `fallback triggered on primary very low confidence`() = runTest {
        // Primary returns very low confidence (0.10 < 0.15 threshold) → fallback triggered
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Obscure Game", confidence = 0.10f, thumbnailUrl = null)
        )
        fakeScanRepository.analyzeFallbackResult = Result.Success(
            ScanResult(gameTitle = "Fallback Success", confidence = 0.85f, thumbnailUrl = null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Verify fallback was called
        assertEquals(1, fakeScanRepository.analyzeCallCount)
        assertEquals(1, fakeScanRepository.analyzeFallbackCallCount)

        // Verify final state uses fallback result
        val state = viewModel.uiState.first()
        assertEquals("Fallback Success", state.scanResult?.gameTitle)
    }

    @Test
    fun `fallback triggered on primary timeout error`() = runTest {
        // Primary returns timeout error → fallback triggered
        fakeScanRepository.analyzeResult = Result.Error(
            message = "Timeout",
            cause = java.net.SocketTimeoutException("Read timed out")
        )
        fakeScanRepository.analyzeFallbackResult = Result.Success(
            ScanResult(gameTitle = "Fallback Success", confidence = 0.85f, thumbnailUrl = null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Verify fallback was called
        assertEquals(1, fakeScanRepository.analyzeCallCount)
        assertEquals(1, fakeScanRepository.analyzeFallbackCallCount)

        // Verify fallback result succeeded
        val state = viewModel.uiState.first()
        assertEquals("Fallback Success", state.scanResult?.gameTitle)
    }

    @Test
    fun `fallback NOT triggered on no internet error`() = runTest {
        // Primary returns no internet error → fallback NOT triggered (will also fail)
        fakeScanRepository.analyzeResult = Result.Error(
            message = "No internet",
            cause = java.net.UnknownHostException("api.example.com")
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Verify fallback was NOT called
        assertEquals(1, fakeScanRepository.analyzeCallCount)
        assertEquals(0, fakeScanRepository.analyzeFallbackCallCount)

        // Verify error state
        val state = viewModel.uiState.first()
        assertNotNull(state.error)
    }

    @Test
    fun `fallback NOT triggered on high confidence primary success`() = runTest {
        // Primary returns high confidence (>= 0.80 threshold) → no fallback needed
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Popular Game", confidence = 0.95f, thumbnailUrl = null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Verify fallback was NOT called
        assertEquals(1, fakeScanRepository.analyzeCallCount)
        assertEquals(0, fakeScanRepository.analyzeFallbackCallCount)

        // Verify primary result was used
        val state = viewModel.uiState.first()
        assertEquals("Popular Game", state.scanResult?.gameTitle)
    }

    @Test
    fun `fallback success continues normal confidence flow`() = runTest {
        // Primary fails, fallback succeeds with high confidence → auto-proceed
        fakeScanRepository.analyzeResult = Result.Error("Primary failed", cause = java.net.SocketTimeoutException())
        fakeScanRepository.analyzeFallbackResult = Result.Success(
            ScanResult(gameTitle = "Fallback High Conf", confidence = 0.90f, thumbnailUrl = null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Verify reached SAVING_RULES phase (auto-proceeded due to high confidence)
        val state = viewModel.uiState.first()
        assertEquals(ScanPhase.SAVING_RULES, state.currentPhase)
        assertEquals("Fallback High Conf", state.gameTitleDisplay)
    }

    @Test
    fun `fallback failure shows manual entry`() = runTest {
        // Primary fails, fallback also fails → show manual entry (not error event)
        fakeScanRepository.analyzeResult = Result.Error("Primary failed", cause = java.net.SocketTimeoutException())
        fakeScanRepository.analyzeFallbackResult = Result.Error("Fallback failed", cause = Exception("Unknown error"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Verify showManualEntry is true (not error state)
        val state = viewModel.uiState.first()
        assertTrue(state.showManualEntry)
        assertNull(state.error)
    }

    @Test
    fun `fallback clears fallback state after completion`() = runTest {
        // Verify fallback state is properly cleared after a fallback attempt completes
        fakeScanRepository.analyzeResult = Result.Error("Primary failed", cause = java.net.SocketTimeoutException())
        fakeScanRepository.analyzeFallbackResult = Result.Success(
            ScanResult(gameTitle = "Fallback Success", confidence = 0.85f, thumbnailUrl = null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Verify fallback was executed (proves isFallbackInProgress was set and cleared)
        assertEquals(1, fakeScanRepository.analyzeFallbackCallCount)

        // Verify fallback UI state is cleared after completion
        val finalState = viewModel.uiState.first()
        assertFalse(finalState.isFallbackInProgress)
        assertNull(finalState.fallbackMessage)

        // Verify the fallback result was used successfully
        assertEquals("Fallback Success", finalState.scanResult?.gameTitle)
    }

    @Test
    fun `fallback tracks scan_fallback_used analytics on success`() = runTest {
        fakeScanRepository.analyzeResult = Result.Error("Primary timeout", cause = java.net.SocketTimeoutException())
        fakeScanRepository.analyzeFallbackResult = Result.Success(
            ScanResult(gameTitle = "Fallback Success", confidence = 0.85f, thumbnailUrl = null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Verify scan_fallback_used event was tracked
        val fallbackUsedEvent = fakeAnalyticsManager.trackedEvents.firstOrNull { it.name == "scan_fallback_used" }
        assertNotNull(fallbackUsedEvent)
        assertEquals("timeout", fallbackUsedEvent.properties["primary_error_type"])
        assertEquals("success", fallbackUsedEvent.properties["fallback_result"])
    }

    @Test
    fun `fallback tracks scan_fallback_used analytics on low confidence trigger`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Obscure Game", confidence = 0.10f, thumbnailUrl = null)
        )
        fakeScanRepository.analyzeFallbackResult = Result.Success(
            ScanResult(gameTitle = "Fallback Success", confidence = 0.85f, thumbnailUrl = null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Verify scan_fallback_used event was tracked with confidence
        val fallbackUsedEvent = fakeAnalyticsManager.trackedEvents.firstOrNull { it.name == "scan_fallback_used" }
        assertNotNull(fallbackUsedEvent)
        assertEquals("low_confidence", fallbackUsedEvent.properties["primary_error_type"])
        assertEquals("0.1", fallbackUsedEvent.properties["primary_confidence"])
        assertEquals("success", fallbackUsedEvent.properties["fallback_result"])
    }

    @Test
    fun `fallback tracks scan_fallback_failed analytics when both fail`() = runTest {
        fakeScanRepository.analyzeResult = Result.Error("Primary timeout", cause = java.net.SocketTimeoutException())
        fakeScanRepository.analyzeFallbackResult = Result.Error("Fallback failed", cause = Exception("Server error"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Verify scan_fallback_failed event was tracked
        val fallbackFailedEvent = fakeAnalyticsManager.trackedEvents.firstOrNull { it.name == "scan_fallback_failed" }
        assertNotNull(fallbackFailedEvent)
        assertEquals("timeout", fallbackFailedEvent.properties["primary_error_type"])
        assertEquals("unknown", fallbackFailedEvent.properties["fallback_error_type"])
    }
}

/**
 * Fake implementation of [ScanRepository] for testing.
 */
class FakeScanRepository : ScanRepository {

    var analyzeResult: Result<ScanResult> = Result.Success(
        ScanResult(gameTitle = "Test Game", confidence = 0.95f, thumbnailUrl = null)
    )
    var analyzeCallCount = 0
    var lastAnalyzeUri: String? = null

    var analyzeFallbackResult: Result<ScanResult> = Result.Success(
        ScanResult(gameTitle = "Fallback Game", confidence = 0.85f, thumbnailUrl = null)
    )
    var analyzeFallbackCallCount = 0
    var lastAnalyzeFallbackUri: String? = null

    var generateResult: Result<Rules> = Result.Success(
        Rules(
            gameId = "Test Game",
            overview = RuleSection(title = "Overview", content = "Overview content", items = null),
            setup = RuleSection(title = "Setup", content = "Setup content", items = null),
            firstRound = RuleSection(title = "First Round", content = "First round content", items = null),
            advanced = RuleSection(title = "Advanced", content = "Advanced content", items = null)
        )
    )
    var generateCallCount = 0
    var lastGenerateGameTitle: String? = null
    var lastGenerateThumbnailUrl: String? = null

    // Story 5.9: Error test flags
    var shouldReturnNoInternetError = false
    var shouldFailRulesGeneration = false
    var shouldThrowGenericException = false

    override suspend fun analyzeImage(imageUri: String): Result<ScanResult> {
        analyzeCallCount++
        lastAnalyzeUri = imageUri
        if (shouldReturnNoInternetError) {
            return Result.Error("No internet connection", cause = java.net.UnknownHostException("api.example.com"))
        }
        if (shouldThrowGenericException) {
            throw RuntimeException("Simulated exception")
        }
        return analyzeResult
    }

    override suspend fun analyzeImageFallback(imageUri: String): Result<ScanResult> {
        analyzeFallbackCallCount++
        lastAnalyzeFallbackUri = imageUri
        return analyzeFallbackResult
    }

    override suspend fun generateRules(gameTitle: String, thumbnailUrl: String?): Result<Rules> {
        generateCallCount++
        lastGenerateGameTitle = gameTitle
        lastGenerateThumbnailUrl = thumbnailUrl
        if (shouldFailRulesGeneration) {
            return Result.Error("Failed to generate rules", cause = Exception("AI service error"))
        }
        return generateResult
    }
}

/**
 * Fake implementation of [AnalyticsManager] for testing.
 */
class FakeAnalyticsManager : AnalyticsManager {

    data class TrackedEvent(
        val name: String,
        val properties: Map<String, String>
    )

    private val _trackedEvents = mutableListOf<TrackedEvent>()
    val trackedEvents: List<TrackedEvent> get() = _trackedEvents.toList()

    private val _trackedScreenViews = mutableListOf<String>()

    private var shouldThrowOnTrackEvent = false

    fun setThrowOnTrackEvent(shouldThrow: Boolean) {
        shouldThrowOnTrackEvent = shouldThrow
    }

    override fun trackEvent(name: String, properties: Map<String, String>) {
        if (shouldThrowOnTrackEvent) {
            throw RuntimeException("Simulated analytics error")
        }
        _trackedEvents.add(TrackedEvent(name, properties))
    }

    override fun trackScreenView(screenName: String) {
        _trackedScreenViews.add(screenName)
    }

    fun clear() {
        _trackedEvents.clear()
        _trackedScreenViews.clear()
        shouldThrowOnTrackEvent = false
    }
}

/**
 * Fake implementation of [com.rulebook.core.data.repository.GameRepository] for testing.
 */
class FakeGameRepository : com.rulebook.core.data.repository.GameRepository {

    var saveGameWithRulesResult: Result<String> = Result.Success("test-game-id-123")
    var saveGameWithRulesCallCount = 0
    var lastSavedGame: com.rulebook.core.model.Game? = null
    var lastSavedRules: Rules? = null
    var lastSavedRawJson: String? = null

    // Story 5.9: Error test flag
    var shouldFailSave = false

    override suspend fun getGames(): Result<List<com.rulebook.core.model.Game>> {
        return Result.Success(emptyList())
    }

    override fun getGamesSorted(sortOrder: com.rulebook.core.model.SortOrder): kotlinx.coroutines.flow.Flow<List<com.rulebook.core.model.Game>> {
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }

    override suspend fun getGameById(id: String): Result<com.rulebook.core.model.Game> {
        return Result.Error("Not implemented")
    }

    override suspend fun saveGame(game: com.rulebook.core.model.Game): Result<Unit> {
        return Result.Success(Unit)
    }

    override suspend fun deleteGame(id: String): Result<Unit> {
        return Result.Success(Unit)
    }

    override suspend fun getRulesForGame(gameId: String): Result<Rules> {
        return Result.Error("Not implemented")
    }

    override suspend fun saveGameWithRules(
        game: com.rulebook.core.model.Game,
        rules: Rules,
        rawJson: String
    ): Result<String> {
        saveGameWithRulesCallCount++
        lastSavedGame = game
        lastSavedRules = rules
        lastSavedRawJson = rawJson
        if (shouldFailSave) {
            return Result.Error("Database save failed", cause = Exception("SQLite error"))
        }
        return saveGameWithRulesResult
    }

    override suspend fun updateLastAccessed(gameId: String): Result<Unit> {
        return Result.Success(Unit)
    }
}

/**
 * Fake implementation of [com.rulebook.core.data.repository.CreditRepository] for testing.
 */
class FakeCreditRepository : com.rulebook.core.data.repository.CreditRepository {

    var deductResult = true
    var deductCallCount = 0
    var shouldThrowOnDeduct = false

    override val creditBalance: kotlinx.coroutines.flow.Flow<Int> =
        kotlinx.coroutines.flow.MutableStateFlow(3)

    override suspend fun awardInitialCredits(amount: Int): Boolean {
        return true
    }

    override suspend fun deductCredit(): Boolean {
        deductCallCount++
        if (shouldThrowOnDeduct) throw RuntimeException("Simulated DataStore IO error")
        return deductResult
    }

    override suspend fun hasCredits(): Boolean {
        return true
    }
}
