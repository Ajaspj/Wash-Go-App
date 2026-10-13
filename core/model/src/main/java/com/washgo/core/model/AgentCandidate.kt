package com.washgo.core.model

data class AgentCandidate(
    val agentId: String,
    val displayName: String,
    val phone: String,
    val currentLatitude: Double,
    val currentLongitude: Double,
    val averageRating: Double,
    val totalCompletedWashes: Int,
    val vehicleSpecialty: VehicleSpecialty,
    val distanceMeters: Double,
    val estimatedEtaMinutes: Int
) {
    val formattedDistance: String
        get() = if (distanceMeters >= 1000) {
            "%.1f km".format(distanceMeters / 1000.0)
        } else {
            "${distanceMeters.toInt()} m"
        }

    val formattedEta: String
        get() = "$estimatedEtaMinutes mins"
}
