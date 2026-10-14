package com.washgo.user.data.network

data class CreateDispatchRequest(
    val bookingId: String? = null,
    val agentId: String? = null,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val vehicleCategory: String,
    val price: Double,
    val packageName: String = "Deluxe Foam Wash",
    val addOns: List<String> = emptyList()
)

data class DispatchOfferResponse(
    val offerId: String,
    val bookingId: String,
    val serviceName: String,
    val durationSeconds: Int = 20
)

data class BookingMatchPush(
    val bookingId: String,
    val status: String,
    val agentId: String,
    val agentName: String,
    val agentPhone: String,
    val agentRating: Double,
    val agentLatitude: Double,
    val agentLongitude: Double,
    val etaMinutes: Int,
    val startOtp: String = "4821",
    val packageTitle: String = "Deluxe Foam Wash",
    val totalPrice: Double = 299.0,
    val vehicleCategory: String = "Car"
)
