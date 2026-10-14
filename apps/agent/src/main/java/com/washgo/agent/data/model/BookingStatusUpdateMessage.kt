package com.washgo.agent.data.model

import com.google.gson.annotations.SerializedName

data class BookingStatusUpdateMessage(
    @SerializedName("bookingId") val bookingId: String,
    @SerializedName("agentId") val agentId: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("agentLatitude") val agentLatitude: Double? = null,
    @SerializedName("agentLongitude") val agentLongitude: Double? = null,
    @SerializedName("etaMinutes") val etaMinutes: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)
