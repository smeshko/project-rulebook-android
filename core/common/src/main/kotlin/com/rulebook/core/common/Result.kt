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

/**
 * Executes the given action only if this is a Success result.
 * Returns the original Result for chaining.
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Executes the given action only if this is an Error result.
 * Returns the original Result for chaining.
 */
inline fun <T> Result<T>.onError(action: (Result.Error) -> Unit): Result<T> {
    if (this is Result.Error) action(this)
    return this
}

/**
 * Returns the data if this is a Success result, null otherwise.
 */
fun <T> Result<T>.getOrNull(): T? =
    when (this) {
        is Result.Success -> data
        is Result.Error -> null
    }

/**
 * Returns the data if this is a Success result, the provided default otherwise.
 */
fun <T> Result<T>.getOrDefault(default: T): T =
    when (this) {
        is Result.Success -> data
        is Result.Error -> default
    }

/**
 * Returns the data if this is a Success result, or computes a value from the Error otherwise.
 */
inline fun <T> Result<T>.getOrElse(onError: (Result.Error) -> T): T =
    when (this) {
        is Result.Success -> data
        is Result.Error -> onError(this)
    }

/**
 * Applies the appropriate function based on whether this is a Success or Error,
 * returning the result.
 */
inline fun <T, R> Result<T>.fold(
    onSuccess: (T) -> R,
    onError: (Result.Error) -> R
): R = when (this) {
    is Result.Success -> onSuccess(data)
    is Result.Error -> onError(this)
}
