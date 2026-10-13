package com.example.waggoapp.data.repository

import com.example.waggoapp.data.network.ApiClient
import com.example.waggoapp.data.network.ApiErrorHandler
import com.example.waggoapp.data.network.WashGoApiService
import com.example.waggoapp.data.network.dto.DeviceTokenRequest
import com.example.waggoapp.data.network.dto.DeviceTokenResponse
import com.example.waggoapp.data.network.dto.NotificationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepository(
    private val apiService: WashGoApiService = ApiClient.apiService
) {

    suspend fun getNotifications(): Result<List<NotificationResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCustomerNotifications(page = 0, size = 50)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data.content)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun markAsRead(id: String): Result<NotificationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markNotificationRead(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun markAllAsRead(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markAllNotificationsRead()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun registerDeviceToken(token: String): Result<DeviceTokenResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.registerDeviceToken(DeviceTokenRequest(token, "ANDROID"))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }
}
