package com.rulebook.feature.generation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.data.repository.ScanRepository
import com.rulebook.core.model.Game
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
 * @param gameRepository Repository for saving games to database.
 * @param creditRepository Repository for credit management.
 */
class GenerationViewModel(
    savedStateHandle: SavedStateHandle,
    private val analyticsManager: AnalyticsManager,
    private val scanRepository: ScanRepository,
    private val gameRepository: GameRepository,
    private val creditRepository: CreditRepository,
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
                            // Story 5.6: Generate rules for the identified game
                            generateRules(scanResult.gameTitle, scanResult.thumbnailUrl)
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
        val scanResult = _uiState.value.scanResult ?: return
        val confidence = scanResult.confidence
        _uiState.update { it.copy(showConfirmation = false, gameTitleDisplay = scanResult.gameTitle) }
        trackScanConfirmed(confidence)
        updatePhase(ScanPhase.GENERATING_RULES)
        // Story 5.6: Generate rules for the confirmed game
        generationJob = viewModelScope.launch {
            generateRules(scanResult.gameTitle, scanResult.thumbnailUrl)
        }
    }

    /**
     * Called when the user rejects the identified game and wants manual entry.
     * Tracks analytics and shows the manual entry UI.
     */
    fun onRejectGame() {
        val confidence = _uiState.value.scanResult?.confidence ?: return
        _uiState.update { it.copy(showConfirmation = false, showManualEntry = true) }
        trackScanManualEntry(confidence)
    }

    /**
     * Called when the manual game name text changes.
     * Updates the manual game name in the UI state.
     */
    fun onManualGameNameChanged(name: String) {
        _uiState.update { it.copy(manualGameName = name) }
    }

    /**
     * Called when the user submits a manually entered game name.
     * Validates the name is not blank, clears manual entry, sets the game title,
     * tracks analytics, and advances to rules generation.
     */
    fun onManualGameNameSubmitted() {
        val name = _uiState.value.manualGameName.trim()
        if (name.isBlank()) return

        val thumbnailUrl = _uiState.value.scanResult?.thumbnailUrl
        _uiState.update {
            it.copy(
                showManualEntry = false,
                gameTitleDisplay = name
            )
        }
        trackScanManualNameSubmitted(name)
        updatePhase(ScanPhase.GENERATING_RULES)
        // Story 5.6: Generate rules for the manually entered game
        generationJob = viewModelScope.launch {
            generateRules(name, thumbnailUrl)
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

    private fun trackScanManualNameSubmitted(gameName: String) {
        try {
            analyticsManager.trackScanManualNameSubmitted(gameName)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to track scan manual name submitted analytics", e)
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

    /**
     * Generates rules for the identified game by calling the repository.
     *
     * Tracks generation start time, calls the API, handles success/error,
     * updates state with the generated rules, and advances to SAVING_RULES phase.
     *
     * @param gameTitle The name of the game to generate rules for
     * @param thumbnailUrl Optional thumbnail URL from the scan result
     */
    private suspend fun generateRules(gameTitle: String, thumbnailUrl: String?) {
        val startTime = System.currentTimeMillis()

        val result = scanRepository.generateRules(gameTitle, thumbnailUrl)

        val durationMs = System.currentTimeMillis() - startTime

        when (result) {
            is Result.Success -> {
                val rules = result.data
                _uiState.update { it.copy(rules = rules) }
                trackScanGenerationComplete(gameTitle, durationMs)
                updatePhase(ScanPhase.SAVING_RULES)
                // Story 5.7: Save rules to local database
                saveRulesAndNavigate()
            }
            is Result.Error -> {
                _uiState.update { it.copy(error = result.message) }
                _events.send(GenerationEvent.Error(result.message))
                trackScanFailed(categorizeError(result.cause))
                // Do NOT advance phase on error
            }
        }
    }

    /**
     * Saves the generated rules to the local database and navigates to the Rules screen.
     *
     * Story 5.7 implementation:
     * - Constructs a Game from current UI state
     * - Calls gameRepository.saveGameWithRules() with transaction
     * - Deducts credit on success
     * - Emits NavigateToRules event
     * - On error, sets error state and emits Error event
     */
    private suspend fun saveRulesAndNavigate() {
        val currentState = _uiState.value
        val rules = currentState.rules ?: run {
            Log.e(TAG, "saveRulesAndNavigate called but rules is null")
            _uiState.update { it.copy(error = "No rules to save") }
            _events.send(GenerationEvent.Error("No rules to save"))
            return
        }

        val gameTitle = currentState.gameTitleDisplay ?: run {
            Log.e(TAG, "saveRulesAndNavigate called but gameTitleDisplay is null")
            _uiState.update { it.copy(error = "No game title available") }
            _events.send(GenerationEvent.Error("No game title available"))
            return
        }

        val thumbnailUrl = currentState.scanResult?.thumbnailUrl

        // Construct Game domain model (id will be generated by repository)
        val currentTimeMillis = System.currentTimeMillis()
        val game = Game(
            id = "", // Will be generated by repository
            title = gameTitle,
            thumbnailUrl = thumbnailUrl,
            createdAt = currentTimeMillis,
            lastAccessedAt = currentTimeMillis
        )

        // Build a JSON representation of the rules for future re-parsing
        // Uses simple escaping to avoid Android framework dependencies in tests
        fun escapeJson(s: String) = s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

        val rawJson = buildString {
            append("{")
            append("\"gameTitle\":\"${escapeJson(gameTitle)}\",")
            append("\"overview\":\"${escapeJson(rules.overview.content)}\",")
            append("\"setup\":\"${escapeJson(rules.setup.content)}\",")
            append("\"firstRound\":\"${escapeJson(rules.firstRound.content)}\",")
            append("\"advanced\":\"${escapeJson(rules.advanced.content)}\",")
            append("\"timestamp\":$currentTimeMillis")
            append("}")
        }

        // Save game and rules to database
        when (val saveResult = gameRepository.saveGameWithRules(game, rules, rawJson)) {
            is Result.Success -> {
                val gameId = saveResult.data

                // Deduct credit — wrapped in try/catch so IO errors don't block navigation
                try {
                    val creditDeducted = creditRepository.deductCredit()
                    if (!creditDeducted) {
                        Log.w(TAG, "Credit deduction returned false after save - balance may already be 0")
                    }
                } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Credit deduction failed after successful save", e)
                }

                // Update progress to 100%
                _uiState.update { it.copy(overallProgress = 1.0f) }

                // Navigate to Rules screen
                _events.send(GenerationEvent.NavigateToRules(gameId))
            }
            is Result.Error -> {
                Log.e(TAG, "Failed to save game and rules", saveResult.cause)
                _uiState.update { it.copy(error = saveResult.message) }
                _events.send(GenerationEvent.Error(saveResult.message))
            }
        }
    }

    /**
     * Categorizes an error throwable into a user-friendly error type string
     * for analytics tracking.
     */
    private fun categorizeError(cause: Throwable?): String {
        return when (cause) {
            is java.net.SocketTimeoutException -> "timeout"
            is java.net.UnknownHostException -> "no_internet"
            else -> {
                // Check for HttpException by class name to avoid direct dependency
                if (cause?.javaClass?.simpleName == "HttpException") {
                    // Attempt to extract status code via reflection
                    try {
                        val codeMethod = cause.javaClass.getMethod("code")
                        val statusCode = codeMethod.invoke(cause) as? Int
                        when (statusCode) {
                            in 500..599 -> "server_error"
                            else -> "request_failed"
                        }
                    } catch (e: Exception) {
                        "request_failed"
                    }
                } else {
                    "unknown"
                }
            }
        }
    }

    private fun trackScanGenerationComplete(gameName: String, durationMs: Long) {
        try {
            analyticsManager.trackScanGenerationComplete(gameName, durationMs)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to track scan generation complete analytics", e)
        }
    }

    private fun trackScanFailed(errorType: String) {
        try {
            analyticsManager.trackScanFailed(errorType)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to track scan failed analytics", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        generationJob?.cancel()
        Log.d(TAG, "GenerationViewModel cleared")
    }
}
