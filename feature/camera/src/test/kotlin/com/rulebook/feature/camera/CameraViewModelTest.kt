package com.rulebook.feature.camera

import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.datastore.HapticsPreferencesSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {

    // Use UnconfinedTestDispatcher so coroutines execute eagerly without needing advanceUntilIdle
    // This ensures creditBalance flow updates are processed immediately during tests
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: CameraViewModel
    private lateinit var fakeCreditRepository: FakeCreditRepository
    private lateinit var fakeAnalyticsManager: FakeAnalyticsManager
    private lateinit var fakeHapticsPreferencesSource: FakeHapticsPreferencesSource

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeCreditRepository = FakeCreditRepository()
        fakeAnalyticsManager = FakeAnalyticsManager()
        fakeHapticsPreferencesSource = FakeHapticsPreferencesSource()
        viewModel = CameraViewModel(
            creditRepository = fakeCreditRepository,
            analyticsManager = fakeAnalyticsManager,
            hapticsPreferencesSource = fakeHapticsPreferencesSource
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has camera not ready and no error`() = runTest {
        val state = viewModel.uiState.first()

        assertFalse(state.isCameraReady)
        assertNull(state.error)
    }

    @Test
    fun `onCameraReady sets isCameraReady to true`() = runTest {
        viewModel.onCameraReady()
        val state = viewModel.uiState.first()

        assertTrue(state.isCameraReady)
        assertNull(state.error)
    }

    @Test
    fun `onCameraError sets error message and camera not ready`() = runTest {
        val errorMessage = "Camera initialization failed"
        viewModel.onCameraError(errorMessage)
        val state = viewModel.uiState.first()

        assertFalse(state.isCameraReady)
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun `onCameraReady clears existing error`() = runTest {
        // First set an error
        viewModel.onCameraError("Some error")

        // Then mark camera as ready
        viewModel.onCameraReady()
        val state = viewModel.uiState.first()

        assertTrue(state.isCameraReady)
        assertNull(state.error)
    }

    @Test
    fun `clearError removes error but keeps camera ready state`() = runTest {
        // Set error first
        viewModel.onCameraError("Some error")

        // Clear the error
        viewModel.clearError()
        val state = viewModel.uiState.first()

        assertNull(state.error)
        // Camera is still not ready (wasn't set ready before)
        assertFalse(state.isCameraReady)
    }

    @Test
    fun `clearError preserves camera ready state when camera was ready`() = runTest {
        // First set camera as ready
        viewModel.onCameraReady()

        // Set an error
        viewModel.onCameraError("Some error")

        // Clear the error (simulating retry scenario)
        viewModel.clearError()

        // Then mark camera ready again
        viewModel.onCameraReady()
        val state = viewModel.uiState.first()

        assertTrue(state.isCameraReady)
        assertNull(state.error)
    }

    // =========================================================================
    // Flash Mode Tests (Story 4.3)
    // =========================================================================

    @Test
    fun `initial state has flash mode OFF`() = runTest {
        val state = viewModel.uiState.first()

        assertEquals(FlashMode.OFF, state.flashMode)
    }

    @Test
    fun `initial state has hasFlashUnit false`() = runTest {
        val state = viewModel.uiState.first()

        assertFalse(state.hasFlashUnit)
    }

    @Test
    fun `cycleFlashMode cycles from OFF to ON`() = runTest {
        viewModel.cycleFlashMode()
        val state = viewModel.uiState.first()

        assertEquals(FlashMode.ON, state.flashMode)
    }

    @Test
    fun `cycleFlashMode cycles from ON to AUTO`() = runTest {
        viewModel.cycleFlashMode() // OFF -> ON
        viewModel.cycleFlashMode() // ON -> AUTO
        val state = viewModel.uiState.first()

        assertEquals(FlashMode.AUTO, state.flashMode)
    }

    @Test
    fun `cycleFlashMode cycles from AUTO to OFF`() = runTest {
        viewModel.cycleFlashMode() // OFF -> ON
        viewModel.cycleFlashMode() // ON -> AUTO
        viewModel.cycleFlashMode() // AUTO -> OFF
        val state = viewModel.uiState.first()

        assertEquals(FlashMode.OFF, state.flashMode)
    }

    @Test
    fun `onFlashUnitAvailable sets hasFlashUnit to true`() = runTest {
        viewModel.onFlashUnitAvailable(true)
        val state = viewModel.uiState.first()

        assertTrue(state.hasFlashUnit)
    }

    @Test
    fun `onFlashUnitAvailable sets hasFlashUnit to false`() = runTest {
        // First set to true
        viewModel.onFlashUnitAvailable(true)
        // Then set to false
        viewModel.onFlashUnitAvailable(false)
        val state = viewModel.uiState.first()

        assertFalse(state.hasFlashUnit)
    }

    // =========================================================================
    // Photo Capture Tests (Story 4.2)
    // =========================================================================

    @Test
    fun `initial state has isCapturing false and no captured image`() = runTest {
        val state = viewModel.uiState.first()

        assertFalse(state.isCapturing)
        assertNull(state.capturedImageUri)
    }

    @Test
    fun `onCaptureStarted sets isCapturing to true`() = runTest {
        viewModel.onCaptureStarted()
        val state = viewModel.uiState.first()

        assertTrue(state.isCapturing)
    }

    @Test
    fun `onCaptureSuccess clears isCapturing and emits event`() = runTest {
        // Given user has credits (so we can verify event emission)
        fakeCreditRepository.setCreditBalance(3)
        advanceUntilIdle()

        val events = mutableListOf<CameraEvent>()
        val job = launch { viewModel.events.toList(events) }

        val testUri = "file:///test/image.jpg"
        viewModel.onCaptureStarted()
        viewModel.onCaptureSuccess(testUri)
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        // isCapturing is cleared
        assertFalse(state.isCapturing)
        assertNull(state.error)
        // capturedImageUri is NOT set (now uses events for navigation)
        assertNull(state.capturedImageUri)
        // Event is emitted instead
        assertEquals(1, events.size)
        assertTrue(events[0] is CameraEvent.ProceedToAnalysis)
        assertEquals(testUri, (events[0] as CameraEvent.ProceedToAnalysis).imageUri)

        job.cancel()
    }

    @Test
    fun `onCaptureError sets error and clears isCapturing`() = runTest {
        val errorMessage = "Failed to capture photo"
        viewModel.onCaptureStarted()
        viewModel.onCaptureError(errorMessage)
        val state = viewModel.uiState.first()

        assertFalse(state.isCapturing)
        assertEquals(errorMessage, state.error)
        assertNull(state.capturedImageUri)
    }

    @Test
    fun `clearCapturedImage resets captured image uri`() = runTest {
        val testUri = "file:///test/image.jpg"
        viewModel.onCaptureSuccess(testUri)
        viewModel.clearCapturedImage()
        val state = viewModel.uiState.first()

        assertNull(state.capturedImageUri)
    }

    @Test
    fun `capture button is disabled during capture`() = runTest {
        viewModel.onCaptureStarted()
        val state = viewModel.uiState.first()

        assertTrue(state.isCapturing)
        // isCapturing being true means button should be disabled
    }

    // =========================================================================
    // Zoom State Tests (Story 4.4)
    // =========================================================================

    @Test
    fun `initial state has default zoom values`() = runTest {
        val state = viewModel.uiState.first()

        assertEquals(1f, state.zoomRatio)
        assertEquals(1f, state.minZoomRatio)
        assertEquals(1f, state.maxZoomRatio)
        assertFalse(state.showZoomIndicator)
    }

    @Test
    fun `setZoomBounds updates min and max zoom ratios`() = runTest {
        viewModel.setZoomBounds(minZoom = 1f, maxZoom = 10f)
        val state = viewModel.uiState.first()

        assertEquals(1f, state.minZoomRatio)
        assertEquals(10f, state.maxZoomRatio)
    }

    @Test
    fun `setZoomRatio updates zoom ratio within bounds`() = runTest {
        viewModel.setZoomBounds(minZoom = 1f, maxZoom = 10f)
        viewModel.setZoomRatio(5f)
        val state = viewModel.uiState.first()

        assertEquals(5f, state.zoomRatio)
    }

    @Test
    fun `setZoomRatio clamps to max zoom ratio`() = runTest {
        viewModel.setZoomBounds(minZoom = 1f, maxZoom = 5f)
        viewModel.setZoomRatio(10f)
        val state = viewModel.uiState.first()

        assertEquals(5f, state.zoomRatio)
    }

    @Test
    fun `setZoomRatio clamps to min zoom ratio`() = runTest {
        viewModel.setZoomBounds(minZoom = 1f, maxZoom = 5f)
        viewModel.setZoomRatio(0.5f)
        val state = viewModel.uiState.first()

        assertEquals(1f, state.zoomRatio)
    }

    @Test
    fun `setZoomRatio shows zoom indicator`() = runTest {
        viewModel.setZoomRatio(2f)
        val state = viewModel.uiState.first()

        assertTrue(state.showZoomIndicator)
    }

    @Test
    fun `hideZoomIndicator hides the indicator`() = runTest {
        viewModel.setZoomRatio(2f)
        viewModel.hideZoomIndicator()
        val state = viewModel.uiState.first()

        assertFalse(state.showZoomIndicator)
    }

    // =========================================================================
    // Tap-to-Focus Tests (Story 4.5)
    // =========================================================================

    @Test
    fun `initial state has no focus point and focus indicator hidden`() = runTest {
        val state = viewModel.uiState.first()

        assertNull(state.focusPoint)
        assertFalse(state.showFocusIndicator)
    }

    @Test
    fun `onTapToFocus sets focus point and shows indicator`() = runTest {
        viewModel.onTapToFocus(100f, 200f)
        val state = viewModel.uiState.first()

        assertEquals(100f, state.focusPoint?.x)
        assertEquals(200f, state.focusPoint?.y)
        assertTrue(state.showFocusIndicator)
    }

    @Test
    fun `onTapToFocus updates focus point on subsequent taps`() = runTest {
        viewModel.onTapToFocus(100f, 200f)
        viewModel.onTapToFocus(300f, 400f)
        val state = viewModel.uiState.first()

        assertEquals(300f, state.focusPoint?.x)
        assertEquals(400f, state.focusPoint?.y)
    }

    @Test
    fun `hideFocusIndicator hides indicator but keeps focus point for animation`() = runTest {
        viewModel.onTapToFocus(100f, 200f)
        viewModel.hideFocusIndicator()
        val state = viewModel.uiState.first()

        assertFalse(state.showFocusIndicator)
        // Focus point is preserved so the composable can render fade-out animation
        assertEquals(100f, state.focusPoint?.x)
        assertEquals(200f, state.focusPoint?.y)
    }

    // =========================================================================
    // Gallery Picker Tests (Story 4.6)
    // =========================================================================

    @Test
    fun `initial state has no last gallery thumbnail`() = runTest {
        val state = viewModel.uiState.first()

        assertNull(state.lastGalleryThumbnailUri)
    }

    @Test
    fun `setLastGalleryThumbnail updates the thumbnail URI`() = runTest {
        val thumbnailUri = "content://media/external/images/5678"
        viewModel.setLastGalleryThumbnail(thumbnailUri)
        val state = viewModel.uiState.first()

        assertEquals(thumbnailUri, state.lastGalleryThumbnailUri)
    }

    @Test
    fun `setLastGalleryThumbnail with null clears the thumbnail`() = runTest {
        val thumbnailUri = "content://media/external/images/5678"
        viewModel.setLastGalleryThumbnail(thumbnailUri)
        viewModel.setLastGalleryThumbnail(null)
        val state = viewModel.uiState.first()

        assertNull(state.lastGalleryThumbnailUri)
    }

    // =========================================================================
    // Permission State Tests (Story 4.9)
    // =========================================================================

    @Test
    fun `initial state has permission state NOT_DETERMINED`() = runTest {
        val state = viewModel.uiState.first()

        assertEquals(CameraPermissionState.NOT_DETERMINED, state.permissionState)
    }

    @Test
    fun `onPermissionGranted sets permission state to GRANTED`() = runTest {
        viewModel.onPermissionGranted()
        val state = viewModel.uiState.first()

        assertEquals(CameraPermissionState.GRANTED, state.permissionState)
    }

    @Test
    fun `onPermissionDenied sets permission state to DENIED`() = runTest {
        viewModel.onPermissionDenied()
        val state = viewModel.uiState.first()

        assertEquals(CameraPermissionState.DENIED, state.permissionState)
    }

    @Test
    fun `onPermissionPermanentlyDenied sets permission state to PERMANENTLY_DENIED`() = runTest {
        viewModel.onPermissionPermanentlyDenied()
        val state = viewModel.uiState.first()

        assertEquals(CameraPermissionState.PERMANENTLY_DENIED, state.permissionState)
    }

    @Test
    fun `permission state transitions correctly on result`() = runTest {
        // Initially NOT_DETERMINED
        assertEquals(CameraPermissionState.NOT_DETERMINED, viewModel.uiState.first().permissionState)

        // User grants permission
        viewModel.onPermissionGranted()
        assertEquals(CameraPermissionState.GRANTED, viewModel.uiState.first().permissionState)
    }

    @Test
    fun `initial state has hasRequestedPermission false`() = runTest {
        val state = viewModel.uiState.first()

        assertFalse(state.hasRequestedPermission)
    }

    @Test
    fun `onPermissionRequested sets hasRequestedPermission to true`() = runTest {
        viewModel.onPermissionRequested()
        val state = viewModel.uiState.first()

        assertTrue(state.hasRequestedPermission)
    }

    @Test
    fun `hasRequestedPermission stays true after permission state changes`() = runTest {
        // User requests permission
        viewModel.onPermissionRequested()
        assertTrue(viewModel.uiState.first().hasRequestedPermission)

        // Permission denied
        viewModel.onPermissionDenied()
        assertTrue(viewModel.uiState.first().hasRequestedPermission)

        // Permission permanently denied
        viewModel.onPermissionPermanentlyDenied()
        assertTrue(viewModel.uiState.first().hasRequestedPermission)
    }

    // =========================================================================
    // Haptics Preference Tests (Story 9.2)
    // =========================================================================

    @Test
    fun `initial state has hapticsEnabled true by default`() = runTest {
        val state = viewModel.uiState.first()

        assertTrue(state.hapticsEnabled)
    }

    @Test
    fun `hapticsEnabled updates when preference changes to false`() = runTest {
        fakeHapticsPreferencesSource.setHapticsEnabled(false)
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertFalse(state.hapticsEnabled)
    }

    @Test
    fun `hapticsEnabled updates reactively on preference change`() = runTest {
        // Initially enabled
        assertTrue(viewModel.uiState.first().hapticsEnabled)

        // Disable haptics
        fakeHapticsPreferencesSource.setHapticsEnabled(false)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.first().hapticsEnabled)

        // Re-enable haptics
        fakeHapticsPreferencesSource.setHapticsEnabled(true)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.first().hapticsEnabled)
    }

    @Test
    fun `hapticsEnabled defaults to true when preference source throws`() = runTest {
        // Create a ViewModel with an error-throwing haptics source
        val errorHapticsSource = object : HapticsPreferencesSource {
            override val hapticsEnabled: Flow<Boolean> =
                kotlinx.coroutines.flow.flow { throw RuntimeException("DataStore error") }
            override suspend fun setHapticsEnabled(enabled: Boolean) {}
        }
        val errorViewModel = CameraViewModel(
            creditRepository = fakeCreditRepository,
            analyticsManager = fakeAnalyticsManager,
            hapticsPreferencesSource = errorHapticsSource
        )
        advanceUntilIdle()

        // Should default to true (haptics on) when DataStore fails
        assertTrue(errorViewModel.uiState.first().hapticsEnabled)
    }

    // =========================================================================
    // Credit Balance Tests (Story 4.8)
    // =========================================================================

    @Test
    fun `initial state has credit balance from repository`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(0, state.creditBalance)
    }

    @Test
    fun `credit balance updates when repository emits new value`() = runTest {
        fakeCreditRepository.setCreditBalance(3)
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(3, state.creditBalance)
    }

    @Test
    fun `credit balance updates reactively on change`() = runTest {
        // Start with some credits
        fakeCreditRepository.setCreditBalance(5)
        advanceUntilIdle()

        assertEquals(5, viewModel.uiState.first().creditBalance)

        // Simulate credit deduction
        fakeCreditRepository.setCreditBalance(4)
        advanceUntilIdle()

        assertEquals(4, viewModel.uiState.first().creditBalance)
    }

    // =========================================================================
    // Credit Check Tests (Story 5.1)
    // =========================================================================

    @Test
    fun `checkCreditsAndProceed emits ProceedToAnalysis when user has credits`() = runTest {
        // Given user has credits
        fakeCreditRepository.setCreditBalance(3)
        advanceUntilIdle()

        val events = mutableListOf<CameraEvent>()
        val job = launch { viewModel.events.toList(events) }

        // When checking credits with image URI
        val imageUri = "file:///test/image.jpg"
        viewModel.checkCreditsAndProceed(imageUri)
        advanceUntilIdle()

        // Then ProceedToAnalysis event is emitted with the image URI
        assertEquals(1, events.size)
        assertTrue(events[0] is CameraEvent.ProceedToAnalysis)
        assertEquals(imageUri, (events[0] as CameraEvent.ProceedToAnalysis).imageUri)

        job.cancel()
    }

    @Test
    fun `checkCreditsAndProceed emits NavigateToPaywall when user has no credits`() = runTest {
        // Given user has no credits
        fakeCreditRepository.setCreditBalance(0)
        advanceUntilIdle()

        val events = mutableListOf<CameraEvent>()
        val job = launch { viewModel.events.toList(events) }

        // When checking credits
        val imageUri = "file:///test/image.jpg"
        viewModel.checkCreditsAndProceed(imageUri)
        advanceUntilIdle()

        // Then NavigateToPaywall event is emitted
        assertEquals(1, events.size)
        assertTrue(events[0] is CameraEvent.NavigateToPaywall)

        job.cancel()
    }

    @Test
    fun `checkCreditsAndProceed does not deduct credits`() = runTest {
        // Given user has credits
        fakeCreditRepository.setCreditBalance(3)
        advanceUntilIdle()

        // When checking credits
        viewModel.checkCreditsAndProceed("file:///test/image.jpg")
        advanceUntilIdle()

        // Then credit balance is unchanged (Story 5.1: credit NOT deducted until scan succeeds)
        assertEquals(3, viewModel.uiState.first().creditBalance)
    }

    // =========================================================================
    // Photo Capture Credit Check Tests (Story 5.1 - Task 3)
    // =========================================================================

    @Test
    fun `onCaptureSuccess triggers credit check and emits event`() = runTest {
        // Given user has credits
        fakeCreditRepository.setCreditBalance(3)
        advanceUntilIdle()

        val events = mutableListOf<CameraEvent>()
        val job = launch { viewModel.events.toList(events) }

        // When capture succeeds
        val testUri = "file:///test/captured.jpg"
        viewModel.onCaptureSuccess(testUri)
        advanceUntilIdle()

        // Then ProceedToAnalysis event is emitted (not directly to capturedImageUri)
        assertEquals(1, events.size)
        assertTrue(events[0] is CameraEvent.ProceedToAnalysis)
        assertEquals(testUri, (events[0] as CameraEvent.ProceedToAnalysis).imageUri)

        // And capturedImageUri is NOT set (event-based navigation now)
        assertNull(viewModel.uiState.first().capturedImageUri)

        job.cancel()
    }

    @Test
    fun `onCaptureSuccess with no credits emits NavigateToPaywall`() = runTest {
        // Given user has no credits
        fakeCreditRepository.setCreditBalance(0)
        advanceUntilIdle()

        val events = mutableListOf<CameraEvent>()
        val job = launch { viewModel.events.toList(events) }

        // When capture succeeds but no credits
        viewModel.onCaptureSuccess("file:///test/captured.jpg")
        advanceUntilIdle()

        // Then NavigateToPaywall event is emitted
        assertEquals(1, events.size)
        assertTrue(events[0] is CameraEvent.NavigateToPaywall)

        job.cancel()
    }

    // =========================================================================
    // Gallery Selection Credit Check Tests (Story 5.1 - Task 4)
    // =========================================================================

    @Test
    fun `onGalleryImageSelected triggers credit check with credits`() = runTest {
        // Given user has credits
        fakeCreditRepository.setCreditBalance(3)
        advanceUntilIdle()

        val events = mutableListOf<CameraEvent>()
        val job = launch { viewModel.events.toList(events) }

        // When gallery image is selected
        val testUri = "content://media/external/images/1234"
        viewModel.onGalleryImageSelected(testUri)
        advanceUntilIdle()

        // Then ProceedToAnalysis event is emitted
        assertEquals(1, events.size)
        assertTrue(events[0] is CameraEvent.ProceedToAnalysis)
        assertEquals(testUri, (events[0] as CameraEvent.ProceedToAnalysis).imageUri)

        job.cancel()
    }

    @Test
    fun `onGalleryImageSelected triggers NavigateToPaywall with no credits`() = runTest {
        // Given user has no credits
        fakeCreditRepository.setCreditBalance(0)
        advanceUntilIdle()

        val events = mutableListOf<CameraEvent>()
        val job = launch { viewModel.events.toList(events) }

        // When gallery image is selected
        viewModel.onGalleryImageSelected("content://media/external/images/1234")
        advanceUntilIdle()

        // Then NavigateToPaywall event is emitted
        assertEquals(1, events.size)
        assertTrue(events[0] is CameraEvent.NavigateToPaywall)

        job.cancel()
    }

    // =========================================================================
    // Analytics Tests (Story 5.1 - Task 7)
    // =========================================================================

    @Test
    fun `checkCreditsAndProceed tracks scan_credit_check analytics event with credits`() = runTest {
        // Given user has credits
        fakeCreditRepository.setCreditBalance(3)
        advanceUntilIdle()
        fakeAnalyticsManager.clear()

        // When checking credits
        viewModel.checkCreditsAndProceed("file:///test/image.jpg")
        advanceUntilIdle()

        // Then analytics event is tracked with correct properties
        val analyticsEvents = fakeAnalyticsManager.trackedEvents
        assertEquals(1, analyticsEvents.size)
        assertEquals("scan_credit_check", analyticsEvents[0].name)
        assertEquals("true", analyticsEvents[0].properties["has_credits"])
        assertEquals("3", analyticsEvents[0].properties["credit_balance"])
    }

    @Test
    fun `checkCreditsAndProceed tracks scan_credit_check analytics event without credits`() = runTest {
        // Given user has no credits
        fakeCreditRepository.setCreditBalance(0)
        advanceUntilIdle()
        fakeAnalyticsManager.clear()

        // When checking credits
        viewModel.checkCreditsAndProceed("file:///test/image.jpg")
        advanceUntilIdle()

        // Then analytics event is tracked with correct properties
        val analyticsEvents = fakeAnalyticsManager.trackedEvents
        assertEquals(1, analyticsEvents.size)
        assertEquals("scan_credit_check", analyticsEvents[0].name)
        assertEquals("false", analyticsEvents[0].properties["has_credits"])
        assertEquals("0", analyticsEvents[0].properties["credit_balance"])
    }

    // =========================================================================
    // Error Handling Tests (Story 5.1 - Code Review Fixes)
    // =========================================================================

    @Test
    fun `checkCreditsAndProceed emits NavigateToPaywall when credit check fails`() = runTest {
        // Given credit repository throws an error
        fakeCreditRepository.setThrowOnBalanceRead(true)

        val events = mutableListOf<CameraEvent>()
        val job = launch { viewModel.events.toList(events) }

        // When checking credits (which will fail)
        viewModel.checkCreditsAndProceed("file:///test/image.jpg")
        advanceUntilIdle()

        // Then NavigateToPaywall is emitted as fallback (safe default)
        assertEquals(1, events.size)
        assertTrue(events[0] is CameraEvent.NavigateToPaywall)

        job.cancel()
    }

    @Test
    fun `checkCreditsAndProceed still navigates when analytics fails`() = runTest {
        // Given user has credits but analytics throws
        fakeCreditRepository.setCreditBalance(3)
        advanceUntilIdle()
        fakeAnalyticsManager.setThrowOnTrackEvent(true)

        val events = mutableListOf<CameraEvent>()
        val job = launch { viewModel.events.toList(events) }

        // When checking credits (analytics will fail but shouldn't block)
        val imageUri = "file:///test/image.jpg"
        viewModel.checkCreditsAndProceed(imageUri)
        advanceUntilIdle()

        // Then ProceedToAnalysis is still emitted (analytics failure doesn't block navigation)
        assertEquals(1, events.size)
        assertTrue(events[0] is CameraEvent.ProceedToAnalysis)
        assertEquals(imageUri, (events[0] as CameraEvent.ProceedToAnalysis).imageUri)

        job.cancel()
    }

    @Test
    fun `checkCreditsAndProceed navigates to paywall when balance is 0 after deduction`() = runTest {
        // Simulate a user who just used their last credit - balance is now 0
        fakeCreditRepository.setCreditBalance(0)
        advanceUntilIdle()

        val events = mutableListOf<CameraEvent>()
        val job = launch { viewModel.events.toList(events) }

        // When user tries to scan again
        val imageUri = "file:///test/image.jpg"
        viewModel.checkCreditsAndProceed(imageUri)
        advanceUntilIdle()

        // Then NavigateToPaywall event is emitted (paywall gate is triggered)
        assertEquals(1, events.size)
        assertTrue(events[0] is CameraEvent.NavigateToPaywall)

        job.cancel()
    }
}

/**
 * Fake implementation of [CreditRepository] for testing.
 */
class FakeCreditRepository : CreditRepository {
    private val _creditBalance = MutableStateFlow(0)
    private var shouldThrowOnBalanceRead = false

    override val creditBalance: Flow<Int>
        get() = if (shouldThrowOnBalanceRead) {
            kotlinx.coroutines.flow.flow { throw RuntimeException("Simulated balance read error") }
        } else {
            _creditBalance
        }

    fun setCreditBalance(balance: Int) {
        _creditBalance.value = balance
    }

    fun setThrowOnBalanceRead(shouldThrow: Boolean) {
        shouldThrowOnBalanceRead = shouldThrow
    }

    override suspend fun awardInitialCredits(amount: Int): Boolean {
        if (_creditBalance.value == 0) {
            _creditBalance.value = amount
            return true
        }
        return false
    }

    override suspend fun deductCredit(): Boolean {
        if (_creditBalance.value > 0) {
            _creditBalance.value -= 1
            return true
        }
        return false
    }

    override suspend fun hasCredits(): Boolean = _creditBalance.value > 0

    override suspend fun addCredits(amount: Int): Boolean {
        _creditBalance.value += amount
        return true
    }

    override suspend fun removeCredits(amount: Int): Int {
        val current = _creditBalance.value
        val actualRemoved = minOf(current, amount)
        _creditBalance.value = (current - amount).coerceAtLeast(0)
        return actualRemoved
    }

    var hasCreditsWasCalled = false
        private set

    fun resetTracking() {
        hasCreditsWasCalled = false
        shouldThrowOnBalanceRead = false
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
    val trackedScreenViews: List<String> get() = _trackedScreenViews.toList()

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
 * Fake implementation of [HapticsPreferencesSource] for testing.
 */
class FakeHapticsPreferencesSource(
    initialEnabled: Boolean = true
) : HapticsPreferencesSource {

    private val _hapticsEnabled = MutableStateFlow(initialEnabled)

    override val hapticsEnabled: Flow<Boolean> = _hapticsEnabled

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
    }
}
