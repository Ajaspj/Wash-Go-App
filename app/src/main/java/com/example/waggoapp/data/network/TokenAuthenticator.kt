package com.example.waggoapp.data.network

import com.example.waggoapp.data.local.TokenManager
import com.example.waggoapp.data.network.dto.ApiResponse
import com.example.waggoapp.data.network.dto.AuthResponse
import com.example.waggoapp.data.network.dto.RefreshRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

/**
 * Handles HTTP 401 Unauthorized responses with synchronized refresh-token rotation.
 * Uses a Mutex to ensure exactly ONE refresh network call occurs during concurrent 401s.
 * Atomically rotates both access token and refresh token.
 */
class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val gson: Gson = Gson()
) : Authenticator {

    private val refreshMutex = Mutex()

    // Event emitted when refresh fails and the user must be directed to Login
    private val _sessionExpiredEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredEvents: SharedFlow<Unit> = _sessionExpiredEvents.asSharedFlow()

    // Isolated OkHttpClient for refresh requests (without this authenticator to avoid recursion)
    private val refreshClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite retry loops
        if (responseCount(response) >= 3) {
            tokenManager.clear()
            _sessionExpiredEvents.tryEmit(Unit)
            return null
        }

        // Do not attempt refresh on auth endpoints themselves
        val path = response.request.url.encodedPath
        if (path.contains("/api/v1/auth/login") ||
            path.contains("/api/v1/auth/register") ||
            path.contains("/api/v1/auth/refresh")
        ) {
            return null
        }

        val failedAuthorizationHeader = response.request.header("Authorization")
        val failedAccessToken = failedAuthorizationHeader?.removePrefix("Bearer ")?.trim()

        return runBlocking {
            refreshMutex.withLock {
                val currentAccessToken = tokenManager.getAccessToken()

                // Check if another concurrent thread already successfully refreshed the token
                if (!currentAccessToken.isNullOrBlank() && currentAccessToken != failedAccessToken) {
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $currentAccessToken")
                        .build()
                }

                val currentRefreshToken = tokenManager.getRefreshToken()
                if (currentRefreshToken.isNullOrBlank()) {
                    tokenManager.clear()
                    _sessionExpiredEvents.tryEmit(Unit)
                    return@withLock null
                }

                // Execute the refresh call
                val newTokens = performTokenRefresh(currentRefreshToken)
                if (newTokens != null) {
                    // Atomically replace BOTH access token and refresh token
                    tokenManager.saveTokens(
                        accessToken = newTokens.accessToken,
                        refreshToken = newTokens.refreshToken
                    )

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()
                } else {
                    // Refresh failed (token revoked, expired, or invalid)
                    tokenManager.clear()
                    _sessionExpiredEvents.tryEmit(Unit)
                    null
                }
            }
        }
    }

    private fun performTokenRefresh(refreshToken: String): AuthResponse? {
        return try {
            val refreshPayload = gson.toJson(RefreshRequest(refreshToken))
            val requestBody = refreshPayload.toRequestBody("application/json".toMediaType())

            val baseUrl = ApiConfig.getBaseUrl()
            val refreshUrl = "${baseUrl.trimEnd('/')}/api/v1/auth/refresh"

            val refreshRequest = Request.Builder()
                .url(refreshUrl)
                .post(requestBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()

            val refreshResponse = refreshClient.newCall(refreshRequest).execute()
            if (refreshResponse.isSuccessful) {
                val responseBody = refreshResponse.body?.string()
                if (!responseBody.isNullOrBlank()) {
                    val type = object : TypeToken<ApiResponse<AuthResponse>>() {}.type
                    val apiResponse: ApiResponse<AuthResponse> = gson.fromJson(responseBody, type)
                    apiResponse.data
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
