package com.rulebook.core.model

data class ValidationResult(
    val status: ValidationStatus,
    val creditsGranted: Int,
)
