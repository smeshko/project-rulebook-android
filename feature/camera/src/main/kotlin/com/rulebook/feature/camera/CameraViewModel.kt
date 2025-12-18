package com.rulebook.feature.camera

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the Camera screen.
 *
 * Manages the camera state and provides functions to update it.
 * Follows the MVI pattern with [CameraUiState] as the single source of truth.
 *
 * The ViewModel handles:
 * - Camera initialization state tracking
 * - Error state management
 *
 * Camera operations (binding, unbinding) are handled by the CameraPreview composable
 * using CameraX's lifecycle integration, but state changes are reported back to this
 * ViewModel for UI updates.
 */
class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())

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
}
