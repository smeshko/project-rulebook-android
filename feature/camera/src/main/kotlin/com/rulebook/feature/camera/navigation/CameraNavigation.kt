package com.rulebook.feature.camera.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.rulebook.feature.camera.CameraScreen

/**
 * Route constant for camera navigation.
 */
const val CAMERA_ROUTE = "camera"

/**
 * Adds the camera screen to the navigation graph.
 *
 * The camera screen displays a full-screen CameraX preview with immersive mode
 * (hidden system bars). It is intended to be navigated to from the camera FAB.
 *
 * @param onPhotoCaptured Callback when a photo is captured, receives the image URI.
 * @param onGalleryImageSelected Callback when a gallery image is selected, receives the image URI.
 */
fun NavGraphBuilder.cameraScreen(
    onPhotoCaptured: (String) -> Unit = {},
    onGalleryImageSelected: (String) -> Unit = {}
) {
    composable(route = CAMERA_ROUTE) {
        CameraScreen(
            onPhotoCaptured = onPhotoCaptured,
            onGalleryImageSelected = onGalleryImageSelected
        )
    }
}

/**
 * Navigates to the camera screen.
 *
 * @param from Optional source route for analytics/logging.
 */
fun NavController.navigateToCamera(from: String? = null) {
    navigate(CAMERA_ROUTE)
}
