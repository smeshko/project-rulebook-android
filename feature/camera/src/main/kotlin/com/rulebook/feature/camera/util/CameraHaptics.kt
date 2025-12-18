package com.rulebook.feature.camera.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import com.rulebook.core.common.HapticUtils

/**
 * Composable function that provides a haptic feedback trigger for photo capture.
 *
 * Returns a lambda that, when invoked, performs haptic feedback
 * suitable for capture actions. The feedback automatically respects
 * system haptic settings.
 *
 * Usage:
 * ```kotlin
 * val hapticFeedback = rememberCaptureHapticFeedback()
 *
 * CaptureButton(onClick = {
 *     hapticFeedback()
 *     capturePhoto()
 * })
 * ```
 *
 * @return A lambda that performs capture haptic feedback when invoked.
 */
@Composable
fun rememberCaptureHapticFeedback(): () -> Unit {
    val view = LocalView.current
    return remember(view) {
        { HapticUtils.performCaptureHaptic(view) }
    }
}
