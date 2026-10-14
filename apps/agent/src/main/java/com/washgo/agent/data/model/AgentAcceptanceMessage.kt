package com.washgo.agent.data.model

import com.google.gson.annotations.SerializedName

data class AgentAcceptanceMessage(
    @SerializedName("action") val action: String = "AGENT_ACCEPT",
    @SerializedName("bookingId") val bookingId: String,
    @SerializedName("agentId") val agentId: String,
    @SerializedName("agentLatitude") val agentLatitude: Double,
    @SerializedName("agentLongitude") val agentLongitude: Double,
    @SerializedName("acceptedAt") val acceptedAt: String
)
