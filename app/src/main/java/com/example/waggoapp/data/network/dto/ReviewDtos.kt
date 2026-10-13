package com.example.waggoapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class CreateReviewRequest(
    @SerializedName("rating")
    val rating: Int, // 1 to 5

    @SerializedName("comment")
    val comment: String? = null
)

data class ReviewResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("bookingId")
    val bookingId: String,

    @SerializedName("serviceId")
    val serviceId: String,

    @SerializedName("customerId")
    val customerId: String,

    @SerializedName("rating")
    val rating: Int,

    @SerializedName("comment")
    val comment: String?,

    @SerializedName("visible")
    val visible: Boolean,

    @SerializedName("createdAt")
    val createdAt: String?
)
