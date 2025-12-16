package com.rulebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Main entry point for the Rulebook app.
 *
 * Configures edge-to-edge display and sets up the root composable.
 * Edge-to-edge is enabled before setContent to ensure proper system bar handling.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge BEFORE super.onCreate() and setContent
        // This ensures proper status bar and navigation bar transparency
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            RulebookTheme {
                RulebookApp()
            }
        }
    }
}
