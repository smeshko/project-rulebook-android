package com.rulebook.feature.camera

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.data.repository.CreditRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "CameraViewModel"

/**
 * ViewModel for the Camera screen.
 *
 * Manages the camera state and provides functions to update it.
 * Follows the MVI pattern with [CameraUiState] as the single source of truth.
 *
 * The ViewModel handles:
 * - Camera initialization state tracking
 * - Error state management
 * - Flash mode control
 * - Photo capture state management
 * - Zoom level management (zoom ratio, bounds, indicator visibility)
 * - Credit balance observation (Story 4.8)
 *
 * Camera operations (binding, unbinding) are handled by the CameraPreview composable
 * using CameraX's lifecycle integration, but state changes are reported back to this
 * ViewModel for UI updates.
 *
 * @param creditRepository Repository for observing credit balance.
 * @param analyticsManager Manager for tracking analytics events (Story 5.1).
 */
class CameraViewModel(
    private val creditRepository: CreditRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())

    /**
     * Channel for one-time navigation events.
     *
     * Using Channel with BUFFERED capacity ensures events aren't lost if emitted
     * before the collector is ready. Events are consumed exactly once.
     */
    private val _events = Channel<CameraEvent>(Channel.BUFFERED)

    /**
     * Flow of one-time navigation events for the UI to collect.
     *
     * Use this for navigation actions that should only be handled once,
     * such as navigating to paywall or proceeding to image analysis.
     */
    val events: Flow<CameraEvent> = _events.receiveAsFlow()

    init {
        // Observe credit balance changes and update UI state (Story 4.8)
        // catch emits 0 on DataStore IOException to keep UI functional
        creditRepository.creditBalance
            .catch { emit(0) }
            .onEach { balance ->
                _uiState.update { it.copy(creditBalance = balance) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * The current UI state of the camera screen.
     */
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    /**
     * Called when the camera preview becomes ready and starts displaying frames.
     */
    fun onCameraReady() {
        _uiState.update { it.copy(isCameraReady = true, error = null) }
    }

    /**
     * Called when an error occurs during camera initialization or operation.
     *
     * @param message The error message to display to the user.
     */
    fun onCameraError(message: String) {
        _uiState.update { it.copy(isCameraReady = false, error = message) }
    }

    /**
     * Clears any existing error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // =========================================================================
    // Credit Check (Story 5.1)
    // =========================================================================

    /**
     * Checks if the user has credits and emits the appropriate navigation event.
     *
     * If the user has credits, emits [CameraEvent.ProceedToAnalysis] with the image URI.
     * If the user has no credits, emits [CameraEvent.NavigateToPaywall].
     *
     * Also tracks the "scan_credit_check" analytics event with credit status.
     *
     * Note: This method does NOT deduct credits. Credit deduction happens only
     * when the scan completes successfully (Story 5.7).
     *
     * @param imageUri The URI of the captured or selected image.
     */
    fun checkCreditsAndProceed(imageUri: String) {
        viewModelScope.launch {
            try {
                // Read credit balance from repository for consistent analytics data
                val creditBalance = creditRepository.creditBalance.first()
                val hasCredits = creditBalance > 0

                // Track analytics event for credit check (Story 5.1)
                // Analytics is wrapped separately so it doesn't block navigation
                try {
                    analyticsManager.trackScanCreditCheck(hasCredits, creditBalance)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to track scan credit check analytics", e)
                }

                if (hasCredits) {
                    _events.send(CameraEvent.ProceedToAnalysis(imageUri))
                } else {
                    _events.send(CameraEvent.NavigateToPaywall)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check credits", e)
                // Fallback: navigate to paywall on error (safe default)
                _events.send(CameraEvent.NavigateToPaywall)
            }
        }
    }

    // =========================================================================
    // Flash Mode Control (Story 4.3)
    // =========================================================================

    /**
     * Cycles the flash mode to the next state.
     * Cycle order: OFF → ON → AUTO → OFF
     */
    fun cycleFlashMode() {
        _uiState.update { it.copy(flashMode = it.flashMode.next()) }
    }

    /**
     * Updates whether the device has a flash unit available.
     *
     * @param hasFlash True if the device has a flash unit.
     */
    fun onFlashUnitAvailable(hasFlash: Boolean) {
        _uiState.update { it.copy(hasFlashUnit = hasFlash) }
    }

    // =========================================================================
    // Photo Capture (Story 4.2)
    // =========================================================================

    /**
     * Called when photo capture starts.
     * Sets isCapturing to true to disable the capture button and prevent double-tap.
     */
    fun onCaptureStarted() {
        _uiState.update { it.copy(isCapturing = true) }
    }

    /**
     * Called when photo capture completes successfully.
     *
     * Checks if the user has credits before proceeding:
     * - If credits > 0: emits [CameraEvent.ProceedToAnalysis]
     * - If credits = 0: emits [CameraEvent.NavigateToPaywall]
     *
     * Note: Credits are NOT deducted here. Deduction happens only on successful
     * rules generation (Story 5.7).
     *
     * @param imageUri The URI of the captured image file.
     */
    fun onCaptureSuccess(imageUri: String) {
        _uiState.update {
            it.copy(
                isCapturing = false,
                error = null
            )
        }
        // Check credits and emit appropriate navigation event (Story 5.1)
        checkCreditsAndProceed(imageUri)
    }

    /**
     * Called when photo capture fails.
     *
     * @param message The error message describing the failure.
     */
    fun onCaptureError(message: String) {
        _uiState.update {
            it.copy(
                isCapturing = false,
                error = message
            )
        }
    }

    /**
     * Clears the captured image URI.
     * Called after the image has been passed to the processing flow.
     */
    fun clearCapturedImage() {
        _uiState.update { it.copy(capturedImageUri = null) }
    }

    // =========================================================================
    // Zoom Control (Story 4.4)
    // =========================================================================

    /**
     * Sets the zoom bounds supported by the camera.
     *
     * Called after camera initialization when zoom state becomes available.
     *
     * @param minZoom Minimum zoom ratio (typically 1.0f).
     * @param maxZoom Maximum zoom ratio supported by the device camera.
     */
    fun setZoomBounds(minZoom: Float, maxZoom: Float) {
        _uiState.update { it.copy(minZoomRatio = minZoom, maxZoomRatio = maxZoom) }
    }

    /**
     * Sets the current zoom ratio.
     *
     * The value is clamped to the min/max bounds. Also shows the zoom indicator.
     *
     * @param ratio The desired zoom ratio.
     */
    fun setZoomRatio(ratio: Float) {
        _uiState.update { state ->
            val clampedRatio = ratio.coerceIn(state.minZoomRatio, state.maxZoomRatio)
            state.copy(zoomRatio = clampedRatio, showZoomIndicator = true)
        }
    }

    /**
     * Hides the zoom indicator overlay.
     *
     * Called after a delay when zoom gesture ends to auto-hide the indicator.
     */
    fun hideZoomIndicator() {
        _uiState.update { it.copy(showZoomIndicator = false) }
    }

    // =========================================================================
    // Tap-to-Focus (Story 4.5)
    // =========================================================================

    /**
     * Called when the user taps on the camera preview to focus.
     *
     * Sets the focus point and shows the focus indicator.
     *
     * @param x The x-coordinate of the tap in pixels.
     * @param y The y-coordinate of the tap in pixels.
     */
    fun onTapToFocus(x: Float, y: Float) {
        _uiState.update {
            it.copy(
                focusPoint = FocusPoint(x, y),
                showFocusIndicator = true
            )
        }
    }

    /**
     * Hides the focus indicator.
     *
     * Called after the focus indicator display duration completes.
     * Note: We only clear showFocusIndicator here, keeping focusPoint so the
     * composable can still render during the fade-out animation. The focusPoint
     * will be replaced on the next tap, or naturally cleaned up.
     */
    fun hideFocusIndicator() {
        _uiState.update {
            it.copy(showFocusIndicator = false)
        }
    }

    // =========================================================================
    // Gallery Picker (Story 4.6)
    // =========================================================================

    /**
     * Sets the URI of the last photo in the device gallery.
     * Used to display a thumbnail preview on the gallery button.
     *
     * @param uri The URI of the last gallery photo, or null if unavailable.
     */
    fun setLastGalleryThumbnail(uri: String?) {
        _uiState.update { it.copy(lastGalleryThumbnailUri = uri) }
    }

    /**
     * Called when a gallery image is selected by the user.
     *
     * Performs the same credit check as photo capture:
     * - If credits > 0: emits [CameraEvent.ProceedToAnalysis]
     * - If credits = 0: emits [CameraEvent.NavigateToPaywall]
     *
     * Note: Credits are NOT deducted here. Deduction happens only on successful
     * rules generation (Story 5.7).
     *
     * @param imageUri The URI of the selected gallery image.
     */
    fun onGalleryImageSelected(imageUri: String) {
        // Same flow as captured photos - check credits before proceeding (Story 5.1)
        checkCreditsAndProceed(imageUri)
    }

    // =========================================================================
    // Permission Handling (Story 4.9)
    // =========================================================================

    /**
     * Called when camera permission has been granted.
     */
    fun onPermissionGranted() {
        _uiState.update { it.copy(permissionState = CameraPermissionState.GRANTED) }
    }

    /**
     * Called when camera permission was denied.
     * This indicates the user denied but can still be asked again.
     */
    fun onPermissionDenied() {
        _uiState.update { it.copy(permissionState = CameraPermissionState.DENIED) }
    }

    /**
     * Called when camera permission was permanently denied.
     * This indicates the user selected "Don't ask again" and must go to settings to grant.
     */
    fun onPermissionPermanentlyDenied() {
        _uiState.update { it.copy(permissionState = CameraPermissionState.PERMANENTLY_DENIED) }
    }

    /**
     * Called when the permission request dialog is launched.
     * This helps distinguish first-time users from permanently denied users.
     */
    fun onPermissionRequested() {
        _uiState.update { it.copy(hasRequestedPermission = true) }
    }

    // =========================================================================
    // Resource Cleanup (Story 4.10)
    // =========================================================================

    /**
     * Called when the ViewModel is cleared (screen removed from composition).
     *
     * Logs cleanup for debugging purposes. The viewModelScope is automatically
     * cancelled by ViewModel, which stops the credit balance observation.
     * Camera resources are released by CameraPreview's DisposableEffect.
     */
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "CameraViewModel cleared - viewModelScope cancelled")
    }
}
