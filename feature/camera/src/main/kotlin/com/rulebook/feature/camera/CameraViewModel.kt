package com.rulebook.feature.camera

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.data.repository.CreditRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

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
 */
class CameraViewModel(
    creditRepository: CreditRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())

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
     * @param imageUri The URI of the captured image file.
     */
    fun onCaptureSuccess(imageUri: String) {
        _uiState.update {
            it.copy(
                isCapturing = false,
                capturedImageUri = imageUri,
                error = null
            )
        }
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
