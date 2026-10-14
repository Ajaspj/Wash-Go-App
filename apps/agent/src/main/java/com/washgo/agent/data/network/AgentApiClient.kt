package com.washgo.agent.data.network

import android.content.Context
import com.washgo.agent.BuildConfig
import com.washgo.agent.data.local.AgentTokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AgentApiClient private constructor(context: Context) {

    private val tokenManager = AgentTokenManager.getInstance(context)

    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.BASIC
        }
    }

    // Unauthenticated OkHttp client for login / OTP
    private val authOkHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Authenticated OkHttp client with token injection & authenticator
    private val authenticatedOkHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AgentAuthInterceptor(tokenManager))
        .authenticator(AgentTokenAuthenticator(tokenManager))
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val authApi: AgentAuthApi = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(authOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AgentAuthApi::class.java)

    val agentApi: AgentApi = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(authenticatedOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AgentApi::class.java)

    companion object {
        @Volatile
        private var instance: AgentApiClient? = null

        fun getInstance(context: Context): AgentApiClient {
            return instance ?: synchronized(this) {
                instance ?: AgentApiClient(context.applicationContext).also { instance = it }
            }
        }
    }
}
