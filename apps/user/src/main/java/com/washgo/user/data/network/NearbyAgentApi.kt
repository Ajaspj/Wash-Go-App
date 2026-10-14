package com.washgo.user.data.network

import com.google.gson.Gson
import com.washgo.user.BuildConfig
import com.washgo.user.data.local.TokenManager
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface NearbyAgentApi {
    @GET("api/bookings/search-nearby")
    suspend fun searchNearby(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radiusMeters") radiusMeters: Double = 5000.0
    ): ApiResponse<List<NearbyAgentDto>>

    @POST("api/bookings/dispatch")
    suspend fun dispatchBooking(
        @retrofit2.http.Body request: CreateDispatchRequest
    ): ApiResponse<DispatchOfferResponse>
}

object ApiClient {
    private var tokenManager: TokenManager? = null
    private val gson = Gson()

    fun initialize(manager: TokenManager) {
        tokenManager = manager
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val path = original.url.encodedPath

        // Do not attach token for public auth challenge and token rotation endpoints
        if (path.contains("/auth/otp/") || path.contains("/auth/refresh")) {
            return@Interceptor chain.proceed(original)
        }

        val accessToken = tokenManager?.getAccessToken()
        if (!accessToken.isNullOrBlank()) {
            val authenticated = original.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(authenticated)
        } else {
            chain.proceed(original)
        }
    }

    private val tokenAuthenticator = Authenticator { _: Route?, response: Response ->
        // Prevent infinite loops if refresh endpoint itself failed with 401
        if (response.request.url.encodedPath.contains("/auth/refresh")) {
            tokenManager?.clear()
            return@Authenticator null
        }

        val refreshToken = tokenManager?.getRefreshToken() ?: run {
            tokenManager?.clear()
            return@Authenticator null
        }

        synchronized(this) {
            // Check if another concurrent thread already refreshed the token
            val currentToken = tokenManager?.getAccessToken()
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()

            if (currentToken != null && currentToken != requestToken) {
                // Token was already rotated by another thread; retry with new token
                return@Authenticator response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Perform synchronous refresh call
            val refreshUrl = BuildConfig.BASE_URL.removeSuffix("/") + "/api/v1/auth/refresh"
            val refreshPayload = gson.toJson(RefreshRequest(refreshToken))
            val refreshRequest = Request.Builder()
                .url(refreshUrl)
                .post(refreshPayload.toRequestBody("application/json".toMediaType()))
                .build()

            try {
                val refreshClient = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()

                val refreshResponse = refreshClient.newCall(refreshRequest).execute()
                if (refreshResponse.isSuccessful) {
                    val bodyString = refreshResponse.body?.string()
                    val envelope = gson.fromJson<ApiResponse<AuthResponse>>(
                        bodyString,
                        object : com.google.gson.reflect.TypeToken<ApiResponse<AuthResponse>>() {}.type
                    )
                    val newAuth = envelope?.data
                    if (newAuth != null) {
                        tokenManager?.saveTokens(newAuth.accessToken, newAuth.refreshToken)
                        return@Authenticator response.request.newBuilder()
                            .header("Authorization", "Bearer ${newAuth.accessToken}")
                            .build()
                    }
                }
            } catch (e: Exception) {
                // Network or parse exception during refresh
            }

            // Refresh failed or token revoked: force clear session
            tokenManager?.clear()
            return@Authenticator null
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val nearbyAgentApi: NearbyAgentApi by lazy {
        retrofit.create(NearbyAgentApi::class.java)
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}
