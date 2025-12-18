package com.rulebook.feature.camera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.feature.camera.components.CaptureButton
import org.junit.Rule
import org.junit.Test

class CaptureButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun captureButton_isDisplayed() {
        composeTestRule.setContent {
            RulebookTheme {
                CaptureButton(onClick = {})
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Capture photo")
            .assertIsDisplayed()
    }

    @Test
    fun captureButton_isClickable_whenEnabled() {
        var clicked = false

        composeTestRule.setContent {
            RulebookTheme {
                CaptureButton(
                    onClick = { clicked = true },
                    enabled = true
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Capture photo")
            .assertIsEnabled()
            .performClick()

        assert(clicked) { "Button click callback was not invoked" }
    }

    @Test
    fun captureButton_isNotClickable_whenDisabled() {
        var clicked = false

        composeTestRule.setContent {
            RulebookTheme {
                CaptureButton(
                    onClick = { clicked = true },
                    enabled = false
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Capture photo")
            .assertIsNotEnabled()
            .performClick()

        assert(!clicked) { "Button click callback should not be invoked when disabled" }
    }
}
