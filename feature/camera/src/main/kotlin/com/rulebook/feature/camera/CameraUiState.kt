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
 */
data class CameraUiState(
    val isCameraReady: Boolean = false,
    val error: String? = null,
    val flashMode: FlashMode = FlashMode.OFF,
    val hasFlashUnit: Boolean = false
)
