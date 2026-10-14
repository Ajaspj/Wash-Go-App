package com.washgo.user.data.network

import com.google.gson.annotations.SerializedName

data class SendOtpRequest(
    @SerializedName("phone") val phone: String
)

data class SendOtpResponse(
    @SerializedName("phone") val phone: String,
    @SerializedName("expiresInSeconds") val expiresInSeconds: Long,
    @SerializedName("resendAfterSeconds") val resendAfterSeconds: Long,
    @SerializedName("message") val message: String? = null,
    @SerializedName("debugOtp") val debugOtp: String? = null
)

data class VerifyOtpRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("otp") val otp: String
)

data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("tokenType") val tokenType: String = "Bearer",
    @SerializedName("expiresInSeconds") val expiresInSeconds: Long = 3600
)

data class RefreshRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class LogoutRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class UserProfile(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("profileImageUrl") val profileImageUrl: String?,
    @SerializedName("preferredAddress") val preferredAddress: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("status") val status: String?
)

data class ApiErrorResponse(
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("status") val status: Int? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("fieldErrors") val fieldErrors: Map<String, String>? = null
)
