package com.washgo.user.data.network

data class ApiResponse<T>(
    val data: T?,
    val message: String? = null,
    val code: String? = null
)

data class NearbyAgentDto(
    val id: String,
    val name: String,
    val phone: String,
    val isAvailable: Boolean,
    val rating: Double,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Double,
    val distanceKm: Double,
    val etaMinutes: Int,
    val formattedEta: String
)
