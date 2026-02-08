package com.rulebook.feature.generation

import androidx.lifecycle.SavedStateHandle
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.ScanRepository
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
        )
    }

    // =========================================================================
    // Initial State Tests
    // =========================================================================

    @Test
    fun `successful analysis with high confidence auto-proceeds to GENERATING_RULES phase`() = runTest {
        // Default fake returns 0.95 confidence (>= 0.80 threshold) → auto-proceed
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.GENERATING_RULES, state.currentPhase)
    }

    @Test
    fun `initial state extracts imageUri from SavedStateHandle`() = runTest {
        val viewModel = createViewModel(imageUri = "content://test/photo.jpg")
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals("content://test/photo.jpg", state.imageUri)
    }

    @Test
    fun `successful analysis with high confidence sets progress to GENERATING_RULES start`() = runTest {
        // Default fake returns 0.95 confidence → auto-proceed to GENERATING_RULES (0.60)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(0.60f, state.overallProgress)
    }

    @Test
    fun `initial state is not cancelling`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.isCancelling)
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

        // After successful analysis with high confidence, phase is GENERATING_RULES (60-90%)
        viewModel.updateProgress(0.75f)
        val state = viewModel.uiState.first()

        assertEquals(0.75f, state.overallProgress)
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
        // Default fake returns 0.95 confidence → auto-proceed to GENERATING_RULES
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.GENERATING_RULES, state.currentPhase)
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
    fun `analyzeImage error emits Error event`() = runTest {
        fakeScanRepository.analyzeResult = Result.Error(
            message = "No internet connection. Please check your network."
        )
        val events = mutableListOf<GenerationEvent>()

        val viewModel = createViewModel()
        val job = launch { viewModel.events.toList(events) }
        advanceUntilIdle()

        assertTrue(events.any { it is GenerationEvent.Error })
        val errorEvent = events.filterIsInstance<GenerationEvent.Error>().first()
        assertEquals("No internet connection. Please check your network.", errorEvent.message)

        job.cancel()
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

        assertEquals(ScanPhase.GENERATING_RULES, state.currentPhase)
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

        // 0.80 >= 0.80 → auto-proceed
        assertEquals(ScanPhase.GENERATING_RULES, state.currentPhase)
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
        assertEquals(ScanPhase.GENERATING_RULES, state.currentPhase)
    }

    @Test
    fun `confidence of 0_0 shows confirmation`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Unknown", confidence = 0.0f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertTrue(state.showConfirmation)
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
        assertEquals(ScanPhase.GENERATING_RULES, state.currentPhase)
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
    fun `onConfirmGame advances to GENERATING_RULES`() = runTest {
        fakeScanRepository.analyzeResult = Result.Success(
            ScanResult(gameTitle = "Catan", confidence = 0.65f, thumbnailUrl = null)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onConfirmGame()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.GENERATING_RULES, state.currentPhase)
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
    fun `onManualGameNameSubmitted with valid name advances to GENERATING_RULES`() = runTest {
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

        assertEquals(ScanPhase.GENERATING_RULES, state.currentPhase)
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

    override suspend fun analyzeImage(imageUri: String): Result<ScanResult> {
        analyzeCallCount++
        lastAnalyzeUri = imageUri
        return analyzeResult
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
