package com.washgo.agent.data.repository

import android.content.Context
import com.washgo.agent.data.local.AgentTokenManager
import com.washgo.agent.data.model.AcceptBookingRequest
import com.washgo.agent.data.model.AgentAuthResponse
import com.washgo.agent.data.model.AgentProfileDto
import com.washgo.agent.data.model.AgentStatsDto
import com.washgo.agent.data.model.BookingMatchResult
import com.washgo.agent.data.model.LocationUpdateRequest
import com.washgo.agent.data.model.OnlineToggleRequest
import com.washgo.agent.data.model.RefreshTokenRequest
import com.washgo.agent.data.model.RegisterAgentOtpRequest
import com.washgo.agent.data.model.RejectBookingRequest
import com.washgo.agent.data.model.SendOtpRequest
import com.washgo.agent.data.model.SendOtpResponse
import com.washgo.agent.data.model.VerifyOtpRequest
import com.washgo.agent.data.network.AgentApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AgentRepository private constructor(context: Context) {

    private val apiClient = AgentApiClient.getInstance(context)
    private val tokenManager = AgentTokenManager.getInstance(context)

    suspend fun sendOtp(phone: String): Result<SendOtpResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.authApi.sendOtp(SendOtpRequest(phone))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to send OTP"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(phone: String, otp: String): Result<AgentAuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.authApi.verifyAgentOtp(VerifyOtpRequest(phone, otp))
            if (response.isSuccessful && response.body()?.data != null) {
                val authData = response.body()!!.data!!
                if (authData.isRegistered && authData.accessToken != null && authData.refreshToken != null) {
                    tokenManager.saveAgentSession(
                        accessToken = authData.accessToken,
                        refreshToken = authData.refreshToken,
                        agentId = authData.agentProfile?.id,
                        agentCode = authData.agentProfile?.agentCode,
                        phone = authData.phone,
                        displayName = authData.agentProfile?.displayName,
                        status = authData.agentProfile?.status ?: "PENDING_VERIFICATION"
                    )
                }
                Result.success(authData)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Verification failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerAgent(request: RegisterAgentOtpRequest): Result<AgentAuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.authApi.registerAgent(request)
            if (response.isSuccessful && response.body()?.data != null) {
                val authData = response.body()!!.data!!
                if (authData.accessToken != null && authData.refreshToken != null) {
                    tokenManager.saveAgentSession(
                        accessToken = authData.accessToken,
                        refreshToken = authData.refreshToken,
                        agentId = authData.agentProfile?.id,
                        agentCode = authData.agentProfile?.agentCode,
                        phone = authData.phone,
                        displayName = authData.agentProfile?.displayName,
                        status = authData.agentProfile?.status ?: "PENDING_VERIFICATION"
                    )
                }
                Result.success(authData)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Registration failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<AgentProfileDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.agentApi.getSelf()
            if (response.isSuccessful && response.body()?.data != null) {
                val profile = response.body()!!.data!!
                tokenManager.updateProfile(
                    displayName = profile.displayName,
                    agentCode = profile.agentCode,
                    status = profile.status
                )
                Result.success(profile)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to load agent profile"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitForReview(): Result<AgentProfileDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.agentApi.submitForReview()
            if (response.isSuccessful && response.body()?.data != null) {
                val profile = response.body()!!.data!!
                tokenManager.updateAgentStatus(profile.status)
                Result.success(profile)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to submit for review"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStats(): Result<AgentStatsDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.agentApi.getStats()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to load statistics"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleOnline(isOnline: Boolean): Result<AgentProfileDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.agentApi.toggleOnline(OnlineToggleRequest(isOnline))
            if (response.isSuccessful && response.body()?.data != null) {
                val profile = response.body()!!.data!!
                tokenManager.updateAgentStatus(profile.status)
                Result.success(profile)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to change online status"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLocation(lat: Double, lng: Double, heading: Double? = null): Result<AgentProfileDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.agentApi.updateLocation(LocationUpdateRequest(lat, lng, heading))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to update location"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptBooking(bookingId: String): Result<BookingMatchResult> = withContext(Dispatchers.IO) {
        try {
            val agentId = tokenManager.getAgentId()
            val response = apiClient.agentApi.acceptBooking(bookingId, AcceptBookingRequest(bookingId, agentId))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to accept booking"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectBooking(bookingId: String, reason: String = "DECLINED_BY_AGENT"): Result<String> = withContext(Dispatchers.IO) {
        try {
            val agentId = tokenManager.getAgentId()
            val response = apiClient.agentApi.rejectBooking(bookingId, RejectBookingRequest(bookingId, agentId, reason))
            if (response.isSuccessful) {
                Result.success(response.body()?.data ?: "Rejected")
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to reject booking"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken != null) {
                apiClient.agentApi.logout(RefreshTokenRequest(refreshToken))
            }
        } catch (_: Exception) {
            // Ignore network errors on logout
        } finally {
            tokenManager.clear()
        }
        Result.success(Unit)
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val json = JSONObject(errorBody)
            when {
                json.has("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        @Volatile
        private var instance: AgentRepository? = null

        fun getInstance(context: Context): AgentRepository {
            return instance ?: synchronized(this) {
                instance ?: AgentRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
