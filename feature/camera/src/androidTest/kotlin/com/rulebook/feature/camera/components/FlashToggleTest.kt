package com.rulebook.feature.camera.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.rulebook.feature.camera.FlashMode
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

/**
 * UI tests for FlashToggle composable.
 *
 * Tests verify:
 * - Correct icons displayed for each flash mode
 * - Accessibility content descriptions
 * - Click handling
 */
class FlashToggleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun flashToggle_offMode_displaysCorrectContentDescription() {
        composeTestRule.setContent {
            FlashToggle(
                flashMode = FlashMode.OFF,
                onToggle = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Flash off. Tap to turn flash on.")
            .assertIsDisplayed()
    }

    @Test
    fun flashToggle_onMode_displaysCorrectContentDescription() {
        composeTestRule.setContent {
            FlashToggle(
                flashMode = FlashMode.ON,
                onToggle = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Flash on. Tap to set flash to auto.")
            .assertIsDisplayed()
    }

    @Test
    fun flashToggle_autoMode_displaysCorrectContentDescription() {
        composeTestRule.setContent {
            FlashToggle(
                flashMode = FlashMode.AUTO,
                onToggle = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Flash auto. Tap to turn flash off.")
            .assertIsDisplayed()
    }

    @Test
    fun flashToggle_onClick_callsCallback() {
        var clickCount = 0

        composeTestRule.setContent {
            FlashToggle(
                flashMode = FlashMode.OFF,
                onToggle = { clickCount++ }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Flash off. Tap to turn flash on.")
            .performClick()

        assertEquals(1, clickCount)
    }
}
