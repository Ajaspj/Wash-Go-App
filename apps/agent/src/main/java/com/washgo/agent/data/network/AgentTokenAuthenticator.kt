package com.washgo.agent.data.network

import android.util.Log
import com.google.gson.Gson
import com.washgo.agent.BuildConfig
import com.washgo.agent.data.local.AgentTokenManager
import com.washgo.agent.data.model.RefreshTokenRequest
import com.washgo.agent.data.model.TokenResponse
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject

class AgentTokenAuthenticator(
    private val tokenManager: AgentTokenManager
) : Authenticator {

    private val gson = Gson()

    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops if refresh endpoint itself failed with 401
        if (response.request.url.encodedPath.contains("/auth/refresh")) {
            tokenManager.clear()
            return null
        }

        val refreshToken = tokenManager.getRefreshToken() ?: run {
            tokenManager.clear()
            return null
        }

        // Check if access token was already updated by another concurrent thread
        val currentAccessToken = tokenManager.getAccessToken()
        val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
        if (currentAccessToken != null && currentAccessToken != requestToken) {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentAccessToken")
                .build()
        }

        // Attempt synchronous refresh call
        try {
            val refreshUrl = BuildConfig.BASE_URL.trimEnd('/') + "/api/v1/auth/refresh"
            val payload = JSONObject().apply {
                put("refreshToken", refreshToken)
            }.toString()

            val refreshRequest = Request.Builder()
                .url(refreshUrl)
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()

            val rawClient = OkHttpClient.Builder().build()
            val refreshResponse = rawClient.newCall(refreshRequest).execute()

            if (refreshResponse.isSuccessful) {
                val bodyStr = refreshResponse.body?.string()
                if (!bodyStr.isNullOrBlank()) {
                    val json = JSONObject(bodyStr)
                    val data = json.optJSONObject("data")
                    if (data != null) {
                        val newAccess = data.optString("accessToken")
                        val newRefresh = data.optString("refreshToken", refreshToken)
                        if (newAccess.isNotBlank()) {
                            tokenManager.saveTokens(newAccess, newRefresh)
                            return response.request.newBuilder()
                                .header("Authorization", "Bearer $newAccess")
                                .build()
                        }
                    }
                }
            } else {
                Log.w("AgentTokenAuth", "Token refresh failed with code ${refreshResponse.code}")
            }
        } catch (e: Exception) {
            Log.e("AgentTokenAuth", "Token refresh error", e)
        }

        // Refresh failed, clear session
        tokenManager.clear()
        return null
    }
}
