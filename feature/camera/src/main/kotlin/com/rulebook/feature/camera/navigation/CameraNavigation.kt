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
 * Includes credit gate logic (Story 5.1):
 * - When image is captured/selected, checks credit balance
 * - If credits > 0, navigates to processing screen
 * - If credits = 0, navigates to paywall
 *
 * @param onNavigateBack Callback when user closes camera, returns to previous screen.
 * @param onNavigateToProcessing Callback when user has credits and scan should proceed.
 * @param onNavigateToPaywall Callback when user has no credits and needs to purchase.
 */
fun NavGraphBuilder.cameraScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToProcessing: (String) -> Unit = {},
    onNavigateToPaywall: () -> Unit = {}
) {
    composable(route = CAMERA_ROUTE) {
        CameraScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToProcessing = onNavigateToProcessing,
            onNavigateToPaywall = onNavigateToPaywall
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
