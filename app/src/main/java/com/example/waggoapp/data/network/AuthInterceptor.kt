package com.example.waggoapp.data.network

import com.example.waggoapp.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches JWT Bearer authentication token from secure storage.
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
            .header("Accept", "application/json")

        // If body exists and Content-Type not already set, specify application/json
        if (originalRequest.body != null && originalRequest.header("Content-Type") == null) {
            builder.header("Content-Type", "application/json")
        }

        val token = tokenManager.getAccessToken()
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}
