package com.washgo.agent.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.washgo.agent.data.local.AgentTokenManager
import com.washgo.agent.data.model.AcceptBookingRequest
import com.washgo.agent.data.model.AgentAuthResponse
import com.washgo.agent.data.model.AgentDocumentDto
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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

    suspend fun uploadDocument(
        documentType: String,
        documentNumber: String?,
        fileName: String,
        contentType: String,
        fileBytes: ByteArray
    ): Result<AgentDocumentDto> = withContext(Dispatchers.IO) {
        try {
            if (fileBytes.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Uploaded document cannot be empty"))
            }
            if (fileBytes.size > 10 * 1024 * 1024) {
                return@withContext Result.failure(IllegalArgumentException("Document exceeds the maximum allowable size of 10 MiB"))
            }
            val allowedMimeTypes = setOf("image/jpeg", "image/png", "image/webp", "application/pdf")
            val effectiveMime = if (allowedMimeTypes.contains(contentType.lowercase())) {
                contentType
            } else {
                when {
                    fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                    fileName.endsWith(".png", ignoreCase = true) -> "image/png"
                    fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
                    fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                    else -> "application/pdf"
                }
            }

            val docTypeBody = documentType.toRequestBody("text/plain".toMediaTypeOrNull())
            val docNumberBody = documentNumber?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
            val fileRequestBody = fileBytes.toRequestBody(effectiveMime.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", fileName, fileRequestBody)

            val response = apiClient.agentApi.uploadDocument(docTypeBody, docNumberBody, filePart)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to upload KYC document"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadDocumentFromUri(
        context: Context,
        documentType: String,
        documentNumber: String?,
        uri: Uri
    ): Result<AgentDocumentDto> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            var fileName = "kyc_document.jpg"
            var fileSize: Long = 0

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            if (fileSize > 10 * 1024 * 1024) {
                return@withContext Result.failure(IllegalArgumentException("Document exceeds the maximum allowable size of 10 MiB"))
            }

            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(IllegalStateException("Unable to read selected file"))

            uploadDocument(documentType, documentNumber, fileName, mimeType, bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listDocuments(): Result<List<AgentDocumentDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.agentApi.listDocuments()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to retrieve KYC documents"
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
