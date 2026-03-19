package com.rulebook.feature.camera.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import com.rulebook.core.common.HapticUtils

/**
 * Composable function that provides a haptic feedback trigger for photo capture.
 *
 * Returns a lambda that, when invoked, performs haptic feedback
 * suitable for capture actions, respecting the user's haptic preference.
 *
 * Usage:
 * ```kotlin
 * val hapticFeedback = rememberCaptureHapticFeedback(hapticsEnabled = uiState.hapticsEnabled)
 *
 * CaptureButton(onClick = {
 *     hapticFeedback()
 *     capturePhoto()
 * })
 * ```
 *
 * @param hapticsEnabled Whether haptic feedback is enabled per user preference.
 * @return A lambda that performs capture haptic feedback when invoked.
 */
@Composable
fun rememberCaptureHapticFeedback(hapticsEnabled: Boolean = true): () -> Unit {
    val view = LocalView.current
    return remember(view, hapticsEnabled) {
        { HapticUtils.performCaptureHaptic(view, hapticsEnabled) }
    }
}
