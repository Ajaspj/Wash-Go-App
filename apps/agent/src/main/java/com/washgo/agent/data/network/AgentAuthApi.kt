package com.washgo.agent.data.network

import com.washgo.agent.data.model.AgentApiResponse
import com.washgo.agent.data.model.AgentAuthResponse
import com.washgo.agent.data.model.RefreshTokenRequest
import com.washgo.agent.data.model.RegisterAgentOtpRequest
import com.washgo.agent.data.model.SendOtpRequest
import com.washgo.agent.data.model.SendOtpResponse
import com.washgo.agent.data.model.TokenResponse
import com.washgo.agent.data.model.VerifyOtpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AgentAuthApi {

    @POST("api/v1/auth/otp/send")
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): Response<AgentApiResponse<SendOtpResponse>>

    @POST("api/v1/auth/agent/otp/verify")
    suspend fun verifyAgentOtp(
        @Body request: VerifyOtpRequest
    ): Response<AgentApiResponse<AgentAuthResponse>>

    @POST("api/v1/auth/agent/register")
    suspend fun registerAgent(
        @Body request: RegisterAgentOtpRequest
    ): Response<AgentApiResponse<AgentAuthResponse>>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshTokenRequest
    ): Response<AgentApiResponse<TokenResponse>>

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body request: RefreshTokenRequest
    ): Response<Unit>
}
