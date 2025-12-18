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
 * @param zoomRatio Current zoom level, ranging from [minZoomRatio] to [maxZoomRatio].
 *                  Default is 1.0f (no zoom).
 * @param minZoomRatio Minimum zoom ratio supported by the camera. Default is 1.0f.
 * @param maxZoomRatio Maximum zoom ratio supported by the camera. Default is 1.0f.
 * @param showZoomIndicator Whether to display the zoom level indicator overlay.
 * @param selectedGalleryImageUri The URI of an image selected from the gallery, if any.
 *                                Used to pass the image to the processing flow (Story 4.6).
 * @param lastGalleryThumbnailUri The URI of the most recent photo in the device gallery.
 *                                 Displayed as a thumbnail preview on the gallery button (Story 4.6).
 */
data class CameraUiState(
    val isCameraReady: Boolean = false,
    val error: String? = null,
    val flashMode: FlashMode = FlashMode.OFF,
    val hasFlashUnit: Boolean = false,
    val isCapturing: Boolean = false,
    val capturedImageUri: String? = null,
    val zoomRatio: Float = 1f,
    val minZoomRatio: Float = 1f,
    val maxZoomRatio: Float = 1f,
    val showZoomIndicator: Boolean = false,
    val selectedGalleryImageUri: String? = null,
    val lastGalleryThumbnailUri: String? = null
)
