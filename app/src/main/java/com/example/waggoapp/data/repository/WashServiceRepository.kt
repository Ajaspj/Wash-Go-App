package com.example.waggoapp.data.repository

import com.example.waggoapp.data.network.ApiClient
import com.example.waggoapp.data.network.ApiErrorHandler
import com.example.waggoapp.data.network.WashGoApiService
import com.example.waggoapp.data.network.dto.AddonResponse
import com.example.waggoapp.data.network.dto.ServiceCategory
import com.example.waggoapp.data.network.dto.VehicleType
import com.example.waggoapp.data.network.dto.WashServiceResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WashServiceRepository(
    private val apiService: WashGoApiService = ApiClient.apiService
) {

    suspend fun getServices(
        vehicleType: VehicleType? = null,
        category: ServiceCategory? = null
    ): Result<List<WashServiceResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getActiveServices(vehicleType, category)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun getAddons(): Result<List<AddonResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAddons(page = 0, size = 50)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data.content)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }
}
