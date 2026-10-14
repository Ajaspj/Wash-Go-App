package com.washgo.agent.data.model

import com.google.gson.annotations.SerializedName

data class AgentLocationUpdate(
    @SerializedName("action") val action: String = "UPDATE_LOCATION",
    @SerializedName("agentId") val agentId: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("speedKmph") val speedKmph: Double = 25.0,
    @SerializedName("heading") val heading: Float = 0f,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis()
)
