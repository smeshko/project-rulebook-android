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
}
