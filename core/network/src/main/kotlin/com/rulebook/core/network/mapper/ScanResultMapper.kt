package com.rulebook.core.network.mapper

import com.rulebook.core.model.ScanResult
import com.rulebook.core.network.model.AnalyzeResponse

/**
 * Extension function to convert an AnalyzeResponse from the API to a ScanResult domain model.
 */
fun AnalyzeResponse.toDomain(): ScanResult = ScanResult(
    gameTitle = gameTitle,
    confidence = confidence,
    thumbnailUrl = thumbnailUrl
)

/**
 * Extension function to convert a list of AnalyzeResponses to a list of ScanResult domain models.
 */
fun List<AnalyzeResponse>.toDomainList(): List<ScanResult> = map { it.toDomain() }
