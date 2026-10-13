package com.washgo.user.ui.booking

import com.washgo.user.data.network.NearbyAgentDto

sealed interface BookingUiState {
    data object Idle : BookingUiState
    data object Loading : BookingUiState
    data class Success(
        val agents: List<NearbyAgentDto>,
        val selectedAgentId: String? = null,
        val isBookingInProgress: Boolean = false,
        val confirmedAgent: NearbyAgentDto? = null
    ) : BookingUiState
    data class Matched(
        val match: com.washgo.user.data.network.BookingMatchPush
    ) : BookingUiState
    data object Empty : BookingUiState
    data class Error(val message: String) : BookingUiState
}

data class LocationState(
    val latitude: Double = 12.9716,
    val longitude: Double = 77.5946,
    val address: String = "12th Main Road, Indiranagar, Bengaluru",
    val isLocked: Boolean = false,
    val isDragging: Boolean = false
)
