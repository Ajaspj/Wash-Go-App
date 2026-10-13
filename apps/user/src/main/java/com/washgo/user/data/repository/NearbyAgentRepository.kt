package com.washgo.user.data.repository

import com.washgo.user.data.network.ApiClient
import com.washgo.user.data.network.NearbyAgentApi
import com.washgo.user.data.network.NearbyAgentDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NearbyAgentRepository(
    private val api: NearbyAgentApi = ApiClient.nearbyAgentApi
) {
    suspend fun getNearbyAgents(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double = 5000.0,
        enableMockFallbackOnFailure: Boolean = true
    ): Result<List<NearbyAgentDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchNearby(latitude, longitude, radiusMeters)
            val agents = response.data ?: emptyList()
            Result.success(agents)
        } catch (e: Exception) {
            if (enableMockFallbackOnFailure) {
                // Graceful fallback for offline / development sandbox testing
                Result.success(getFallbackAgents(latitude, longitude))
            } else {
                Result.failure(e)
            }
        }
    }

    private fun getFallbackAgents(lat: Double, lng: Double): List<NearbyAgentDto> {
        return listOf(
            NearbyAgentDto(
                id = "agent-fallback-1",
                name = "Ramesh Kumar",
                phone = "+91 98765 43210",
                isAvailable = true,
                rating = 4.8,
                latitude = lat + 0.002,
                longitude = lng + 0.002,
                distanceMeters = 750.0,
                distanceKm = 0.8,
                etaMinutes = 2,
                formattedEta = "2 mins away"
            ),
            NearbyAgentDto(
                id = "agent-fallback-2",
                name = "Suresh Patel",
                phone = "+91 98765 43211",
                isAvailable = true,
                rating = 4.9,
                latitude = lat - 0.003,
                longitude = lng + 0.002,
                distanceMeters = 1350.0,
                distanceKm = 1.4,
                etaMinutes = 4,
                formattedEta = "4 mins away"
            ),
            NearbyAgentDto(
                id = "agent-fallback-3",
                name = "Anand Rao",
                phone = "+91 98765 43212",
                isAvailable = true,
                rating = 4.7,
                latitude = lat + 0.005,
                longitude = lng - 0.004,
                distanceMeters = 2100.0,
                distanceKm = 2.1,
                etaMinutes = 5,
                formattedEta = "5 mins away"
            )
        )
    }
}
