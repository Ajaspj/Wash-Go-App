package com.washgo.agent.data.model

import com.google.gson.annotations.SerializedName

data class AgentJobNotificationMessage(
    @SerializedName("bookingId") val bookingId: String,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("customerName") val customerName: String? = "Customer",
    @SerializedName("customerLatitude") val customerLatitude: Double = 12.9716,
    @SerializedName("customerLongitude") val customerLongitude: Double = 77.5946,
    @SerializedName("customerAddress") val customerAddress: String? = "Indiranagar, Bengaluru",
    @SerializedName("vehicleType") val vehicleType: String? = "SUV",
    @SerializedName("earningsEstimate") val earningsEstimate: Double = 350.0,
    @SerializedName("timeoutSeconds") val timeoutSeconds: Int = 15,
    @SerializedName("requestedAt") val requestedAt: String? = null
)
