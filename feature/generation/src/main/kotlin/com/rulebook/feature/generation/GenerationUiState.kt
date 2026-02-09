package com.rulebook.feature.generation

import com.rulebook.core.model.Rules
import com.rulebook.core.model.ScanResult

/**
 * UI state for the Generation/Progress screen.
 *
 * Tracks the current phase of the scan pipeline, overall progress,
 * and whether the operation has been cancelled or encountered an error.
 *
 * @param currentPhase The current phase of the scan pipeline.
 * @param overallProgress The overall progress as a fraction (0.0-1.0).
 * @param isCancelling True when cancel has been requested but not yet completed.
 * @param error An error message if the operation failed, null otherwise.
 * @param imageUri The URI of the image being processed.
 * @param scanResult The result from image analysis, null until analysis completes.
 * @param showManualEntry True when the manual game name entry UI should be displayed.
 * @param manualGameName The current text value of the manual game name input.
 * @param rules The generated rules for the identified game, null until generation completes.
 * @param isFallbackInProgress True when the fallback AI model is being tried.
 * @param fallbackMessage Message to display during fallback attempt, null otherwise.
 */
data class GenerationUiState(
    val currentPhase: ScanPhase = ScanPhase.PROCESSING_IMAGE,
    val overallProgress: Float = 0f,
    val isCancelling: Boolean = false,
    val error: String? = null,
    val imageUri: String = "",
    val scanResult: ScanResult? = null,
    val showConfirmation: Boolean = false,
    val gameTitleDisplay: String? = null,
    val showManualEntry: Boolean = false,
    val manualGameName: String = "",
    val rules: Rules? = null,
    val isFallbackInProgress: Boolean = false,
    val fallbackMessage: String? = null,
)
