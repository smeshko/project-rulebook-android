package com.rulebook.feature.camera

import android.Manifest
import android.app.Activity
import androidx.camera.core.CameraControl
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.rulebook.feature.camera.components.CameraPreview
import com.rulebook.feature.camera.components.CaptureButton
import com.rulebook.feature.camera.components.FlashToggle
import com.rulebook.feature.camera.components.ZoomIndicator
import com.rulebook.feature.camera.util.rememberCaptureHapticFeedback
import org.koin.androidx.compose.koinViewModel

/**
 * Camera screen composable with full-screen immersive camera preview.
 *
 * This screen provides:
 * - Camera permission handling with rationale
 * - Full-screen camera preview using CameraX
 * - Photo capture with haptic feedback
 * - Flash/torch control toggle
 * - Immersive mode with hidden system bars (status bar and navigation bar)
 * - Loading state while camera initializes
 * - Error state display for camera failures
 *
 * The screen automatically hides system bars when entering and restores them on exit.
 * This provides an immersive camera experience without any UI chrome.
 *
 * @param modifier Optional modifier for the screen container.
 * @param viewModel The ViewModel managing camera state.
 * @param onPhotoCaptured Callback invoked when a photo is captured successfully with the image URI.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = koinViewModel(),
    onPhotoCaptured: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val hapticFeedback = rememberCaptureHapticFeedback()

    // Store the capture function when CameraPreview provides it
    val capturePhotoState = remember { mutableStateOf<(() -> Unit)?>(null) }

    // Handle immersive mode - hide system bars
    ImmersiveMode()

    // Request permission on first composition if not granted
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // Handle successful capture - trigger haptic feedback and notify parent
    LaunchedEffect(uiState.capturedImageUri) {
        uiState.capturedImageUri?.let { uri ->
            // Haptic feedback confirms successful capture (AC #2)
            hapticFeedback()
            onPhotoCaptured(uri)
            viewModel.clearCapturedImage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Permission granted - show camera preview
            cameraPermissionState.status.isGranted -> {
                // Track CameraControl for applying zoom
                var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

                // Camera preview with zoom gesture detection
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(uiState.zoomRatio, uiState.minZoomRatio, uiState.maxZoomRatio) {
                            detectTransformGestures { _, _, zoom, _ ->
                                // Calculate new zoom based on gesture scale factor
                                val newZoom = (uiState.zoomRatio * zoom).coerceIn(
                                    uiState.minZoomRatio,
                                    uiState.maxZoomRatio
                                )
                                viewModel.setZoomRatio(newZoom)

                                // Apply zoom to camera immediately
                                cameraControl?.setZoomRatio(newZoom)
                            }
                        }
                ) {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        flashMode = uiState.flashMode,
                        onPreviewReady = { viewModel.onCameraReady() },
                        onError = { viewModel.onCameraError(it) },
                        onFlashUnitAvailable = { viewModel.onFlashUnitAvailable(it) },
                        onImageCaptureReady = { captureFunction ->
                            capturePhotoState.value = captureFunction
                        },
                        onImageCaptured = { uri ->
                            viewModel.onCaptureSuccess(uri.toString())
                        },
                        onCaptureError = { error ->
                            viewModel.onCaptureError(error)
                        },
                        onZoomBoundsAvailable = { bounds ->
                            viewModel.setZoomBounds(bounds.minZoomRatio, bounds.maxZoomRatio)
                        },
                        onCameraControlAvailable = { control ->
                            cameraControl = control
                        }
                    )
                }

                // Zoom indicator overlay - centered on screen
                ZoomIndicator(
                    zoomRatio = uiState.zoomRatio,
                    visible = uiState.showZoomIndicator,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Show loading indicator while camera initializes
                if (!uiState.isCameraReady && uiState.error == null) {
                    CircularProgressIndicator(
                        color = Color.White
                    )
                }

                // Show error message if camera fails
                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Flash toggle - only show if device has flash unit (Story 4.3)
                if (uiState.hasFlashUnit) {
                    FlashToggle(
                        flashMode = uiState.flashMode,
                        onToggle = { viewModel.cycleFlashMode() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    )
                }

                // Show capture button when camera is ready (Story 4.2)
                if (uiState.isCameraReady) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                            .padding(bottom = 48.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        CaptureButton(
                            onClick = {
                                capturePhotoState.value?.let { capturePhoto ->
                                    viewModel.onCaptureStarted()
                                    capturePhoto()
                                }
                            },
                            enabled = uiState.isCameraReady && capturePhotoState.value != null,
                            isCapturing = uiState.isCapturing
                        )
                    }
                }
            }

            // Permission denied but can show rationale
            cameraPermissionState.status.shouldShowRationale -> {
                PermissionRationale(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                )
            }

            // Permission denied permanently or waiting for initial request
            else -> {
                PermissionDenied()
            }
        }
    }
}

/**
 * Composable showing rationale for camera permission request.
 *
 * Displayed when the user has denied the permission once but hasn't selected
 * "Don't ask again". Explains why the permission is needed and offers a button
 * to request again.
 *
 * @param onRequestPermission Callback to trigger permission request.
 */
@Composable
internal fun PermissionRationale(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Permission Required",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "The camera is needed to capture photos of your game boxes for rule extraction.",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}

/**
 * Composable shown when camera permission is denied.
 *
 * Displayed when the permission has been permanently denied. Informs the user
 * they need to enable the permission in system settings.
 */
@Composable
internal fun PermissionDenied() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Permission Denied",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Please enable camera permission in your device settings to use this feature.",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Composable that enables immersive mode by hiding system bars.
 *
 * System bars (status bar and navigation bar) are hidden when this composable
 * enters composition and restored when it leaves. Uses BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
 * so users can temporarily reveal bars by swiping.
 */
@Composable
private fun ImmersiveMode() {
    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose { }
        val insetsController = WindowCompat.getInsetsController(window, view)

        // Hide system bars
        insetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            // Restore system bars when leaving the screen
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
