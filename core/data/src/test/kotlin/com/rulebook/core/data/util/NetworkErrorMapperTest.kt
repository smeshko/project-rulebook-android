package com.rulebook.core.data.util

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.assertEquals

class NetworkErrorMapperTest {

    @Test
    fun `maps SocketTimeoutException to timeout message`() {
        val result = NetworkErrorMapper.mapToUserMessage(SocketTimeoutException("timeout"))
        assertEquals("The request took too long. Please try again.", result)
    }

    @Test
    fun `maps UnknownHostException to no internet message`() {
        val result = NetworkErrorMapper.mapToUserMessage(UnknownHostException("no host"))
        assertEquals("No internet connection. Please check your network.", result)
    }

    @Test
    fun `maps HttpException 500 to server error message`() {
        val response = Response.error<Any>(500, "".toResponseBody(null))
        val result = NetworkErrorMapper.mapToUserMessage(HttpException(response))
        assertEquals("Server error. Please try again later.", result)
    }

    @Test
    fun `maps HttpException 503 to server error message`() {
        val response = Response.error<Any>(503, "".toResponseBody(null))
        val result = NetworkErrorMapper.mapToUserMessage(HttpException(response))
        assertEquals("Server error. Please try again later.", result)
    }

    @Test
    fun `maps HttpException 400 to request failed message`() {
        val response = Response.error<Any>(400, "".toResponseBody(null))
        val result = NetworkErrorMapper.mapToUserMessage(HttpException(response))
        assertEquals("Request failed. Please try again.", result)
    }

    @Test
    fun `maps HttpException 404 to request failed message`() {
        val response = Response.error<Any>(404, "".toResponseBody(null))
        val result = NetworkErrorMapper.mapToUserMessage(HttpException(response))
        assertEquals("Request failed. Please try again.", result)
    }

    @Test
    fun `maps null cause to generic message`() {
        val result = NetworkErrorMapper.mapToUserMessage(null)
        assertEquals("Something went wrong. Please try again.", result)
    }

    @Test
    fun `maps unknown exception to generic message`() {
        val result = NetworkErrorMapper.mapToUserMessage(IllegalStateException("unknown"))
        assertEquals("Something went wrong. Please try again.", result)
    }
}
