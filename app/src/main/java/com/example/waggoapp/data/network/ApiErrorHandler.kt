package com.example.waggoapp.data.network

import com.example.waggoapp.data.network.dto.ApiErrorResponse
import retrofit2.Response

/**
 * Utility to extract user-friendly error messages from backend ErrorResponse JSON.
 */
object ApiErrorHandler {

    fun <T> parseError(response: Response<T>): String {
        return try {
            val errorBodyString = response.errorBody()?.string()
            if (!errorBodyString.isNullOrBlank()) {
                val errorResponse = ApiClient.gson.fromJson(errorBodyString, ApiErrorResponse::class.java)

                // If validation errors exist, format them clearly
                if (!errorResponse.fieldErrors.isNullOrEmpty()) {
                    val fieldMessages = errorResponse.fieldErrors.values.joinToString("; ")
                    "${errorResponse.message}: $fieldMessages"
                } else if (!errorResponse.message.isNullOrBlank()) {
                    errorResponse.message
                } else {
                    "Error (${response.code()}): ${errorResponse.code}"
                }
            } else {
                when (response.code()) {
                    400 -> "Invalid request. Please check your inputs."
                    401 -> "Session expired or authentication failed."
                    403 -> "You do not have permission to perform this action."
                    404 -> "Requested resource was not found."
                    409 -> "Conflict occurred. The requested slot or data is already in use."
                    429 -> "Too many requests. Please wait a moment."
                    500 -> "Server error. Please try again shortly."
                    else -> "Request failed with HTTP status ${response.code()}."
                }
            }
        } catch (e: Exception) {
            "Network error (${response.code()}). Please try again."
        }
    }

    fun parseException(e: Throwable): String {
        return when {
            e is java.net.ConnectException -> "Cannot connect to server. Check your network or connection to http://127.0.0.1:8080."
            e is java.net.SocketTimeoutException -> "Server connection timed out. Please try again."
            e is java.net.UnknownHostException -> "Server host could not be resolved."
            !e.message.isNullOrBlank() -> e.message!!
            else -> "A network error occurred."
        }
    }
}
