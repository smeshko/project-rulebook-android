package com.rulebook.core.network.api

import com.rulebook.core.network.model.AnalyzeRequest
import com.rulebook.core.network.model.AnalyzeResponse
import com.rulebook.core.network.model.GenerateRequest
import com.rulebook.core.network.model.GenerateResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for the Rulebook backend.
 */
interface RulebookApi {

    /**
     * Analyzes an image to identify a board game.
     *
     * @param request The analyze request containing image data.
     * @return The analysis response with game identification.
     */
    @POST("analyze")
    suspend fun analyzeImage(@Body request: AnalyzeRequest): AnalyzeResponse

    /**
     * Analyzes an image using a fallback AI model.
     * Triggered when primary model fails or returns very low confidence.
     *
     * @param request The analyze request containing image data.
     * @return The analysis response with game identification from fallback model.
     */
    @POST("analyze/fallback")
    suspend fun analyzeImageFallback(@Body request: AnalyzeRequest): AnalyzeResponse

    /**
     * Generates rules for a given game.
     *
     * @param request The generate request with game information.
     * @return The response containing generated rules.
     */
    @POST("generate")
    suspend fun generateRules(@Body request: GenerateRequest): GenerateResponse
}
