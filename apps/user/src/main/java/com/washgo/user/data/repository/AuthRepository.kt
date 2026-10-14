package com.washgo.user.data.repository

import com.google.gson.Gson
import com.washgo.user.data.local.TokenManager
import com.washgo.user.data.network.ApiErrorResponse
import com.washgo.user.data.network.ApiClient
import com.washgo.user.data.network.AuthApi
import com.washgo.user.data.network.AuthResponse
import com.washgo.user.data.network.LogoutRequest
import com.washgo.user.data.network.SendOtpRequest
import com.washgo.user.data.network.SendOtpResponse
import com.washgo.user.data.network.UserProfile
import com.washgo.user.data.network.VerifyOtpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException

interface AuthRepository {
    val isLoggedIn: StateFlow<Boolean>
    fun getStoredPhone(): String?
    fun getStoredUserName(): String?
    fun getStoredEmail(): String?
    suspend fun sendOtp(phone: String): Result<SendOtpResponse>
    suspend fun verifyOtp(phone: String, otp: String): Result<AuthResponse>
    suspend fun fetchProfile(): Result<UserProfile>
    suspend fun logout(): Result<Unit>
}

open class RealAuthRepository(
    private val authApi: AuthApi = ApiClient.authApi,
    private val tokenManager: TokenManager
) : AuthRepository {
    private val gson = Gson()

    override val isLoggedIn: StateFlow<Boolean> = tokenManager.isLoggedIn

    override fun getStoredPhone(): String? = tokenManager.getUserPhone()

    override fun getStoredUserName(): String? = tokenManager.getUserFullName()

    override fun getStoredEmail(): String? = tokenManager.getUserEmail()

    override suspend fun sendOtp(phone: String): Result<SendOtpResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.sendOtp(SendOtpRequest(phone = phone))
            val data = response.data ?: throw IllegalStateException("Empty response from OTP server")
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(Exception(parseErrorMessage(e), e))
        }
    }

    override suspend fun verifyOtp(phone: String, otp: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.verifyOtp(VerifyOtpRequest(phone = phone, otp = otp))
            val auth = response.data ?: throw IllegalStateException("Empty auth tokens returned")
            
            // Persist tokens securely in Keystore-backed storage
            tokenManager.saveUserSession(
                accessToken = auth.accessToken,
                refreshToken = auth.refreshToken,
                phone = phone
            )

            // Try fetching profile immediately to enrich session
            try {
                val profileRes = authApi.getProfile()
                profileRes.data?.let { profile ->
                    tokenManager.saveUserSession(
                        accessToken = auth.accessToken,
                        refreshToken = auth.refreshToken,
                        phone = profile.phone ?: phone,
                        email = profile.email,
                        fullName = profile.name,
                        userId = profile.id
                    )
                }
            } catch (ignored: Exception) {
                // Profile fetch failure does not block login
            }

            Result.success(auth)
        } catch (e: Exception) {
            Result.failure(Exception(parseErrorMessage(e), e))
        }
    }

    override suspend fun fetchProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.getProfile()
            val profile = response.data ?: throw IllegalStateException("Empty profile returned")
            tokenManager.updateProfile(
                fullName = profile.name,
                email = profile.email,
                phone = profile.phone
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(Exception(parseErrorMessage(e), e))
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        val refreshToken = tokenManager.getRefreshToken()
        try {
            if (!refreshToken.isNullOrBlank()) {
                authApi.logout(LogoutRequest(refreshToken = refreshToken))
            }
        } catch (ignored: Exception) {
            // Even if server call fails or offline, local session must be revoked
        } finally {
            tokenManager.clear()
        }
        Result.success(Unit)
    }

    private fun parseErrorMessage(throwable: Throwable): String {
        if (throwable is HttpException) {
            try {
                val errorBody = throwable.response()?.errorBody()?.string()
                if (!errorBody.isNullOrBlank()) {
                    val errorObj = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                    if (!errorObj.message.isNullOrBlank()) {
                        return errorObj.message
                    }
                }
            } catch (ignored: Exception) {
            }
            return when (throwable.code()) {
                400 -> "Invalid request. Please check your input."
                401 -> "Invalid OTP. Please check the code and try again."
                429 -> "Too many attempts. Please wait before retrying."
                else -> "Server error (${throwable.code()}). Please try again."
            }
        }
        return throwable.localizedMessage ?: "Network connection failed. Please check your internet."
    }
}
