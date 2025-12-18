package com.rulebook.feature.camera

import androidx.camera.core.ImageCapture

/**
 * Represents the available flash modes for the camera.
 *
 * Flash modes control how the camera flash behaves during photo capture:
 * - [OFF]: Flash is disabled, no light emitted
 * - [ON]: Flash/torch is always on (continuous light during preview and capture)
 * - [AUTO]: Flash fires automatically based on lighting conditions
 *
 * The modes cycle in order: OFF → ON → AUTO → OFF
 */
enum class FlashMode {
    OFF,
    ON,
    AUTO;

    /**
     * Returns the next flash mode in the cycle.
     * Cycles: OFF → ON → AUTO → OFF
     */
    fun next(): FlashMode = when (this) {
        OFF -> ON
        ON -> AUTO
        AUTO -> OFF
    }

    /**
     * Converts this flash mode to the corresponding CameraX ImageCapture flash mode constant.
     */
    fun toImageCaptureFlashMode(): Int = when (this) {
        OFF -> ImageCapture.FLASH_MODE_OFF
        ON -> ImageCapture.FLASH_MODE_ON
        AUTO -> ImageCapture.FLASH_MODE_AUTO
    }
}
