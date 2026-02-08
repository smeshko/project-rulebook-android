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
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Collect one-time events for navigation
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GenerationEvent.Cancelled -> onNavigateBack()
                is GenerationEvent.Error -> {
                    // Error handling will be expanded in Story 5.9
                    onNavigateBack()
                }
                is GenerationEvent.AutoProceeding -> {
                    // Brief flash handled in UI state, no navigation needed
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
        modifier = modifier
    )
}

/**
 * Stateless content composable for the generation screen.
 * Separated from the stateful [GenerationScreen] for preview and testing.
 *
 * Conditionally renders the confirmation screen when the confidence is below
 * the auto-proceed threshold, or the progress phase indicator otherwise.
 */
@Composable
internal fun GenerationScreenContent(
    uiState: GenerationUiState,
    onCancel: () -> Unit,
    onConfirmGame: () -> Unit = {},
    onRejectGame: () -> Unit = {},
    onManualGameNameChanged: (String) -> Unit = {},
    onManualGameNameSubmitted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (uiState.showManualEntry) {
        ManualEntryContent(
            gameName = uiState.manualGameName,
            onGameNameChanged = onManualGameNameChanged,
            onSubmit = onManualGameNameSubmitted,
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
                ProgressPhaseIndicator(
                    phases = buildPhaseItems(uiState.currentPhase),
                    overallProgress = uiState.overallProgress,
                    currentMessage = uiState.currentPhase.message
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
