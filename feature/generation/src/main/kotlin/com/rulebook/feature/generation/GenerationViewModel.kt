package com.rulebook.feature.generation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.analytics.AnalyticsManager
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
 * The actual API calls for image analysis and rules generation will be
 * implemented in Stories 5.3-5.7. This ViewModel currently manages the
 * UI state for progress display and cancellation.
 *
 * Follows the MVI pattern with [GenerationUiState] as the single source of truth
 * and [GenerationEvent] for one-time navigation events.
 *
 * @param savedStateHandle Handle to restore navigation arguments.
 * @param analyticsManager Manager for tracking analytics events.
 */
class GenerationViewModel(
    savedStateHandle: SavedStateHandle,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

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
            _uiState.update { it.copy(error = "No image provided.") }
            viewModelScope.launch {
                _events.send(GenerationEvent.Error("No image provided"))
            }
        }
    }

    /**
     * Starts the generation pipeline.
     *
     * Currently simulates phase progression for UI validation.
     * Will be replaced with actual API calls in Stories 5.3-5.7.
     */
    private fun startGeneration() {
        generationJob = viewModelScope.launch {
            try {
                trackScanStarted()
                // Phase 1: Processing Image
                updatePhase(ScanPhase.PROCESSING_IMAGE)

                // Actual pipeline implementation will be added in Stories 5.3-5.7.
                // For now, this ViewModel provides the phase tracking infrastructure.
                // The pipeline will call updatePhase() and updateProgress() as it
                // progresses through API calls.
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e // Respect coroutine cancellation
            } catch (e: Exception) {
                Log.e(TAG, "Generation failed", e)
                _uiState.update { it.copy(error = "Something went wrong. Please try again.") }
                _events.send(GenerationEvent.Error(e.message ?: "Unknown error"))
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
