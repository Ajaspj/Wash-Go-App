package com.example.waggoapp.data.network

import android.content.Context
import com.example.waggoapp.data.local.TokenManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit Network Client configured for local physical device / emulator backend access.
 * Incorporates TokenManager, AuthInterceptor, and Mutex-based TokenAuthenticator.
 */
object ApiClient {

    @Volatile
    private var isInitialized = false

    private lateinit var tokenManager: TokenManager
    private lateinit var tokenAuthenticator: TokenAuthenticator

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    fun init(context: Context) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    tokenManager = TokenManager.getInstance(context)
                    tokenAuthenticator = TokenAuthenticator(tokenManager, gson)
                    isInitialized = true
                }
            }
        }
    }

    fun getTokenManager(): TokenManager {
        check(isInitialized) { "ApiClient must be initialized with context via ApiClient.init(context)" }
        return tokenManager
    }

    fun getTokenAuthenticator(): TokenAuthenticator {
        check(isInitialized) { "ApiClient must be initialized with context via ApiClient.init(context)" }
        return tokenAuthenticator
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        check(isInitialized) { "ApiClient must be initialized with context via ApiClient.init(context)" }

        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenManager))
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private var currentBaseUrl: String? = null
    private var retrofitInstance: Retrofit? = null

    val apiService: WashGoApiService
        get() {
            check(isInitialized) { "ApiClient must be initialized with context via ApiClient.init(context)" }
            val activeBaseUrl = ApiConfig.getBaseUrl()

            synchronized(this) {
                if (retrofitInstance == null || currentBaseUrl != activeBaseUrl) {
                    currentBaseUrl = activeBaseUrl
                    retrofitInstance = Retrofit.Builder()
                        .baseUrl(activeBaseUrl)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()
                }
                return retrofitInstance!!.create(WashGoApiService::class.java)
            }
        }
}
