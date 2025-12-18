package com.rulebook.feature.camera

import android.Manifest
import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.MeteringPointFactory
import androidx.concurrent.futures.await
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import android.net.Uri
import com.rulebook.core.designsystem.component.CreditsDisplay
import com.rulebook.feature.camera.components.CameraPreview
import com.rulebook.feature.camera.components.CaptureButton
import com.rulebook.feature.camera.components.FlashToggle
import com.rulebook.feature.camera.components.FocusIndicator
import com.rulebook.feature.camera.components.GalleryButton
import com.rulebook.feature.camera.components.PermissionRationale
import com.rulebook.feature.camera.components.ZoomIndicator
import com.rulebook.feature.camera.util.getLastPhotoThumbnailUri
import com.rulebook.feature.camera.util.rememberCaptureHapticFeedback
import kotlinx.coroutines.launch
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
 * @param onGalleryImageSelected Callback invoked when a gallery image is selected with the image URI.
 *                                Uses the same processing flow as captured photos.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = koinViewModel(),
    onPhotoCaptured: (String) -> Unit = {},
    onGalleryImageSelected: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val hapticFeedback = rememberCaptureHapticFeedback()

    // Store the capture function when CameraPreview provides it
    val capturePhotoState = remember { mutableStateOf<(() -> Unit)?>(null) }

    // Photo picker for gallery selection (Story 4.6)
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // uri is null when user cancels - no action needed (AC #3)
        uri?.let { selectedUri ->
            // Pass directly to parent via callback - same processing path as captured photos
            onGalleryImageSelected(selectedUri.toString())
        }
    }

    // Handle immersive mode - hide system bars
    ImmersiveMode()

    // Load last gallery thumbnail (Story 4.6)
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val thumbnailUri = getLastPhotoThumbnailUri(context)
        viewModel.setLastGalleryThumbnail(thumbnailUri?.toString())
    }

    // Check permission state on first composition and sync to ViewModel (Story 4.9)
    // NOTE: We do NOT auto-request permission here (AC #5 - not requested at app launch)
    LaunchedEffect(cameraPermissionState.status, uiState.hasRequestedPermission) {
        when {
            cameraPermissionState.status.isGranted -> {
                viewModel.onPermissionGranted()
            }
            cameraPermissionState.status.shouldShowRationale -> {
                // User denied once but can ask again
                viewModel.onPermissionDenied()
            }
            uiState.hasRequestedPermission -> {
                // We've asked before and now !isGranted and !shouldShowRationale
                // This means permanently denied ("Don't ask again" was selected)
                viewModel.onPermissionPermanentlyDenied()
            }
            // else: First time (NOT_DETERMINED) - leave as NOT_DETERMINED
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
                // Track CameraControl for applying zoom and focus
                var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
                var meteringPointFactory by remember { mutableStateOf<MeteringPointFactory?>(null) }
                val coroutineScope = rememberCoroutineScope()

                // Capture latest zoom state for use in gesture handler without restarting it
                val currentZoomRatio by rememberUpdatedState(uiState.zoomRatio)
                val currentMinZoom by rememberUpdatedState(uiState.minZoomRatio)
                val currentMaxZoom by rememberUpdatedState(uiState.maxZoomRatio)

                // Camera preview with tap-to-focus and pinch-to-zoom gesture detection
                // Key on Unit to keep gesture handlers stable throughout gestures
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // Tap gesture for focus (processed first, before transform)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                // Execute focus metering on camera
                                // Only show focus indicator and trigger focus if camera is ready
                                val factory = meteringPointFactory
                                val control = cameraControl
                                if (factory != null && control != null) {
                                    // Store raw pixel coordinates for UI positioning
                                    viewModel.onTapToFocus(offset.x, offset.y)
                                    // Create metering point at tap location
                                    val meteringPoint = factory.createPoint(offset.x, offset.y)
                                    val focusAction = FocusMeteringAction.Builder(
                                        meteringPoint,
                                        FocusMeteringAction.FLAG_AF
                                    )
                                        .setAutoCancelDuration(5, TimeUnit.SECONDS)
                                        .build()

                                    coroutineScope.launch {
                                        try {
                                            control.startFocusAndMetering(focusAction).await()
                                            Log.d("CameraScreen", "Focus started at (${offset.x}, ${offset.y})")
                                        } catch (e: Exception) {
                                            Log.w("CameraScreen", "Failed to focus: ${e.message}")
                                        }
                                    }
                                } else {
                                    // Camera not ready - ignore tap (no false focus feedback)
                                    Log.d("CameraScreen", "Tap ignored - camera not ready")
                                }
                            }
                        }
                        // Transform gesture for zoom (pinch)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                // Calculate new zoom based on gesture scale factor
                                val newZoom = (currentZoomRatio * zoom).coerceIn(
                                    currentMinZoom,
                                    currentMaxZoom
                                )
                                viewModel.setZoomRatio(newZoom)

                                // Apply zoom to camera with error handling
                                cameraControl?.let { control ->
                                    coroutineScope.launch {
                                        try {
                                            control.setZoomRatio(newZoom).await()
                                        } catch (e: Exception) {
                                            Log.w("CameraScreen", "Failed to set zoom ratio: ${e.message}")
                                            // Note: We don't revert UI state as the gesture is continuous
                                            // and the next gesture update will retry
                                        }
                                    }
                                }
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
                        },
                        onMeteringPointFactoryAvailable = { factory ->
                            meteringPointFactory = factory
                        }
                    )
                }

                // Auto-hide zoom indicator after delay
                // Key on both zoomRatio and showZoomIndicator to:
                // 1. Restart timer on each zoom change during continuous gestures
                // 2. Start timer when indicator first becomes visible
                // The indicator will hide 1.5s after the last zoom change
                LaunchedEffect(uiState.zoomRatio, uiState.showZoomIndicator) {
                    if (uiState.showZoomIndicator) {
                        kotlinx.coroutines.delay(1500L)
                        viewModel.hideZoomIndicator()
                    }
                }

                // Auto-hide focus indicator after delay (~1 second)
                // Key on focusPoint to restart timer on each tap
                LaunchedEffect(uiState.focusPoint, uiState.showFocusIndicator) {
                    if (uiState.showFocusIndicator) {
                        kotlinx.coroutines.delay(1000L)
                        viewModel.hideFocusIndicator()
                    }
                }

                // Zoom indicator overlay - centered on screen
                ZoomIndicator(
                    zoomRatio = uiState.zoomRatio,
                    visible = uiState.showZoomIndicator,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Focus indicator overlay - positioned at tap location
                FocusIndicator(
                    focusPoint = uiState.focusPoint,
                    visible = uiState.showFocusIndicator
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
                // Uses statusBarsPadding to avoid notch/punch-hole cutouts
                if (uiState.hasFlashUnit) {
                    FlashToggle(
                        flashMode = uiState.flashMode,
                        onToggle = { viewModel.cycleFlashMode() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .statusBarsPadding()
                            .padding(16.dp)
                    )
                }

                // Credits display - shows user's remaining scan credits (Story 4.8)
                // Uses statusBarsPadding to avoid notch/punch-hole cutouts
                CreditsDisplay(
                    creditCount = uiState.creditBalance,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                )

                // Show camera controls when camera is ready (Story 4.2)
                if (uiState.isCameraReady) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                            .padding(bottom = 48.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Gallery button (Story 4.6) - positioned to the left of capture button
                            GalleryButton(
                                onClick = {
                                    pickMedia.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                thumbnailUri = uiState.lastGalleryThumbnailUri?.let { Uri.parse(it) }
                            )

                            // Capture button (Story 4.2)
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

                            // Spacer for symmetry (balances the gallery button on the left)
                            Spacer(modifier = Modifier.size(56.dp))
                        }
                    }
                }
            }

            // Permission denied but can show rationale OR first time user
            cameraPermissionState.status.shouldShowRationale ||
            uiState.permissionState == CameraPermissionState.NOT_DETERMINED -> {
                PermissionRationale(
                    onRequestPermission = {
                        viewModel.onPermissionRequested()
                        cameraPermissionState.launchPermissionRequest()
                    },
                    onNavigateBack = { /* No-op for now - CameraScreen doesn't have back navigation */ },
                    onGalleryClick = {
                        pickMedia.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    galleryThumbnailUri = uiState.lastGalleryThumbnailUri?.let { Uri.parse(it) }
                )
            }

            // Permission denied permanently ("Don't ask again" selected)
            else -> {
                PermissionDenied(
                    onGalleryClick = {
                        pickMedia.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    galleryThumbnailUri = uiState.lastGalleryThumbnailUri?.let { Uri.parse(it) }
                )
            }
        }
    }
}

/**
 * Composable shown when camera permission is denied.
 *
 * Displayed when the permission has been permanently denied. Informs the user
 * they need to enable the permission in system settings. Also provides gallery
 * access as an alternative (Story 4.6).
 *
 * @param onGalleryClick Callback to open the gallery picker.
 * @param galleryThumbnailUri Optional URI for gallery button thumbnail.
 */
@Composable
internal fun PermissionDenied(
    onGalleryClick: () -> Unit,
    galleryThumbnailUri: Uri?
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Or select an existing photo from your gallery:",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        // Gallery button as alternative - available even without camera permission (Story 4.6)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            GalleryButton(
                onClick = onGalleryClick,
                thumbnailUri = galleryThumbnailUri
            )
        }
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
