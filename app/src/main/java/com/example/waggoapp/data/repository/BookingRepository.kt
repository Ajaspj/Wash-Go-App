package com.example.waggoapp.data.repository

import com.example.waggoapp.data.network.ApiClient
import com.example.waggoapp.data.network.ApiErrorHandler
import com.example.waggoapp.data.network.WashGoApiService
import com.example.waggoapp.data.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookingRepository(
    private val apiService: WashGoApiService = ApiClient.apiService
) {

    suspend fun createBooking(request: CreateBookingRequest): Result<BookingResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createBooking(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun getCurrentBookings(): Result<List<BookingResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentBookings()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun getBookingById(id: String): Result<BookingResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBookingById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun getBookingTracking(id: String): Result<TrackingResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTracking(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun cancelBooking(id: String, reason: String? = null): Result<BookingResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.cancelBooking(id, CancelBookingRequest(reason))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun validateCoupon(bookingId: String, code: String): Result<CouponValidationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.validateCoupon(ValidateCouponRequest(bookingId, code))
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
