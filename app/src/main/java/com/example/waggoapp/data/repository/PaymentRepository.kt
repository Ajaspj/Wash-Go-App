package com.example.waggoapp.data.repository

import com.example.waggoapp.data.network.ApiClient
import com.example.waggoapp.data.network.ApiErrorHandler
import com.example.waggoapp.data.network.WashGoApiService
import com.example.waggoapp.data.network.dto.CapturePaymentRequest
import com.example.waggoapp.data.network.dto.PaymentOrderResponse
import com.example.waggoapp.data.network.dto.PaymentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository(
    private val apiService: WashGoApiService = ApiClient.apiService
) {

    suspend fun createPaymentOrder(bookingId: String): Result<PaymentOrderResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createPaymentOrder(bookingId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(ApiErrorHandler.parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorHandler.parseException(e)))
        }
    }

    suspend fun capturePayment(
        paymentId: String,
        razorpayPaymentId: String,
        razorpaySignature: String
    ): Result<PaymentResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.capturePayment(
                paymentId = paymentId,
                request = CapturePaymentRequest(
                    paymentId = razorpayPaymentId,
                    signature = razorpaySignature
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

    suspend fun listBookingPayments(bookingId: String): Result<List<PaymentResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.listBookingPayments(bookingId)
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
