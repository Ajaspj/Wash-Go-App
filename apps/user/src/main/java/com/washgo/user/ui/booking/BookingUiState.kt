package com.washgo.user.ui.booking

import com.washgo.user.data.network.NearbyAgentDto
import com.washgo.user.data.network.BookingMatchPush

enum class AgentSortOption(val title: String) {
    RECOMMENDED("Recommended"),
    FASTEST_ETA("Fastest Arrival"),
    TOP_RATED("Top Rated")
}

data class WashPackageItem(
    val id: String,
    val name: String,
    val description: String,
    val durationMinutes: Int,
    val priceBike: Double,
    val priceCar: Double
)

data class WashAddOnItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double
)

sealed interface BookingUiState {
    data object Idle : BookingUiState
    data object Loading : BookingUiState
    data class Success(
        val agents: List<NearbyAgentDto>,
        val selectedAgentId: String? = null,
        val sortOption: AgentSortOption = AgentSortOption.RECOMMENDED,
        val isBookingInProgress: Boolean = false,
        val confirmedAgent: NearbyAgentDto? = null
    ) : BookingUiState
    data class Matched(
        val match: BookingMatchPush,
        val isCancelling: Boolean = false
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
