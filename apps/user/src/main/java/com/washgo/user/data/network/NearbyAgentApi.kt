package com.washgo.user.data.network

import com.washgo.user.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface NearbyAgentApi {
    @GET("api/bookings/search-nearby")
    suspend fun searchNearby(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radiusMeters") radiusMeters: Double = 5000.0
    ): ApiResponse<List<NearbyAgentDto>>

    @retrofit2.http.POST("api/bookings/dispatch")
    suspend fun dispatchBooking(
        @retrofit2.http.Body request: CreateDispatchRequest
    ): ApiResponse<DispatchOfferResponse>
}

object ApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val nearbyAgentApi: NearbyAgentApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NearbyAgentApi::class.java)
    }
}
