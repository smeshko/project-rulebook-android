package com.rulebook.core.data.repository

import com.rulebook.core.common.Result
import com.rulebook.core.model.ScanResult

interface ScanRepository {

    suspend fun analyzeImage(imageUri: String): Result<ScanResult>
}
