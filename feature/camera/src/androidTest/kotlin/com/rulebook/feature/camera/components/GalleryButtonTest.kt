package com.rulebook.feature.camera.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

/**
 * UI tests for GalleryButton composable.
 *
 * These tests verify the gallery button renders correctly and responds to clicks.
 */
class GalleryButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun galleryButton_isDisplayed() {
        composeTestRule.setContent {
            GalleryButton(onClick = {})
        }

        composeTestRule
            .onNodeWithContentDescription("Open gallery")
            .assertIsDisplayed()
    }

    @Test
    fun galleryButton_onClick_isInvoked() {
        var clicked = false

        composeTestRule.setContent {
            GalleryButton(onClick = { clicked = true })
        }

        composeTestRule
            .onNodeWithContentDescription("Open gallery")
            .performClick()

        assertTrue(clicked)
    }
}
