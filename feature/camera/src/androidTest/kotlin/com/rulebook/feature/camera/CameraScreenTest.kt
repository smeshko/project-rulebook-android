package com.rulebook.feature.camera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for CameraScreen composable.
 *
 * These tests verify the camera screen layout and UI states without requiring
 * actual camera hardware. Permission states are tested through the UI responses.
 *
 * Note: CameraPreview is not tested here as it requires actual camera hardware.
 * These tests focus on the permission UI states (rationale, denied).
 */
class CameraScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun permissionDenied_showsDeniedMessage() {
        // When permission is permanently denied, the denied message should be displayed
        // Note: This test verifies the UI renders correctly when in denied state
        // Actual permission state testing requires a more complex setup with mocked permissions

        composeTestRule.setContent {
            PermissionDenied()
        }

        composeTestRule
            .onNodeWithText("Camera Permission Denied")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Please enable camera permission in your device settings to use this feature.")
            .assertIsDisplayed()
    }

    @Test
    fun permissionRationale_showsRationaleMessage() {
        composeTestRule.setContent {
            PermissionRationale(onRequestPermission = {})
        }

        composeTestRule
            .onNodeWithText("Camera Permission Required")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("The camera is needed to capture photos of your game boxes for rule extraction.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Grant Permission")
            .assertIsDisplayed()
    }
}
