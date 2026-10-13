package com.example.waggoapp.data.repository

import com.example.waggoapp.data.network.ApiClient
import com.example.waggoapp.data.network.ApiErrorHandler
import com.example.waggoapp.data.network.WashGoApiService
import com.example.waggoapp.data.network.dto.CreateReviewRequest
import com.example.waggoapp.data.network.dto.ReviewResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReviewRepository(
    private val apiService: WashGoApiService = ApiClient.apiService
) {

    suspend fun submitReview(
        bookingId: String,
        rating: Int,
        comment: String? = null
    ): Result<ReviewResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createBookingReview(
                bookingId = bookingId,
                request = CreateReviewRequest(
                    rating = rating.coerceIn(1, 5),
                    comment = comment?.trim()
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

    suspend fun getBookingReview(bookingId: String): Result<ReviewResponse?> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBookingReview(bookingId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else if (response.code() == 404) {
                Result.success(null)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }
}
