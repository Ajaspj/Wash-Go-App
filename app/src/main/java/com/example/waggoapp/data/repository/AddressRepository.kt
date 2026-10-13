package com.example.waggoapp.data.repository

import com.example.waggoapp.data.network.ApiClient
import com.example.waggoapp.data.network.ApiErrorHandler
import com.example.waggoapp.data.network.WashGoApiService
import com.example.waggoapp.data.network.dto.AddressRequest
import com.example.waggoapp.data.network.dto.AddressResponse
import com.example.waggoapp.data.network.dto.ServiceAreaCheckResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddressRepository(
    private val apiService: WashGoApiService = ApiClient.apiService
) {

    suspend fun getAddresses(): Result<List<AddressResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAddresses()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun createAddress(request: AddressRequest): Result<AddressResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createAddress(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun deleteAddress(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteAddress(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun setDefaultAddress(id: String): Result<AddressResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.setDefaultAddress(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun checkServiceability(
        latitude: Double,
        longitude: Double
    ): Result<ServiceAreaCheckResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkServiceability(latitude, longitude)
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
