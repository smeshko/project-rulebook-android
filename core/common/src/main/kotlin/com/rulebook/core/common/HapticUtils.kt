package com.rulebook.core.common

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Provides haptic feedback functionality for capture actions.
 *
 * This utility handles:
 * - API level differences in vibration APIs (S+ vs older)
 * - Device capability detection (hasVibrator)
 * - System haptic settings respect via View.performHapticFeedback
 *
 * Uses View.performHapticFeedback which automatically respects system haptic settings,
 * so no need to manually check Settings.System.HAPTIC_FEEDBACK_ENABLED.
 */
object HapticUtils {

    /**
     * Performs a click haptic feedback suitable for capture actions.
     *
     * Uses [HapticFeedbackConstants.CONFIRM] on API 30+ for a confirmation-style
     * feedback, or [HapticFeedbackConstants.KEYBOARD_TAP] on older APIs.
     *
     * This method automatically respects system haptic feedback settings.
     *
     * @param view The view to perform haptic feedback from.
     */
    fun performCaptureHaptic(view: View) {
        val feedbackConstant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.KEYBOARD_TAP
        }
        view.performHapticFeedback(feedbackConstant)
    }

    /**
     * Performs haptic feedback using the Vibrator service.
     *
     * This is an alternative to View-based haptic feedback for cases
     * where a View is not available. Note that this method does NOT
     * automatically respect system haptic settings.
     *
     * @param context The Android context.
     * @param durationMs Duration of the vibration in milliseconds.
     */
    fun performVibration(context: Context, durationMs: Long = 50L) {
        val vibrator = getVibrator(context) ?: return

        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    /**
     * Gets the Vibrator service, handling API differences.
     *
     * @param context The Android context.
     * @return The Vibrator instance, or null if unavailable.
     */
    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
