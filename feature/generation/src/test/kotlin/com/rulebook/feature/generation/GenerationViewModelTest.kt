package com.rulebook.feature.generation

import androidx.lifecycle.SavedStateHandle
import com.rulebook.core.analytics.AnalyticsManager
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GenerationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeAnalyticsManager: FakeAnalyticsManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAnalyticsManager = FakeAnalyticsManager()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(imageUri: String = "file:///test/image.jpg"): GenerationViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to imageUri))
        return GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager
        )
    }

    // =========================================================================
    // Initial State Tests
    // =========================================================================

    @Test
    fun `initial state has PROCESSING_IMAGE phase when imageUri provided`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(ScanPhase.PROCESSING_IMAGE, state.currentPhase)
    }

    @Test
    fun `initial state extracts imageUri from SavedStateHandle`() = runTest {
        val viewModel = createViewModel(imageUri = "content://test/photo.jpg")
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals("content://test/photo.jpg", state.imageUri)
    }

    @Test
    fun `initial state has zero progress for PROCESSING_IMAGE`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(0f, state.overallProgress)
    }

    @Test
    fun `initial state is not cancelling`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.isCancelling)
    }

    @Test
    fun `initial state has no error`() = runTest {
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
        val viewModel = GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager
        )
        advanceUntilIdle()

        // No scan_started event since generation didn't start
        assertTrue(fakeAnalyticsManager.trackedEvents.none { it.name == "scan_started" })
    }

    @Test
    fun `blank imageUri sets error state`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to ""))
        val viewModel = GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager
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
            analyticsManager = fakeAnalyticsManager
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
        val viewModel = GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager
        )
        advanceUntilIdle()

        assertTrue(fakeAnalyticsManager.trackedEvents.none { it.name == "scan_started" })
    }

    @Test
    fun `missing imageUri sets error state`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = GenerationViewModel(
            savedStateHandle = savedStateHandle,
            analyticsManager = fakeAnalyticsManager
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

        viewModel.updateProgress(0.10f)
        val state = viewModel.uiState.first()

        assertEquals(0.10f, state.overallProgress)
    }

    @Test
    fun `updateProgress clamps to current phase max`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Phase is PROCESSING_IMAGE (0-15%)
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

        assertEquals(1, events.size)
        assertTrue(events[0] is GenerationEvent.Cancelled)

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

        // Only one Cancelled event
        assertEquals(1, events.size)

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

        assertEquals(1, events.size)
        assertTrue(events[0] is GenerationEvent.Cancelled)

        job.cancel()
    }

    @Test
    fun `scan_started analytics failure does not prevent generation`() = runTest {
        fakeAnalyticsManager.setThrowOnTrackEvent(true)

        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        // ViewModel should still be in valid state despite analytics failure
        assertEquals(ScanPhase.PROCESSING_IMAGE, state.currentPhase)
        assertNull(state.error)
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
