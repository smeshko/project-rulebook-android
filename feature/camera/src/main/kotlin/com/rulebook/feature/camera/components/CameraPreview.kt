package com.rulebook.feature.camera.components

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rulebook.feature.camera.FlashMode

private const val TAG = "CameraPreview"

/**
 * Composable that displays a CameraX preview.
 *
 * This composable integrates CameraX's PreviewView into Compose using AndroidView.
 * It automatically handles:
 * - Camera initialization and binding to lifecycle
 * - Using rear camera by default
 * - Proper cleanup when the composable leaves composition
 * - Lifecycle-aware camera management (pause/resume)
 * - Flash unit availability detection
 * - Flash mode configuration for ImageCapture
 * - Torch control based on flash mode
 *
 * ## Performance Notes
 * - Uses COMPATIBLE implementation mode for broad device support
 * - Uses FILL_CENTER scale type to fill the preview area
 * - Camera is bound to the composable's lifecycle owner for automatic pause/resume
 *
 * @param modifier Modifier for the preview container.
 * @param flashMode Current flash mode to apply to ImageCapture and torch.
 * @param onPreviewReady Callback invoked when camera preview starts displaying frames.
 * @param onError Callback invoked if camera initialization fails.
 * @param onFlashUnitAvailable Callback invoked with flash unit availability status.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    flashMode: FlashMode = FlashMode.OFF,
    onPreviewReady: () -> Unit = {},
    onError: (String) -> Unit = {},
    onFlashUnitAvailable: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Remember the PreviewView to avoid recreation
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Track whether the composable is still active to prevent binding after disposal
    val isActiveState = remember { mutableStateOf(true) }

    // Store Camera instance for torch control
    val cameraState: MutableState<Camera?> = remember { mutableStateOf(null) }

    // Store ImageCapture instance for flash mode updates
    val imageCaptureState: MutableState<ImageCapture?> = remember { mutableStateOf(null) }

    // Track whether the device has a flash unit to guard torch operations
    val hasFlashUnitState = remember { mutableStateOf(false) }

    // Handle camera binding and cleanup
    DisposableEffect(lifecycleOwner) {
        isActiveState.value = true
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // Skip binding if composable has been disposed or lifecycle is destroyed
            if (!isActiveState.value) {
                Log.d(TAG, "Composable disposed before camera ready, skipping bind")
                return@addListener
            }

            if (!lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                Log.d(TAG, "Lifecycle not started, skipping camera bind")
                return@addListener
            }

            try {
                val cameraProvider = cameraProviderFuture.get()
                val (camera, imageCapture, hasFlash) = bindCameraPreview(
                    cameraProvider = cameraProvider,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    flashMode = flashMode,
                    onPreviewReady = onPreviewReady,
                    onFlashUnitAvailable = onFlashUnitAvailable
                )
                cameraState.value = camera
                imageCaptureState.value = imageCapture
                hasFlashUnitState.value = hasFlash
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
                onError("Failed to initialize camera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            // Mark composable as disposed to prevent binding in pending listeners
            isActiveState.value = false

            // Disable torch on dispose (only if device has flash unit)
            if (hasFlashUnitState.value) {
                cameraState.value?.cameraControl?.enableTorch(false)
            }
            cameraState.value = null
            imageCaptureState.value = null
            hasFlashUnitState.value = false

            // Unbind all use cases when leaving the screen
            // Only unbind if the future is already complete to avoid blocking the main thread
            // during back navigation (especially on first launch when camera is still initializing)
            if (cameraProviderFuture.isDone) {
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                } catch (e: Exception) {
                    Log.e(TAG, "Error unbinding camera", e)
                }
            } else {
                // If camera is still initializing, the listener will check isActiveState
                // and skip binding, so we don't need to unbind here
                Log.d(TAG, "Camera provider not ready on dispose, listener will skip bind")
            }
        }
    }

    // Update torch and ImageCapture flash mode when flashMode changes
    LaunchedEffect(flashMode, cameraState.value, imageCaptureState.value, hasFlashUnitState.value) {
        val camera = cameraState.value ?: return@LaunchedEffect
        val imageCapture = imageCaptureState.value ?: return@LaunchedEffect
        val hasFlash = hasFlashUnitState.value

        // Only update flash-related settings if device has a flash unit
        if (!hasFlash) {
            Log.d(TAG, "Device has no flash unit, skipping flash mode update")
            return@LaunchedEffect
        }

        // Update ImageCapture flash mode
        imageCapture.flashMode = flashMode.toImageCaptureFlashMode()
        Log.d(TAG, "ImageCapture flash mode updated to: $flashMode")

        // Enable torch only when flash mode is ON (continuous light)
        // For AUTO mode, flash fires only during capture, not as continuous light
        val enableTorch = flashMode == FlashMode.ON
        camera.cameraControl.enableTorch(enableTorch)
        Log.d(TAG, "Torch enabled: $enableTorch")
    }

    // Display the PreviewView
    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

/**
 * Binds the camera preview to the lifecycle.
 *
 * @param cameraProvider The CameraX provider instance.
 * @param lifecycleOwner The lifecycle owner to bind the camera to.
 * @param previewView The PreviewView to display the preview.
 * @param flashMode Initial flash mode to apply.
 * @param onPreviewReady Callback when preview starts.
 * @param onFlashUnitAvailable Callback with flash unit availability status.
 * @return Triple of Camera instance, ImageCapture instance, and hasFlashUnit flag for external control.
 */
private fun bindCameraPreview(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    flashMode: FlashMode,
    onPreviewReady: () -> Unit,
    onFlashUnitAvailable: (Boolean) -> Unit
): Triple<Camera, ImageCapture, Boolean> {
    // Unbind any existing use cases first
    cameraProvider.unbindAll()

    // Create Preview use case
    val preview = Preview.Builder()
        .build()
        .also { preview ->
            preview.surfaceProvider = previewView.surfaceProvider
        }

    // Create ImageCapture use case with initial flash mode
    val imageCapture = ImageCapture.Builder()
        .setFlashMode(flashMode.toImageCaptureFlashMode())
        .build()

    // Select rear camera
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        // Bind camera to lifecycle with both Preview and ImageCapture
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )

        // Check if the device has a flash unit
        val hasFlash = camera.cameraInfo.hasFlashUnit()
        Log.d(TAG, "Camera has flash unit: $hasFlash")
        onFlashUnitAvailable(hasFlash)

        // Enable torch if flash mode is ON (continuous light during preview)
        if (hasFlash && flashMode == FlashMode.ON) {
            camera.cameraControl.enableTorch(true)
            Log.d(TAG, "Initial torch enabled for flash mode ON")
        }

        Log.d(TAG, "Camera preview bound successfully with ImageCapture")
        onPreviewReady()

        return Triple(camera, imageCapture, hasFlash)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to bind camera preview", e)
        throw e
    }
}
