package com.rulebook.feature.scan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Scan flow screen composable.
 *
 * This screen orchestrates the entire scan flow:
 * 1. Credit check (Story 5.1)
 * 2. Progress indicators (Story 5.2)
 * 3. Image analysis (Story 5.3)
 * 4. Confidence display (Story 5.4)
 * 5. Manual entry fallback (Story 5.5)
 * 6. Rules generation (Story 5.6)
 * 7. Save to database (Story 5.7)
 *
 * For Story 5.1, this screen only handles credit checking and routing.
 *
 * @param viewModel The ViewModel managing scan flow state.
 * @param onNavigateToPaywall Callback to navigate to paywall when credits == 0.
 * @param onProceedToAnalysis Callback to proceed to image analysis (Story 5.2+).
 */
@Composable
fun ScanFlowScreen(
    viewModel: ScanFlowViewModel,
    onNavigateToPaywall: () -> Unit,
    onProceedToAnalysis: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle navigation side effects
    LaunchedEffect(uiState) {
        when (uiState) {
            is ScanFlowUiState.NavigatingToPaywall -> {
                onNavigateToPaywall()
            }
            is ScanFlowUiState.ReadyToAnalyze -> {
                // Story 5.2+ will implement analysis flow
                // For now, this is a placeholder
                onProceedToAnalysis()
            }
            else -> {
                // CheckingCredits or Error - handled in UI below
            }
        }
    }

    // Render UI based on state
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is ScanFlowUiState.CheckingCredits -> {
                CheckingCreditsContent()
            }
            is ScanFlowUiState.NavigatingToPaywall -> {
                // Navigation handled by LaunchedEffect above
                // Show brief loading state
                CheckingCreditsContent()
            }
            is ScanFlowUiState.ReadyToAnalyze -> {
                // Story 5.2+ will show progress screen
                // For now, show placeholder
                Text(
                    text = "Ready to analyze (Story 5.2+)",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is ScanFlowUiState.Error -> {
                ErrorContent(message = state.message)
            }
        }
    }
}

@Composable
private fun CheckingCreditsContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Checking credits...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
