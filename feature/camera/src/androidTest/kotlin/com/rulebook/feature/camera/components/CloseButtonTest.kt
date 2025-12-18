package com.rulebook.feature.camera.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for [CloseButton] composable.
 *
 * Tests verify:
 * - Close button displays correctly with proper icon and accessibility label
 * - Click callback is invoked when button is tapped
 *
 * These tests require an Android device or emulator to run as they use
 * the Compose UI testing framework.
 */
class CloseButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun closeButton_displaysWithCorrectContentDescription() {
        composeTestRule.setContent {
            CloseButton(onClick = {})
        }

        composeTestRule
            .onNodeWithContentDescription("Close camera")
            .assertIsDisplayed()
    }

    @Test
    fun closeButton_invokesCallbackOnClick() {
        var clicked = false

        composeTestRule.setContent {
            CloseButton(onClick = { clicked = true })
        }

        composeTestRule
            .onNodeWithContentDescription("Close camera")
            .performClick()

        assertTrue("Close button callback should be invoked", clicked)
    }
}
