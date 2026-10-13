package com.example.waggoapp.data.network.dto

import com.google.gson.annotations.SerializedName

enum class PaymentStatus {
    CREATED,
    PENDING,
    CAPTURED,
    PARTIALLY_REFUNDED,
    FAILED,
    REFUNDED
}

data class PaymentOrderResponse(
    @SerializedName("paymentId")
    val paymentId: String,

    @SerializedName("razorpayOrderId")
    val razorpayOrderId: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("razorpayKeyId")
    val razorpayKeyId: String
)

data class CapturePaymentRequest(
    @SerializedName("paymentId")
    val paymentId: String, // razorpay_payment_id returned by Razorpay SDK

    @SerializedName("signature")
    val signature: String // razorpay_signature returned by Razorpay SDK
)

data class PaymentResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("bookingId")
    val bookingId: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("status")
    val status: PaymentStatus,

    @SerializedName("razorpayOrderId")
    val razorpayOrderId: String?,

    @SerializedName("razorpayPaymentId")
    val razorpayPaymentId: String?,

    @SerializedName("failureReason")
    val failureReason: String?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("paidAt")
    val paidAt: String?
)
