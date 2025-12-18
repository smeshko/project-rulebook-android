package com.rulebook.feature.camera

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CameraViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CameraViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has camera not ready and no error`() = runTest {
        val state = viewModel.uiState.first()

        assertFalse(state.isCameraReady)
        assertNull(state.error)
    }

    @Test
    fun `onCameraReady sets isCameraReady to true`() = runTest {
        viewModel.onCameraReady()
        val state = viewModel.uiState.first()

        assertTrue(state.isCameraReady)
        assertNull(state.error)
    }

    @Test
    fun `onCameraError sets error message and camera not ready`() = runTest {
        val errorMessage = "Camera initialization failed"
        viewModel.onCameraError(errorMessage)
        val state = viewModel.uiState.first()

        assertFalse(state.isCameraReady)
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun `onCameraReady clears existing error`() = runTest {
        // First set an error
        viewModel.onCameraError("Some error")

        // Then mark camera as ready
        viewModel.onCameraReady()
        val state = viewModel.uiState.first()

        assertTrue(state.isCameraReady)
        assertNull(state.error)
    }

    @Test
    fun `clearError removes error but keeps camera ready state`() = runTest {
        // Set error first
        viewModel.onCameraError("Some error")

        // Clear the error
        viewModel.clearError()
        val state = viewModel.uiState.first()

        assertNull(state.error)
        // Camera is still not ready (wasn't set ready before)
        assertFalse(state.isCameraReady)
    }

    @Test
    fun `clearError preserves camera ready state when camera was ready`() = runTest {
        // First set camera as ready
        viewModel.onCameraReady()

        // Set an error
        viewModel.onCameraError("Some error")

        // Clear the error (simulating retry scenario)
        viewModel.clearError()

        // Then mark camera ready again
        viewModel.onCameraReady()
        val state = viewModel.uiState.first()

        assertTrue(state.isCameraReady)
        assertNull(state.error)
    }

    // =========================================================================
    // Flash Mode Tests (Story 4.3)
    // =========================================================================

    @Test
    fun `initial state has flash mode OFF`() = runTest {
        val state = viewModel.uiState.first()

        assertEquals(FlashMode.OFF, state.flashMode)
    }

    @Test
    fun `initial state has hasFlashUnit false`() = runTest {
        val state = viewModel.uiState.first()

        assertFalse(state.hasFlashUnit)
    }

    @Test
    fun `cycleFlashMode cycles from OFF to ON`() = runTest {
        viewModel.cycleFlashMode()
        val state = viewModel.uiState.first()

        assertEquals(FlashMode.ON, state.flashMode)
    }

    @Test
    fun `cycleFlashMode cycles from ON to AUTO`() = runTest {
        viewModel.cycleFlashMode() // OFF -> ON
        viewModel.cycleFlashMode() // ON -> AUTO
        val state = viewModel.uiState.first()

        assertEquals(FlashMode.AUTO, state.flashMode)
    }

    @Test
    fun `cycleFlashMode cycles from AUTO to OFF`() = runTest {
        viewModel.cycleFlashMode() // OFF -> ON
        viewModel.cycleFlashMode() // ON -> AUTO
        viewModel.cycleFlashMode() // AUTO -> OFF
        val state = viewModel.uiState.first()

        assertEquals(FlashMode.OFF, state.flashMode)
    }

    @Test
    fun `onFlashUnitAvailable sets hasFlashUnit to true`() = runTest {
        viewModel.onFlashUnitAvailable(true)
        val state = viewModel.uiState.first()

        assertTrue(state.hasFlashUnit)
    }

    @Test
    fun `onFlashUnitAvailable sets hasFlashUnit to false`() = runTest {
        // First set to true
        viewModel.onFlashUnitAvailable(true)
        // Then set to false
        viewModel.onFlashUnitAvailable(false)
        val state = viewModel.uiState.first()

        assertFalse(state.hasFlashUnit)
    }

    // =========================================================================
    // Photo Capture Tests (Story 4.2)
    // =========================================================================

    @Test
    fun `initial state has isCapturing false and no captured image`() = runTest {
        val state = viewModel.uiState.first()

        assertFalse(state.isCapturing)
        assertNull(state.capturedImageUri)
    }

    @Test
    fun `onCaptureStarted sets isCapturing to true`() = runTest {
        viewModel.onCaptureStarted()
        val state = viewModel.uiState.first()

        assertTrue(state.isCapturing)
    }

    @Test
    fun `onCaptureSuccess sets capturedImageUri and clears isCapturing`() = runTest {
        val testUri = "file:///test/image.jpg"
        viewModel.onCaptureStarted()
        viewModel.onCaptureSuccess(testUri)
        val state = viewModel.uiState.first()

        assertFalse(state.isCapturing)
        assertEquals(testUri, state.capturedImageUri)
        assertNull(state.error)
    }

    @Test
    fun `onCaptureError sets error and clears isCapturing`() = runTest {
        val errorMessage = "Failed to capture photo"
        viewModel.onCaptureStarted()
        viewModel.onCaptureError(errorMessage)
        val state = viewModel.uiState.first()

        assertFalse(state.isCapturing)
        assertEquals(errorMessage, state.error)
        assertNull(state.capturedImageUri)
    }

    @Test
    fun `clearCapturedImage resets captured image uri`() = runTest {
        val testUri = "file:///test/image.jpg"
        viewModel.onCaptureSuccess(testUri)
        viewModel.clearCapturedImage()
        val state = viewModel.uiState.first()

        assertNull(state.capturedImageUri)
    }

    @Test
    fun `capture button is disabled during capture`() = runTest {
        viewModel.onCaptureStarted()
        val state = viewModel.uiState.first()

        assertTrue(state.isCapturing)
        // isCapturing being true means button should be disabled
    }

    // =========================================================================
    // Zoom State Tests (Story 4.4)
    // =========================================================================

    @Test
    fun `initial state has default zoom values`() = runTest {
        val state = viewModel.uiState.first()

        assertEquals(1f, state.zoomRatio)
        assertEquals(1f, state.minZoomRatio)
        assertEquals(1f, state.maxZoomRatio)
        assertFalse(state.showZoomIndicator)
    }

    @Test
    fun `setZoomBounds updates min and max zoom ratios`() = runTest {
        viewModel.setZoomBounds(minZoom = 1f, maxZoom = 10f)
        val state = viewModel.uiState.first()

        assertEquals(1f, state.minZoomRatio)
        assertEquals(10f, state.maxZoomRatio)
    }

    @Test
    fun `setZoomRatio updates zoom ratio within bounds`() = runTest {
        viewModel.setZoomBounds(minZoom = 1f, maxZoom = 10f)
        viewModel.setZoomRatio(5f)
        val state = viewModel.uiState.first()

        assertEquals(5f, state.zoomRatio)
    }

    @Test
    fun `setZoomRatio clamps to max zoom ratio`() = runTest {
        viewModel.setZoomBounds(minZoom = 1f, maxZoom = 5f)
        viewModel.setZoomRatio(10f)
        val state = viewModel.uiState.first()

        assertEquals(5f, state.zoomRatio)
    }

    @Test
    fun `setZoomRatio clamps to min zoom ratio`() = runTest {
        viewModel.setZoomBounds(minZoom = 1f, maxZoom = 5f)
        viewModel.setZoomRatio(0.5f)
        val state = viewModel.uiState.first()

        assertEquals(1f, state.zoomRatio)
    }

    @Test
    fun `setZoomRatio shows zoom indicator`() = runTest {
        viewModel.setZoomRatio(2f)
        val state = viewModel.uiState.first()

        assertTrue(state.showZoomIndicator)
    }

    @Test
    fun `hideZoomIndicator hides the indicator`() = runTest {
        viewModel.setZoomRatio(2f)
        viewModel.hideZoomIndicator()
        val state = viewModel.uiState.first()

        assertFalse(state.showZoomIndicator)
    }

    // =========================================================================
    // Gallery Picker Tests (Story 4.6)
    // =========================================================================

    @Test
    fun `initial state has no selected gallery image`() = runTest {
        val state = viewModel.uiState.first()

        assertNull(state.selectedGalleryImageUri)
    }

    @Test
    fun `onGalleryImageSelected sets the selected image URI`() = runTest {
        val testUri = "content://media/external/images/1234"
        viewModel.onGalleryImageSelected(testUri)
        val state = viewModel.uiState.first()

        assertEquals(testUri, state.selectedGalleryImageUri)
    }

    @Test
    fun `clearSelectedGalleryImage resets the selected image URI`() = runTest {
        val testUri = "content://media/external/images/1234"
        viewModel.onGalleryImageSelected(testUri)
        viewModel.clearSelectedGalleryImage()
        val state = viewModel.uiState.first()

        assertNull(state.selectedGalleryImageUri)
    }

    @Test
    fun `selecting gallery image does not affect captured image URI`() = runTest {
        val capturedUri = "file:///test/captured.jpg"
        val galleryUri = "content://media/external/images/1234"

        viewModel.onCaptureSuccess(capturedUri)
        viewModel.onGalleryImageSelected(galleryUri)
        val state = viewModel.uiState.first()

        assertEquals(capturedUri, state.capturedImageUri)
        assertEquals(galleryUri, state.selectedGalleryImageUri)
    }

    @Test
    fun `initial state has no last gallery thumbnail`() = runTest {
        val state = viewModel.uiState.first()

        assertNull(state.lastGalleryThumbnailUri)
    }

    @Test
    fun `setLastGalleryThumbnail updates the thumbnail URI`() = runTest {
        val thumbnailUri = "content://media/external/images/5678"
        viewModel.setLastGalleryThumbnail(thumbnailUri)
        val state = viewModel.uiState.first()

        assertEquals(thumbnailUri, state.lastGalleryThumbnailUri)
    }

    @Test
    fun `setLastGalleryThumbnail with null clears the thumbnail`() = runTest {
        val thumbnailUri = "content://media/external/images/5678"
        viewModel.setLastGalleryThumbnail(thumbnailUri)
        viewModel.setLastGalleryThumbnail(null)
        val state = viewModel.uiState.first()

        assertNull(state.lastGalleryThumbnailUri)
    }
}
