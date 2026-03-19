package com.rulebook.core.data.util

import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorMapper {

    fun mapToUserMessage(cause: Throwable?): String = when (cause) {
        is SocketTimeoutException -> "The request took too long. Please try again."
        is UnknownHostException -> "No internet connection. Please check your network."
        is retrofit2.HttpException -> mapHttpException(cause)
        else -> "Something went wrong. Please try again."
    }

    private fun mapHttpException(exception: retrofit2.HttpException): String =
        when (exception.code()) {
            in 500..599 -> "Server error. Please try again later."
            else -> "Request failed. Please try again."
        }
}
