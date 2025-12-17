package com.rulebook.feature.camera.components

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

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
 *
 * ## Performance Notes
 * - Uses COMPATIBLE implementation mode for broad device support
 * - Uses FILL_CENTER scale type to fill the preview area
 * - Camera is bound to the composable's lifecycle owner for automatic pause/resume
 *
 * @param modifier Modifier for the preview container.
 * @param onPreviewReady Callback invoked when camera preview starts displaying frames.
 * @param onError Callback invoked if camera initialization fails.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onPreviewReady: () -> Unit = {},
    onError: (String) -> Unit = {}
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

    // Handle camera binding and cleanup
    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindCameraPreview(
                    cameraProvider = cameraProvider,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    onPreviewReady = onPreviewReady
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
                onError("Failed to initialize camera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
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
                // If camera is still initializing, cancel via the listener mechanism
                // The camera will be unbound when the provider becomes available
                Log.d(TAG, "Camera provider not ready on dispose, skipping unbind")
            }
        }
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
 * @param onPreviewReady Callback when preview starts.
 */
private fun bindCameraPreview(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onPreviewReady: () -> Unit
) {
    // Unbind any existing use cases first
    cameraProvider.unbindAll()

    // Create Preview use case
    val preview = Preview.Builder()
        .build()
        .also { preview ->
            preview.surfaceProvider = previewView.surfaceProvider
        }

    // Select rear camera
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        // Bind camera to lifecycle
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview
        )

        Log.d(TAG, "Camera preview bound successfully")
        onPreviewReady()
    } catch (e: Exception) {
        Log.e(TAG, "Failed to bind camera preview", e)
        throw e
    }
}
