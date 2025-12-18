package com.rulebook.feature.camera

import androidx.camera.core.ImageCapture
import org.junit.Test
import kotlin.test.assertEquals

class FlashModeTest {

    @Test
    fun `next cycles OFF to ON`() {
        assertEquals(FlashMode.ON, FlashMode.OFF.next())
    }

    @Test
    fun `next cycles ON to AUTO`() {
        assertEquals(FlashMode.AUTO, FlashMode.ON.next())
    }

    @Test
    fun `next cycles AUTO to OFF`() {
        assertEquals(FlashMode.OFF, FlashMode.AUTO.next())
    }

    @Test
    fun `toImageCaptureFlashMode returns correct constant for OFF`() {
        assertEquals(ImageCapture.FLASH_MODE_OFF, FlashMode.OFF.toImageCaptureFlashMode())
    }

    @Test
    fun `toImageCaptureFlashMode returns correct constant for ON`() {
        assertEquals(ImageCapture.FLASH_MODE_ON, FlashMode.ON.toImageCaptureFlashMode())
    }

    @Test
    fun `toImageCaptureFlashMode returns correct constant for AUTO`() {
        assertEquals(ImageCapture.FLASH_MODE_AUTO, FlashMode.AUTO.toImageCaptureFlashMode())
    }

    @Test
    fun `full cycle returns to original mode`() {
        val original = FlashMode.OFF
        val afterOneCycle = original.next().next().next()
        assertEquals(original, afterOneCycle)
    }
}
