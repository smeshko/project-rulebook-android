package com.rulebook.feature.generation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.ScanRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "GenerationViewModel"

/**
 * ViewModel for the Generation/Progress screen.
 *
 * Manages the scan pipeline phase state and provides progress tracking.
 * Orchestrates the image analysis API call and maps results to UI state.
 *
 * Follows the MVI pattern with [GenerationUiState] as the single source of truth
 * and [GenerationEvent] for one-time navigation events.
 *
 * @param savedStateHandle Handle to restore navigation arguments.
 * @param analyticsManager Manager for tracking analytics events.
 * @param scanRepository Repository for image analysis API calls.
 */
class GenerationViewModel(
    savedStateHandle: SavedStateHandle,
    private val analyticsManager: AnalyticsManager,
    private val scanRepository: ScanRepository,
    private val autoProceedThreshold: Float = DEFAULT_AUTO_PROCEED_THRESHOLD,
) : ViewModel() {

    companion object {
        const val DEFAULT_AUTO_PROCEED_THRESHOLD = 0.80f
    }

    private val _uiState = MutableStateFlow(GenerationUiState())
    val uiState: StateFlow<GenerationUiState> = _uiState.asStateFlow()

    private val _events = Channel<GenerationEvent>(Channel.BUFFERED)
    val events: Flow<GenerationEvent> = _events.receiveAsFlow()

    /** Tracks the active generation job so it can be cancelled. */
    private var generationJob: Job? = null

    init {
        // Extract image URI from navigation arguments
        val imageUri = savedStateHandle.get<String>("imageUri") ?: ""
        _uiState.update { it.copy(imageUri = imageUri) }

        if (imageUri.isNotBlank()) {
            startGeneration()
        } else {
            Log.e(TAG, "Missing or blank imageUri argument")
            val message = "No image provided."
            _uiState.update { it.copy(error = message) }
            viewModelScope.launch {
                _events.send(GenerationEvent.Error(message))
            }
        }
    }

    /**
     * Starts the generation pipeline.
     *
     * Executes image processing and analysis, advancing through scan phases.
     * On success, stores the [ScanResult] and advances to IDENTIFYING_GAME.
     * On error, sets error state and emits an error event.
     */
    private fun startGeneration() {
        generationJob = viewModelScope.launch {
            try {
                trackScanStarted()

                // Phase 1: Processing Image
                updatePhase(ScanPhase.PROCESSING_IMAGE)

                // Phase 2: Analyzing Image
                updatePhase(ScanPhase.ANALYZING_IMAGE)
                val imageUri = _uiState.value.imageUri
                val result = scanRepository.analyzeImage(imageUri)

                when (result) {
                    is Result.Success -> {
                        val scanResult = result.data
                        _uiState.update { it.copy(scanResult = scanResult) }
                        updatePhase(ScanPhase.IDENTIFYING_GAME)

                        // Story 5.4: Confidence check — auto-proceed or show confirmation
                        val confidence = scanResult.confidence
                        if (confidence >= autoProceedThreshold) {
                            // High confidence: auto-proceed
                            trackScanAnalysisComplete(confidence, autoProceeded = true)
                            _uiState.update { it.copy(gameTitleDisplay = scanResult.gameTitle) }
                            _events.send(GenerationEvent.AutoProceeding(scanResult.gameTitle))
                            updatePhase(ScanPhase.GENERATING_RULES)
                            // Subsequent phases (GENERATING_RULES, SAVING_RULES)
                            // will be implemented in Stories 5.6-5.7.
                        } else {
                            // Low confidence: show confirmation screen
                            trackScanAnalysisComplete(confidence, autoProceeded = false)
                            _uiState.update { it.copy(showConfirmation = true) }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                        _events.send(GenerationEvent.Error(result.message))
                    }
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e // Respect coroutine cancellation
            } catch (e: Exception) {
                Log.e(TAG, "Generation failed", e)
                val message = "Something went wrong. Please try again."
                _uiState.update { it.copy(error = message) }
                _events.send(GenerationEvent.Error(message))
            }
        }
    }

    /**
     * Updates the current phase and adjusts progress to the phase start.
     *
     * @param phase The new scan phase.
     */
    fun updatePhase(phase: ScanPhase) {
        _uiState.update {
            it.copy(
                currentPhase = phase,
                overallProgress = phase.startProgress
            )
        }
    }

    /**
     * Updates the overall progress within the current phase.
     *
     * The progress is clamped to the current phase's range to prevent
     * progress from jumping ahead of the actual phase.
     *
     * @param progress A value between 0.0 and 1.0 representing overall progress.
     */
    fun updateProgress(progress: Float) {
        _uiState.update { state ->
            val clamped = progress.coerceIn(
                state.currentPhase.startProgress,
                state.currentPhase.endProgress
            )
            state.copy(overallProgress = clamped)
        }
    }

    /**
     * Cancels the current generation operation.
     *
     * Sets the cancelling state, cancels the generation coroutine,
     * tracks the cancellation analytics event, and emits a [GenerationEvent.Cancelled].
     */
    fun cancel() {
        if (_uiState.value.isCancelling) return // Prevent double-cancel

        _uiState.update { it.copy(isCancelling = true) }
        generationJob?.cancel()
        generationJob = null

        viewModelScope.launch {
            try {
                analyticsManager.trackEvent(
                    "scan_cancelled",
                    mapOf(
                        "phase" to _uiState.value.currentPhase.name,
                        "progress" to _uiState.value.overallProgress.toString()
                    )
                )
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to track scan cancelled analytics", e)
            }
            _events.send(GenerationEvent.Cancelled)
        }
    }

    /**
     * Called when the user confirms the identified game on the confirmation screen.
     * Clears the confirmation, tracks analytics, and advances to rules generation.
     */
    fun onConfirmGame() {
        val confidence = _uiState.value.scanResult?.confidence ?: return
        _uiState.update { it.copy(showConfirmation = false) }
        trackScanConfirmed(confidence)
        updatePhase(ScanPhase.GENERATING_RULES)
        // Subsequent phases (GENERATING_RULES, SAVING_RULES)
        // will be implemented in Stories 5.6-5.7.
    }

    /**
     * Called when the user rejects the identified game and wants manual entry.
     * Tracks analytics and emits a navigation event to manual entry.
     */
    fun onRejectGame() {
        val confidence = _uiState.value.scanResult?.confidence ?: return
        trackScanManualEntry(confidence)
        viewModelScope.launch {
            _events.send(GenerationEvent.NavigateToManualEntry)
        }
    }

    private fun trackScanAnalysisComplete(confidence: Float, autoProceeded: Boolean) {
        try {
            analyticsManager.trackScanAnalysisComplete(confidence, autoProceeded)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to track scan analysis complete analytics", e)
        }
    }

    private fun trackScanConfirmed(confidence: Float) {
        try {
            analyticsManager.trackScanConfirmed(confidence)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to track scan confirmed analytics", e)
        }
    }

    private fun trackScanManualEntry(confidence: Float) {
        try {
            analyticsManager.trackScanManualEntry(confidence)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to track scan manual entry analytics", e)
        }
    }

    private fun trackScanStarted() {
        try {
            analyticsManager.trackEvent("scan_started")
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to track scan started analytics", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        generationJob?.cancel()
        Log.d(TAG, "GenerationViewModel cleared")
    }
}
