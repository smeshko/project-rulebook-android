package com.rulebook.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {

    // Task 1: Result Sealed Class Tests
    @Test
    fun `Success holds data correctly`() {
        val data = "test data"
        val result: Result<String> = Result.Success(data)

        assertTrue(result is Result.Success)
        assertEquals(data, (result as Result.Success).data)
    }

    @Test
    fun `Success holds complex data correctly`() {
        data class TestData(val id: Int, val name: String)
        val data = TestData(1, "test")
        val result: Result<TestData> = Result.Success(data)

        assertTrue(result is Result.Success)
        assertEquals(data, (result as Result.Success).data)
    }

    @Test
    fun `Error holds message correctly`() {
        val message = "Error occurred"
        val result: Result<String> = Result.Error(message)

        assertTrue(result is Result.Error)
        assertEquals(message, (result as Result.Error).message)
    }

    @Test
    fun `Error holds message and cause correctly`() {
        val message = "Error occurred"
        val cause = RuntimeException("Original error")
        val result: Result<String> = Result.Error(message, cause)

        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertEquals(message, error.message)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `Error cause can be null`() {
        val message = "Error occurred"
        val result: Result<String> = Result.Error(message)

        assertTrue(result is Result.Error)
        assertNull((result as Result.Error).cause)
    }

    @Test
    fun `Result is sealed with only Success and Error subtypes`() {
        val success: Result<Int> = Result.Success(42)
        val error: Result<Int> = Result.Error("error")

        // This verifies exhaustive when matching
        val description = when (success) {
            is Result.Success -> "success"
            is Result.Error -> "error"
        }
        assertEquals("success", description)

        val description2 = when (error) {
            is Result.Success -> "success"
            is Result.Error -> "error"
        }
        assertEquals("error", description2)
    }

    // Task 2: map() Extension Tests
    @Test
    fun `map transforms Success data`() {
        val result: Result<Int> = Result.Success(5)

        val mapped = result.map { it * 2 }

        assertTrue(mapped is Result.Success)
        assertEquals(10, (mapped as Result.Success).data)
    }

    @Test
    fun `map preserves Error without transformation`() {
        val error: Result<Int> = Result.Error("error message")

        val mapped = error.map { it * 2 }

        assertTrue(mapped is Result.Error)
        assertEquals("error message", (mapped as Result.Error).message)
    }

    @Test
    fun `map can change result type`() {
        val result: Result<Int> = Result.Success(42)

        val mapped: Result<String> = result.map { "Number: $it" }

        assertTrue(mapped is Result.Success)
        assertEquals("Number: 42", (mapped as Result.Success).data)
    }

    @Test
    fun `map preserves Error cause`() {
        val cause = RuntimeException("original")
        val error: Result<Int> = Result.Error("error", cause)

        val mapped = error.map { it.toString() }

        assertTrue(mapped is Result.Error)
        val mappedError = mapped as Result.Error
        assertEquals("error", mappedError.message)
        assertEquals(cause, mappedError.cause)
    }

    // Task 3: onSuccess() Extension Tests
    @Test
    fun `onSuccess executes action on Success`() {
        var captured: String? = null
        val result: Result<String> = Result.Success("hello")

        result.onSuccess { captured = it }

        assertEquals("hello", captured)
    }

    @Test
    fun `onSuccess does not execute action on Error`() {
        var executed = false
        val result: Result<String> = Result.Error("error")

        result.onSuccess { executed = true }

        assertEquals(false, executed)
    }

    @Test
    fun `onSuccess returns original Result for chaining`() {
        val original: Result<Int> = Result.Success(42)

        val returned = original.onSuccess { }

        assertTrue(returned === original)
    }

    @Test
    fun `onSuccess preserves Error for chaining`() {
        val original: Result<Int> = Result.Error("error")

        val returned = original.onSuccess { }

        assertTrue(returned === original)
    }

    // Task 4: onError() Extension Tests
    @Test
    fun `onError executes action on Error`() {
        var capturedMessage: String? = null
        val result: Result<String> = Result.Error("error message")

        result.onError { capturedMessage = it.message }

        assertEquals("error message", capturedMessage)
    }

    @Test
    fun `onError does not execute action on Success`() {
        var executed = false
        val result: Result<String> = Result.Success("success")

        result.onError { executed = true }

        assertEquals(false, executed)
    }

    @Test
    fun `onError returns original Result for chaining`() {
        val original: Result<Int> = Result.Error("error")

        val returned = original.onError { }

        assertTrue(returned === original)
    }

    @Test
    fun `onError preserves Success for chaining`() {
        val original: Result<Int> = Result.Success(42)

        val returned = original.onError { }

        assertTrue(returned === original)
    }

    @Test
    fun `onError receives Error with cause`() {
        val cause = RuntimeException("original")
        var capturedCause: Throwable? = null
        val result: Result<String> = Result.Error("error", cause)

        result.onError { capturedCause = it.cause }

        assertEquals(cause, capturedCause)
    }
}
