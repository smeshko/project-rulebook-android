package com.rulebook.feature.generation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rulebook.core.designsystem.component.ButtonVariant
import com.rulebook.core.designsystem.component.PhaseItem
import com.rulebook.core.designsystem.component.PhaseStatus
import com.rulebook.core.designsystem.component.ProgressPhaseIndicator
import com.rulebook.core.designsystem.component.RulebookButton
import com.rulebook.core.designsystem.component.RulebookHeaderBar
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.feature.generation.components.ConfirmationContent
import com.rulebook.feature.generation.components.ErrorContent
import com.rulebook.feature.generation.components.ManualEntryContent
import org.koin.androidx.compose.koinViewModel

/**
 * Generation screen composable displaying scan progress with phase indicators.
 *
 * Shows a vertical list of 5 pipeline phases, an overall progress bar,
 * phase-specific messages, and a cancel button.
 *
 * @param modifier Optional modifier for the screen container.
 * @param viewModel The ViewModel managing generation state.
 * @param onNavigateBack Callback invoked when user cancels or navigates back.
 */
@Composable
fun GenerationScreen(
    modifier: Modifier = Modifier,
    viewModel: GenerationViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToRules: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Collect one-time events for navigation
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GenerationEvent.Cancelled -> onNavigateBack()
                is GenerationEvent.RetryFromCamera -> {
                    // Story 5.9: User chose to retry from error screen
                    onNavigateBack()
                }
                is GenerationEvent.Error -> {
                    // Story 5.9: Error events replaced with in-screen error state
                    // This branch kept for backward compatibility during migration
                    onNavigateBack()
                }
                is GenerationEvent.AutoProceeding -> {
                    // Brief flash handled in UI state, no navigation needed
                }
                is GenerationEvent.NavigateToRules -> {
                    onNavigateToRules(event.gameId)
                }
            }
        }
    }

    GenerationScreenContent(
        uiState = uiState,
        onCancel = viewModel::cancel,
        onConfirmGame = viewModel::onConfirmGame,
        onRejectGame = viewModel::onRejectGame,
        onManualGameNameChanged = viewModel::onManualGameNameChanged,
        onManualGameNameSubmitted = viewModel::onManualGameNameSubmitted,
        onRetry = viewModel::onRetry,
        onErrorManualEntry = viewModel::onErrorManualEntry,
        modifier = modifier
    )
}

/**
 * Stateless content composable for the generation screen.
 * Separated from the stateful [GenerationScreen] for preview and testing.
 *
 * Story 5.9: Rendering priority (highest to lowest):
 * 1. Error screen (showError)
 * 2. Manual entry (showManualEntry)
 * 3. Confirmation screen (showConfirmation)
 * 4. Progress indicator (default)
 */
@Composable
internal fun GenerationScreenContent(
    uiState: GenerationUiState,
    onCancel: () -> Unit,
    onConfirmGame: () -> Unit = {},
    onRejectGame: () -> Unit = {},
    onManualGameNameChanged: (String) -> Unit = {},
    onManualGameNameSubmitted: () -> Unit = {},
    onRetry: () -> Unit = {},
    onErrorManualEntry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Story 5.9: Error screen has highest priority
    if (uiState.showError) {
        ErrorContent(
            errorMessage = uiState.error ?: "Something went wrong. Please try again.",
            onRetry = onRetry,
            onManualEntry = onErrorManualEntry,
            modifier = modifier
        )
    } else if (uiState.showManualEntry) {
        ManualEntryContent(
            gameName = uiState.manualGameName,
            onGameNameChanged = onManualGameNameChanged,
            onSubmit = onManualGameNameSubmitted,
            onCancel = onCancel,
            modifier = modifier
        )
    } else if (uiState.showConfirmation && uiState.scanResult != null) {
        ConfirmationContent(
            gameTitle = uiState.scanResult.gameTitle,
            confidence = uiState.scanResult.confidence,
            onConfirm = onConfirmGame,
            onReject = onRejectGame,
            modifier = modifier
        )
    } else {
        val spacing = RulebookTheme.spacing

        // Determine header title — briefly show game name during auto-proceed
        val headerTitle = uiState.gameTitleDisplay ?: "Analyzing"

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header bar
            RulebookHeaderBar(title = headerTitle)

            // Content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = spacing.xl),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Phase indicator with progress
                // Story 5.8: Show fallback message when fallback is in progress
                val currentMessage = uiState.fallbackMessage ?: uiState.currentPhase.message
                ProgressPhaseIndicator(
                    phases = buildPhaseItems(uiState.currentPhase),
                    overallProgress = uiState.overallProgress,
                    currentMessage = currentMessage
                )
            }

            // Cancel button at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md)
                    .padding(bottom = spacing.xl),
                contentAlignment = Alignment.Center
            ) {
                RulebookButton(
                    text = if (uiState.isCancelling) "Cancelling..." else "Cancel",
                    onClick = onCancel,
                    variant = ButtonVariant.Secondary,
                    enabled = !uiState.isCancelling,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Builds the list of [PhaseItem]s based on the current scan phase.
 *
 * Phases before the current one are marked COMPLETED,
 * the current phase is ACTIVE, and remaining phases are PENDING.
 */
internal fun buildPhaseItems(currentPhase: ScanPhase): List<PhaseItem> {
    return ScanPhase.entries.map { phase ->
        PhaseItem(
            name = phase.displayName,
            status = when {
                phase.ordinal < currentPhase.ordinal -> PhaseStatus.COMPLETED
                phase == currentPhase -> PhaseStatus.ACTIVE
                else -> PhaseStatus.PENDING
            },
            phaseNumber = phase.phaseNumber
        )
    }
}
