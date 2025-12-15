package com.rulebook.core.common

/**
 * A generic wrapper for handling success and error states consistently across the app.
 * All repository operations should return Result<T>.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : Result<Nothing>()
}
