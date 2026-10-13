package com.example.waggoapp

import com.example.waggoapp.data.network.ApiErrorHandler
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class ApiErrorHandlerTest {

    @Test
    fun testParseExceptionNetwork() {
        val ioException = IOException("Connection reset")
        val message = ApiErrorHandler.parseException(ioException)
        assertEquals("Connection reset", message)
    }

    @Test
    fun testParseExceptionTimeout() {
        val timeout = SocketTimeoutException("Read timed out")
        val message = ApiErrorHandler.parseException(timeout)
        assertTrue(message.contains("timed out"))
    }

    @Test
    fun testParseHttpValidationError() {
        val json = """
            {
                "timestamp": "2026-10-12T08:00:00Z",
                "status": 400,
                "error": "Bad Request",
                "code": "VALIDATION_ERROR",
                "message": "Invalid request fields",
                "fieldErrors": {
                    "password": "Password must contain uppercase, lowercase, digit, and special char",
                    "email": "Invalid email format"
                }
            }
        """.trimIndent()

        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())
        val response = Response.error<Any>(400, responseBody)

        val parsed = ApiErrorHandler.parseError(response)
        assertTrue(parsed.contains("Invalid request fields"))
        assertTrue(parsed.contains("Password must contain uppercase"))
    }

    @Test
    fun testParseHttpUnauthorizedError() {
        val json = """
            {
                "status": 401,
                "code": "UNAUTHORIZED",
                "message": "Bad credentials"
            }
        """.trimIndent()

        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())
        val response = Response.error<Any>(401, responseBody)

        val parsed = ApiErrorHandler.parseError(response)
        assertEquals("Bad credentials", parsed)
    }
}
