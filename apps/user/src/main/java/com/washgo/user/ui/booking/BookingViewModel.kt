package com.washgo.user.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.washgo.core.model.VehicleCategory
import com.washgo.user.data.network.ApiClient
import com.washgo.user.data.network.BookingMatchPush
import com.washgo.user.data.network.CreateDispatchRequest
import com.washgo.user.data.network.NearbyAgentDto
import com.washgo.user.data.network.UserWebSocketClient
import com.washgo.user.data.repository.NearbyAgentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class BookingViewModel(
    private val repository: NearbyAgentRepository = NearbyAgentRepository()
) : ViewModel() {

    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Idle)
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow(VehicleCategory.SEDAN)
    val selectedCategory: StateFlow<VehicleCategory> = _selectedCategory.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            UserWebSocketClient.matchEvents.collect { match ->
                _uiState.value = BookingUiState.Matched(match)
                _userMessage.emit("Match confirmed! ${match.agentName} accepted. Share OTP ${match.startOtp}")
            }
        }
    }

    fun updatePinLocation(latitude: Double, longitude: Double, address: String? = null) {
        val resolvedAddress = address ?: resolveAddressForCoordinates(latitude, longitude)
        _locationState.value = _locationState.value.copy(
            latitude = latitude,
            longitude = longitude,
            address = resolvedAddress
        )
    }

    fun setPinDragging(isDragging: Boolean) {
        _locationState.value = _locationState.value.copy(isDragging = isDragging)
    }

    fun snapToGps(gpsLatitude: Double = 12.9716, gpsLongitude: Double = 77.5946) {
        _locationState.value = _locationState.value.copy(
            latitude = gpsLatitude,
            longitude = gpsLongitude,
            address = "12th Main Road, Indiranagar, Bengaluru",
            isLocked = false
        )
        _uiState.value = BookingUiState.Idle
        viewModelScope.launch {
            _userMessage.emit("Snapped to current GPS location")
        }
    }

    fun lockLocationAndSearch(radiusMeters: Double = 5000.0) {
        val currentLoc = _locationState.value
        _locationState.value = currentLoc.copy(isLocked = true)
        _uiState.value = BookingUiState.Loading

        viewModelScope.launch {
            val result = repository.getNearbyAgents(
                latitude = currentLoc.latitude,
                longitude = currentLoc.longitude,
                radiusMeters = radiusMeters
            )

            result.fold(
                onSuccess = { agents ->
                    if (agents.isEmpty()) {
                        _uiState.value = BookingUiState.Empty
                    } else {
                        _uiState.value = BookingUiState.Success(
                            agents = agents,
                            selectedAgentId = agents.firstOrNull()?.id
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = BookingUiState.Error(
                        error.localizedMessage ?: "Failed to find nearby washers. Please retry."
                    )
                }
            )
        }
    }

    fun unlockLocation() {
        _locationState.value = _locationState.value.copy(isLocked = false)
        _uiState.value = BookingUiState.Idle
    }

    fun selectCategory(category: VehicleCategory) {
        _selectedCategory.value = category
    }

    fun selectAgent(agentId: String) {
        val current = _uiState.value
        if (current is BookingUiState.Success) {
            _uiState.value = current.copy(selectedAgentId = agentId)
        }
    }

    fun confirmWashBooking(agent: NearbyAgentDto) {
        val current = _uiState.value
        if (current is BookingUiState.Success) {
            _uiState.value = current.copy(isBookingInProgress = true)
            viewModelScope.launch {
                val bookingId = UUID.randomUUID().toString()
                UserWebSocketClient.connect(bookingId)

                val price = if (_selectedCategory.value == VehicleCategory.BIKE) 149.0 else 299.0
                val request = CreateDispatchRequest(
                    bookingId = bookingId,
                    agentId = agent.id,
                    latitude = _locationState.value.latitude,
                    longitude = _locationState.value.longitude,
                    address = _locationState.value.address,
                    vehicleCategory = _selectedCategory.value.name,
                    price = price
                )

                try {
                    ApiClient.nearbyAgentApi.dispatchBooking(request)
                    _userMessage.emit("Job request broadcasted to ${agent.name}. Waiting for acceptance...")
                } catch (_: Exception) {
                    // Sandbox fallback: auto-simulate agent acceptance after 1.5 seconds if backend is offline
                    delay(1500)
                    UserWebSocketClient.simulateMockMatch(
                        BookingMatchPush(
                            bookingId = bookingId,
                            status = "ACCEPTED",
                            agentId = agent.id,
                            agentName = agent.name,
                            agentPhone = agent.phone,
                            agentRating = agent.rating,
                            agentLatitude = agent.latitude,
                            agentLongitude = agent.longitude,
                            etaMinutes = agent.etaMinutes,
                            startOtp = "4821"
                        )
                    )
                }
            }
        }
    }

    private fun resolveAddressForCoordinates(lat: Double, lng: Double): String {
        val latDiff = ((lat - 12.9716) * 1000).toInt()
        val lngDiff = ((lng - 77.5946) * 1000).toInt()
        return if (latDiff == 0 && lngDiff == 0) {
            "12th Main Road, Indiranagar, Bengaluru"
        } else {
            "Near Sector ${Math.abs(latDiff) % 15 + 1}, Indiranagar (${String.format("%.4f", lat)}, ${String.format("%.4f", lng)})"
        }
    }
}
