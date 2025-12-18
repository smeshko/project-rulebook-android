package com.rulebook.feature.camera.components

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.ZoomState
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
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rulebook.feature.camera.FlashMode
import java.io.File

private const val TAG = "CameraPreview"

/**
 * Data class containing camera zoom bounds.
 *
 * @param minZoomRatio Minimum zoom ratio supported by the camera.
 * @param maxZoomRatio Maximum zoom ratio supported by the camera.
 */
data class ZoomBounds(
    val minZoomRatio: Float,
    val maxZoomRatio: Float
)

/**
 * Composable that displays a CameraX preview with photo capture capability.
 *
 * This composable integrates CameraX's PreviewView into Compose using AndroidView.
 * It automatically handles:
 * - Camera initialization and binding to lifecycle
 * - Using rear camera by default
 * - ImageCapture use case for photo capture
 * - Proper cleanup when the composable leaves composition
 * - Lifecycle-aware camera management (pause/resume)
 * - Flash unit availability detection
 * - Flash mode configuration for ImageCapture
 * - Torch control based on flash mode
 * - Zoom bounds extraction and callback
 *
 * ## Performance Notes
 * - Uses COMPATIBLE implementation mode for broad device support
 * - Uses FILL_CENTER scale type to fill the preview area
 * - Camera is bound to the composable's lifecycle owner for automatic pause/resume
 * - ImageCapture uses MINIMIZE_LATENCY mode for quick captures
 *
 * @param modifier Modifier for the preview container.
 * @param flashMode Current flash mode to apply to ImageCapture and torch.
 * @param onPreviewReady Callback invoked when camera preview starts displaying frames.
 * @param onError Callback invoked if camera initialization fails.
 * @param onFlashUnitAvailable Callback invoked with flash unit availability status.
 * @param onImageCaptureReady Callback providing the capture function. Call the provided
 *                            function to capture a photo. The capture result is delivered
 *                            via onImageCaptured or onCaptureError callbacks.
 * @param onImageCaptured Callback invoked when a photo is captured successfully with the image URI.
 * @param onCaptureError Callback invoked if photo capture fails.
 * @param onZoomBoundsAvailable Callback invoked with zoom bounds after camera binding.
 * @param onCameraControlAvailable Callback invoked with CameraControl for zoom operations.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    flashMode: FlashMode = FlashMode.OFF,
    onPreviewReady: () -> Unit = {},
    onError: (String) -> Unit = {},
    onFlashUnitAvailable: (Boolean) -> Unit = {},
    onImageCaptureReady: ((capturePhoto: () -> Unit) -> Unit)? = null,
    onImageCaptured: (Uri) -> Unit = {},
    onCaptureError: (String) -> Unit = {},
    onZoomBoundsAvailable: (ZoomBounds) -> Unit = {},
    onCameraControlAvailable: (CameraControl) -> Unit = {}
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

    // Store Camera instance for torch control and zoom
    val cameraState: MutableState<Camera?> = remember { mutableStateOf(null) }

    // Store ImageCapture instance for flash mode updates and photo capture
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
                val (camera, imageCapture, hasFlash) = bindCameraPreviewWithCapture(
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

                // Provide CameraControl for zoom operations
                onCameraControlAvailable(camera.cameraControl)

                // Observe zoomState LiveData to get bounds when available
                val zoomStateLiveData = camera.cameraInfo.zoomState
                val zoomObserver = object : Observer<ZoomState> {
                    override fun onChanged(zoomState: ZoomState) {
                        val bounds = ZoomBounds(
                            minZoomRatio = zoomState.minZoomRatio,
                            maxZoomRatio = zoomState.maxZoomRatio
                        )
                        Log.d(TAG, "Zoom bounds: min=${bounds.minZoomRatio}, max=${bounds.maxZoomRatio}")
                        onZoomBoundsAvailable(bounds)
                        // Remove observer after first emission - we only need bounds once
                        zoomStateLiveData.removeObserver(this)
                    }
                }
                zoomStateLiveData.observe(lifecycleOwner, zoomObserver)

                // Provide the capture function to the caller
                onImageCaptureReady?.invoke {
                    capturePhoto(
                        context = context,
                        imageCapture = imageCapture,
                        onImageCaptured = onImageCaptured,
                        onError = onCaptureError
                    )
                }
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
 * Binds the camera preview with ImageCapture to the lifecycle.
 *
 * @param cameraProvider The CameraX provider instance.
 * @param lifecycleOwner The lifecycle owner to bind the camera to.
 * @param previewView The PreviewView to display the preview.
 * @param flashMode Initial flash mode to apply.
 * @param onPreviewReady Callback when preview starts.
 * @param onFlashUnitAvailable Callback with flash unit availability status.
 * @return Triple of Camera instance, ImageCapture instance, and hasFlashUnit flag.
 */
private fun bindCameraPreviewWithCapture(
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

    // Create ImageCapture use case with minimize latency for quick captures
    // Set target rotation from the display to ensure correct EXIF orientation
    // Apply initial flash mode
    val displayRotation = previewView.display?.rotation ?: Surface.ROTATION_0
    val imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .setTargetRotation(displayRotation)
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

        Log.d(TAG, "Camera preview with capture bound successfully")
        onPreviewReady()

        return Triple(camera, imageCapture, hasFlash)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to bind camera preview with capture", e)
        throw e
    }
}

/**
 * Captures a photo and saves it to the app's cache directory.
 *
 * @param context The Android context for file operations.
 * @param imageCapture The ImageCapture use case to take the photo.
 * @param onImageCaptured Callback with the captured image URI.
 * @param onError Callback if capture fails.
 */
private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    // Create output file in cache directory
    val photoFile = File(
        context.cacheDir,
        "IMG_${System.currentTimeMillis()}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                Log.d(TAG, "Photo captured: $savedUri")
                onImageCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed", exception)
                onError("Failed to capture photo: ${exception.message}")
            }
        }
    )
}
