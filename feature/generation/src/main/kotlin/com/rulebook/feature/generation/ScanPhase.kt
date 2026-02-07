package com.rulebook.feature.generation

/**
 * Represents the phases of the scan/generation pipeline.
 *
 * Each phase has a display name, a descriptive message, and a progress range
 * representing the portion of overall progress it occupies.
 *
 * Phase progression (FR26):
 * 1. Processing Image (0-15%)
 * 2. Analyzing Image (15-40%)
 * 3. Identifying Game (40-60%)
 * 4. Generating Rules (60-90%)
 * 5. Saving Rules (90-100%)
 *
 * @param displayName The user-facing name of this phase.
 * @param message A descriptive message explaining the current action.
 * @param startProgress The starting progress percentage (0.0-1.0).
 * @param endProgress The ending progress percentage (0.0-1.0).
 */
enum class ScanPhase(
    val displayName: String,
    val message: String,
    val startProgress: Float,
    val endProgress: Float
) {
    /** Phase 1: Compressing and preparing the captured image. */
    PROCESSING_IMAGE(
        displayName = "Processing Image",
        message = "Preparing your photo for analysis...",
        startProgress = 0f,
        endProgress = 0.15f
    ),

    /** Phase 2: Sending image to AI for visual analysis. */
    ANALYZING_IMAGE(
        displayName = "Analyzing Image",
        message = "Examining the game box...",
        startProgress = 0.15f,
        endProgress = 0.40f
    ),

    /** Phase 3: Matching the analysis result to a known game. */
    IDENTIFYING_GAME(
        displayName = "Identifying Game",
        message = "Finding your game in our database...",
        startProgress = 0.40f,
        endProgress = 0.60f
    ),

    /** Phase 4: Generating structured rules from the identified game. */
    GENERATING_RULES(
        displayName = "Generating Rules",
        message = "Creating your personalized rulebook...",
        startProgress = 0.60f,
        endProgress = 0.90f
    ),

    /** Phase 5: Persisting generated rules to the local database. */
    SAVING_RULES(
        displayName = "Saving Rules",
        message = "Saving your rulebook...",
        startProgress = 0.90f,
        endProgress = 1.0f
    );

    /**
     * The ordinal index of this phase (0-based).
     * Useful for UI rendering of phase indicators.
     */
    val phaseNumber: Int get() = ordinal + 1

    /**
     * Total number of scan phases.
     */
    companion object {
        val TOTAL_PHASES = entries.size
    }
}
