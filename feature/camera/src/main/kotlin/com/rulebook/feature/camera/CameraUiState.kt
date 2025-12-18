package com.rulebook.feature.camera

/**
 * UI state for the Camera screen.
 *
 * Represents the current state of the camera preview and any related UI elements.
 * This state class follows the MVI pattern and is exposed via StateFlow from
 * [CameraViewModel].
 *
 * @param isCameraReady True when the camera preview has been initialized and is ready.
 *                      Used to show loading state while camera initializes.
 * @param error Optional error message to display if camera initialization fails.
 * @param flashMode Current flash mode setting. Defaults to [FlashMode.OFF].
 * @param hasFlashUnit Whether the device has a flash unit available.
 *                     Used to conditionally show/hide the flash toggle.
 * @param isCapturing True when a photo capture is in progress. Used to disable the
 *                    capture button to prevent double-tap.
 * @param capturedImageUri The URI of the captured image, set after successful capture.
 *                         Used to pass the image to the processing flow.
 */
data class CameraUiState(
    val isCameraReady: Boolean = false,
    val error: String? = null,
    val flashMode: FlashMode = FlashMode.OFF,
    val hasFlashUnit: Boolean = false,
    val isCapturing: Boolean = false,
    val capturedImageUri: String? = null
)
