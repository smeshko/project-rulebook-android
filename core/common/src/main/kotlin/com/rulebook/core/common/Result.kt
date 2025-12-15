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

/**
 * Transforms the data of a Success result while preserving Error.
 * Returns a new Result with the transformed type.
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
    }
