package com.washgo.agent.data.network

import com.washgo.agent.data.local.AgentTokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AgentAuthInterceptor(
    private val tokenManager: AgentTokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenManager.getAccessToken()

        return if (!token.isNullOrBlank()) {
            val authenticatedRequest = original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(original)
        }
    }
}
