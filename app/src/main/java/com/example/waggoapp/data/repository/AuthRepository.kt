package com.example.waggoapp.data.repository

import com.example.waggoapp.data.local.TokenManager
import com.example.waggoapp.data.network.ApiClient
import com.example.waggoapp.data.network.ApiErrorHandler
import com.example.waggoapp.data.network.WashGoApiService
import com.example.waggoapp.data.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: WashGoApiService = ApiClient.apiService,
    private val tokenManager: TokenManager = ApiClient.getTokenManager()
) {

    val isLoggedIn: StateFlow<Boolean> = tokenManager.isLoggedIn

    fun getSavedUserFullName(): String? = tokenManager.getUserFullName()
    fun getSavedUserEmail(): String? = tokenManager.getUserEmail()

    suspend fun login(email: String, password: String): Result<ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email.trim(), password))
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!.data
                tokenManager.saveTokens(authData.accessToken, authData.refreshToken)

                // Fetch user profile immediately after login
                val profileRes = apiService.getProfile()
                if (profileRes.isSuccessful && profileRes.body() != null) {
                    val profile = profileRes.body()!!.data
                    tokenManager.saveUserSession(
                        accessToken = authData.accessToken,
                        refreshToken = authData.refreshToken,
                        email = profile.email,
                        fullName = profile.name,
                        userId = profile.id
                    )
                    Result.success(profile)
                } else {
                    // Fallback profile if profile fetch has a momentary hiccup
                    val basicProfile = ProfileResponse(
                        id = "",
                        name = email.substringBefore("@"),
                        email = email,
                        phone = null,
                        profileImageUrl = null,
                        preferredAddress = null,
                        role = "CUSTOMER",
                        status = "ACTIVE"
                    )
                    Result.success(basicProfile)
                }
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(
                RegisterRequest(
                    email = email.trim(),
                    password = password,
                    firstName = firstName.trim(),
                    lastName = lastName.trim()
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!.data
                tokenManager.saveTokens(authData.accessToken, authData.refreshToken)

                val profileRes = apiService.getProfile()
                if (profileRes.isSuccessful && profileRes.body() != null) {
                    val profile = profileRes.body()!!.data
                    tokenManager.saveUserSession(
                        accessToken = authData.accessToken,
                        refreshToken = authData.refreshToken,
                        email = profile.email,
                        fullName = profile.name,
                        userId = profile.id
                    )
                    Result.success(profile)
                } else {
                    val basicProfile = ProfileResponse(
                        id = "",
                        name = "$firstName $lastName".trim(),
                        email = email,
                        phone = null,
                        profileImageUrl = null,
                        preferredAddress = null,
                        role = "CUSTOMER",
                        status = "ACTIVE"
                    )
                    Result.success(basicProfile)
                }
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val refreshToken = tokenManager.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                try {
                    apiService.logout(LogoutRequest(refreshToken))
                } catch (ignored: Exception) {
                    // Network errors on logout should not prevent clearing local credentials
                }
            }
            tokenManager.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.clear()
            Result.success(Unit)
        }
    }

    suspend fun getProfile(): Result<ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!.data
                tokenManager.updateProfile(profile.name, profile.email)
                Result.success(profile)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        phone: String?,
        preferredAddress: String?
    ): Result<ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateProfile(
                UpdateProfileRequest(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    phone = phone?.trim(),
                    preferredAddress = preferredAddress?.trim()
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!.data
                tokenManager.updateProfile(profile.name, profile.email)
                Result.success(profile)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }
}
