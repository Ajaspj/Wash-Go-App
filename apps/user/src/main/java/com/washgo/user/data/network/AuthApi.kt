package com.washgo.user.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/otp/send")
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): ApiResponse<SendOtpResponse>

    @POST("api/v1/auth/otp/verify")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): ApiResponse<AuthResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequest
    ): ApiResponse<AuthResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<Unit>

    @GET("api/v1/users/me")
    suspend fun getProfile(): ApiResponse<UserProfile>
}
