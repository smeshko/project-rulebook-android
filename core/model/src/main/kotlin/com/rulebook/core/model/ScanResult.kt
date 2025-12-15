package com.rulebook.core.model

/**
 * Domain model representing the result of scanning a game box or image.
 * Contains the recognized game title, confidence score, and optional thumbnail.
 * Pure Kotlin data class with no Android dependencies.
 */
data class ScanResult(
    val gameTitle: String,
    val confidence: Float,
    val thumbnailUrl: String?
)
