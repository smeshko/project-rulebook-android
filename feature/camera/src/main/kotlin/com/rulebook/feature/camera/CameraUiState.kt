package com.rulebook.feature.camera

/**
 * Represents the state of camera permission.
 *
 * Used to track permission status and determine which UI to display.
 */
enum class CameraPermissionState {
    /** Permission hasn't been checked yet */
    NOT_DETERMINED,
    /** Permission has been granted */
    GRANTED,
    /** Permission was denied but can ask again */
    DENIED,
    /** Permission was permanently denied ("Don't ask again" selected) */
    PERMANENTLY_DENIED
}

/**
 * Represents a point in 2D screen coordinates.
 *
 * Used to track tap locations for tap-to-focus functionality.
 *
 * @param x The horizontal coordinate in pixels.
 * @param y The vertical coordinate in pixels.
 */
data class FocusPoint(
    val x: Float,
    val y: Float
)

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
 * @param focusPoint The current tap-to-focus point, or null if no manual focus is set.
 * @param showFocusIndicator Whether to display the focus indicator at [focusPoint].
 * @param lastGalleryThumbnailUri The URI of the most recent photo in the device gallery.
 *                                 Displayed as a thumbnail preview on the gallery button (Story 4.6).
 * @param creditBalance The user's current credit balance for scans. Default is 0.
 * @param showPaywall Whether to display the paywall modal. Set to true when user has 0 credits
 *                    and attempts to initiate a scan (Story 5.1).
 * @param permissionState Current state of camera permission. Default is [CameraPermissionState.NOT_DETERMINED].
 * @param hasRequestedPermission True if permission request has been launched this session.
 *                               Used to distinguish first-time users from permanently denied.
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
    val focusPoint: FocusPoint? = null,
    val showFocusIndicator: Boolean = false,
    val lastGalleryThumbnailUri: String? = null,
    val creditBalance: Int = 0,
    val showPaywall: Boolean = false,
    val permissionState: CameraPermissionState = CameraPermissionState.NOT_DETERMINED,
    val hasRequestedPermission: Boolean = false
)
