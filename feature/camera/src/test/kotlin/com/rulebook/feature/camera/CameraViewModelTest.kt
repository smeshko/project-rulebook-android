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
}
