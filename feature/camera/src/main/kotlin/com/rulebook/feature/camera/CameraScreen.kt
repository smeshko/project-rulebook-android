package com.rulebook.feature.camera

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rulebook.feature.camera.components.CameraPreview
import org.koin.androidx.compose.koinViewModel

/**
 * Camera screen composable with full-screen immersive camera preview.
 *
 * This screen provides:
 * - Full-screen camera preview using CameraX
 * - Immersive mode with hidden system bars (status bar and navigation bar)
 * - Loading state while camera initializes
 * - Error state display for camera failures
 *
 * The screen automatically hides system bars when entering and restores them on exit.
 * This provides an immersive camera experience without any UI chrome.
 *
 * @param modifier Optional modifier for the screen container.
 * @param viewModel The ViewModel managing camera state.
 */
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle immersive mode - hide system bars
    ImmersiveMode()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Camera preview fills the screen
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onPreviewReady = { viewModel.onCameraReady() },
            onError = { viewModel.onCameraError(it) }
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
                style = MaterialTheme.typography.bodyLarge
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
