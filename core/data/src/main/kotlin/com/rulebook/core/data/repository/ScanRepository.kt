package com.rulebook.core.data.repository

import com.rulebook.core.common.Result
import com.rulebook.core.model.Rules
import com.rulebook.core.model.ScanResult

interface ScanRepository {

    suspend fun analyzeImage(imageUri: String): Result<ScanResult>

    suspend fun generateRules(gameTitle: String, thumbnailUrl: String? = null): Result<Rules>
}
