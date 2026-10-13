package com.example.waggoapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class ValidateCouponRequest(
    @SerializedName("bookingId")
    val bookingId: String,

    @SerializedName("code")
    val code: String
)

data class CouponValidationResponse(
    @SerializedName("couponId")
    val couponId: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("originalAmount")
    val originalAmount: Double,

    @SerializedName("discount")
    val discount: Double,

    @SerializedName("finalAmount")
    val finalAmount: Double
)
