package com.washgo.agent.data.model

data class AgentApiResponse<T>(
    val data: T?,
    val message: String? = null,
    val code: String? = null,
    val status: Int? = null
)

data class SendOtpRequest(
    val phone: String
)

data class SendOtpResponse(
    val phone: String,
    val expiresInSeconds: Long,
    val resendAfterSeconds: Long,
    val message: String? = null,
    val debugOtp: String? = null
)

data class VerifyOtpRequest(
    val phone: String,
    val otp: String
)

data class AgentAuthResponse(
    val isRegistered: Boolean,
    val phone: String,
    val registrationToken: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val tokenType: String? = null,
    val expiresInSeconds: Long = 0,
    val agentProfile: AgentProfileDto? = null
)

data class RegisterAgentOtpRequest(
    val phone: String,
    val registrationToken: String = "",
    val firstName: String,
    val lastName: String,
    val email: String,
    val vehicleSpecialty: String = "BOTH",
    val waterCapacityLiters: Int = 200,
    val hasPressureWasher: Boolean = true,
    val hasVacuumExtractor: Boolean = true
)

data class AgentProfileDto(
    val id: String,
    val userId: String,
    val agentCode: String,
    val displayName: String,
    val email: String? = null,
    val phone: String,
    val status: String,
    val rejectionReason: String? = null,
    val available: Boolean = false,
    val isOnline: Boolean = false,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val headingDegrees: Double? = null,
    val waterCapacityLiters: Int? = 0,
    val hasPressureWasher: Boolean? = true,
    val hasVacuumExtractor: Boolean? = true,
    val averageRating: Double? = 5.0,
    val totalReviewsCount: Int? = 0,
    val totalCompletedWashes: Int? = 0
)

data class AgentStatsDto(
    val todayEarnings: Double = 0.0,
    val todayCompletedWashes: Int = 0,
    val totalCompletedWashes: Int = 0,
    val acceptanceRatePercentage: Int = 100,
    val averageRating: Double = 5.0,
    val totalReviewsCount: Int = 0
)

data class OnlineToggleRequest(
    val isOnline: Boolean
)

data class AvailabilityToggleRequest(
    val available: Boolean
)

data class LocationUpdateRequest(
    val latitude: Double,
    val longitude: Double,
    val headingDegrees: Double? = null
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresInSeconds: Long
)

data class AcceptBookingRequest(
    val bookingId: String? = null,
    val agentId: String? = null
)

data class RejectBookingRequest(
    val bookingId: String? = null,
    val agentId: String? = null,
    val reason: String? = null
)

data class BookingMatchResult(
    val bookingId: String,
    val status: String,
    val agentId: String,
    val agentName: String,
    val startOtp: String? = null
)
