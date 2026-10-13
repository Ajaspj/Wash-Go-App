package com.example.waggoapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

data class RefreshRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class LogoutRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword")
    val currentPassword: String,

    @SerializedName("newPassword")
    val newPassword: String
)

data class AuthResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("tokenType")
    val tokenType: String,

    @SerializedName("expiresInSeconds")
    val expiresInSeconds: Long
)

data class ProfileResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String?,

    @SerializedName("preferredAddress")
    val preferredAddress: String?,

    @SerializedName("role")
    val role: String,

    @SerializedName("status")
    val status: String
)

data class UpdateProfileRequest(
    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String? = null,

    @SerializedName("preferredAddress")
    val preferredAddress: String? = null
)
