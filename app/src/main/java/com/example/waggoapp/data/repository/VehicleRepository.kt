package com.example.waggoapp.data.repository

import com.example.waggoapp.data.network.ApiClient
import com.example.waggoapp.data.network.ApiErrorHandler
import com.example.waggoapp.data.network.WashGoApiService
import com.example.waggoapp.data.network.dto.CreateVehicleRequest
import com.example.waggoapp.data.network.dto.VehicleResponse
import com.example.waggoapp.data.network.dto.VehicleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VehicleRepository(
    private val apiService: WashGoApiService = ApiClient.apiService
) {

    suspend fun getVehicles(): Result<List<VehicleResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getVehicles()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun createVehicle(
        type: VehicleType,
        make: String,
        model: String,
        year: Int,
        registrationNumber: String,
        color: String,
        nickname: String? = null,
        isDefault: Boolean = false
    ): Result<VehicleResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createVehicle(
                CreateVehicleRequest(
                    vehicleType = type,
                    make = make.trim(),
                    model = model.trim(),
                    year = year,
                    registrationNumber = registrationNumber.trim().uppercase(),
                    color = color.trim(),
                    nickname = nickname?.trim(),
                    isDefault = isDefault
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun deleteVehicle(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteVehicle(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun setDefaultVehicle(id: String): Result<VehicleResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.setDefaultVehicle(id)
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
