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

    val packages: List<WashPackageItem> = listOf(
        WashPackageItem(
            id = "pkg-express",
            name = "Quick Express Wash",
            description = "Waterless pressure spray & ultra-soft microfiber body polish",
            durationMinutes = 20,
            priceBike = 149.0,
            priceCar = 299.0
        ),
        WashPackageItem(
            id = "pkg-deluxe",
            name = "Deluxe Snow Foam Wash",
            description = "Dense foam shampoo, deep rim degrease & hydrophobic shine",
            durationMinutes = 35,
            priceBike = 249.0,
            priceCar = 449.0
        ),
        WashPackageItem(
            id = "pkg-detailing",
            name = "Deep Clean & Detailing",
            description = "Full foam wash, interior vacuuming, dashboard dressing & tire gloss",
            durationMinutes = 55,
            priceBike = 399.0,
            priceCar = 699.0
        )
    )

    val addOns: List<WashAddOnItem> = listOf(
        WashAddOnItem(
            id = "addon-tire",
            name = "Tire & Alloy Shine",
            description = "Hydrophobic matte black dressing on wheels",
            price = 49.0
        ),
        WashAddOnItem(
            id = "addon-vac",
            name = "Interior Vacuuming",
            description = "High-suction carpet and upholstery cleaning",
            price = 99.0
        ),
        WashAddOnItem(
            id = "addon-rain",
            name = "Rain-X Glass Shield",
            description = "Water-beading anti-glare coating on mirrors & glass",
            price = 79.0
        )
    )

    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Idle)
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow(VehicleCategory.SEDAN)
    val selectedCategory: StateFlow<VehicleCategory> = _selectedCategory.asStateFlow()

    private val _selectedPackage = MutableStateFlow(packages[0])
    val selectedPackage: StateFlow<WashPackageItem> = _selectedPackage.asStateFlow()

    private val _selectedAddOnIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedAddOnIds: StateFlow<Set<String>> = _selectedAddOnIds.asStateFlow()

    private val _sortOption = MutableStateFlow(AgentSortOption.RECOMMENDED)
    val sortOption: StateFlow<AgentSortOption> = _sortOption.asStateFlow()

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
                onSuccess = { rawAgents ->
                    if (rawAgents.isEmpty()) {
                        _uiState.value = BookingUiState.Empty
                    } else {
                        val sorted = sortAgents(rawAgents, _sortOption.value)
                        _uiState.value = BookingUiState.Success(
                            agents = sorted,
                            selectedAgentId = sorted.firstOrNull()?.id,
                            sortOption = _sortOption.value
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

    fun selectPackage(pkg: WashPackageItem) {
        _selectedPackage.value = pkg
    }

    fun toggleAddOn(addOnId: String) {
        val current = _selectedAddOnIds.value.toMutableSet()
        if (current.contains(addOnId)) {
            current.remove(addOnId)
        } else {
            current.add(addOnId)
        }
        _selectedAddOnIds.value = current
    }

    fun setSortOption(option: AgentSortOption) {
        _sortOption.value = option
        val current = _uiState.value
        if (current is BookingUiState.Success) {
            val sorted = sortAgents(current.agents, option)
            _uiState.value = current.copy(
                agents = sorted,
                sortOption = option,
                selectedAgentId = sorted.firstOrNull()?.id
            )
        }
    }

    private fun sortAgents(agents: List<NearbyAgentDto>, option: AgentSortOption): List<NearbyAgentDto> {
        return when (option) {
            AgentSortOption.RECOMMENDED -> agents.sortedWith(
                compareBy<NearbyAgentDto> { it.etaMinutes }.thenByDescending { it.rating }
            )
            AgentSortOption.FASTEST_ETA -> agents.sortedBy { it.etaMinutes }
            AgentSortOption.TOP_RATED -> agents.sortedByDescending { it.rating }
        }
    }

    fun selectAgent(agentId: String) {
        val current = _uiState.value
        if (current is BookingUiState.Success) {
            _uiState.value = current.copy(selectedAgentId = agentId)
        }
    }

    fun calculateBasePrice(): Double {
        return if (_selectedCategory.value == VehicleCategory.BIKE) {
            _selectedPackage.value.priceBike
        } else {
            _selectedPackage.value.priceCar
        }
    }

    fun calculateAddOnsTotal(): Double {
        val selectedIds = _selectedAddOnIds.value
        return addOns.filter { selectedIds.contains(it.id) }.sumOf { it.price }
    }

    fun calculateTotalAmount(): Double {
        return calculateBasePrice() + calculateAddOnsTotal()
    }

    fun confirmWashBooking(agent: NearbyAgentDto) {
        val current = _uiState.value
        if (current is BookingUiState.Success) {
            _uiState.value = current.copy(isBookingInProgress = true)
            viewModelScope.launch {
                val bookingId = UUID.randomUUID().toString()
                UserWebSocketClient.connect(bookingId)

                val totalPrice = calculateTotalAmount()
                val selectedAddOnNames = addOns
                    .filter { _selectedAddOnIds.value.contains(it.id) }
                    .map { it.name }

                val request = CreateDispatchRequest(
                    bookingId = bookingId,
                    agentId = agent.id,
                    latitude = _locationState.value.latitude,
                    longitude = _locationState.value.longitude,
                    address = _locationState.value.address,
                    vehicleCategory = _selectedCategory.value.name,
                    price = totalPrice,
                    packageName = _selectedPackage.value.name,
                    addOns = selectedAddOnNames
                )

                try {
                    ApiClient.nearbyAgentApi.dispatchBooking(request)
                    _userMessage.emit("Job request broadcasted to ${agent.name}. Waiting for acceptance...")
                } catch (_: Exception) {
                    // Sandbox fallback: auto-simulate agent acceptance after 1.5s
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
                            startOtp = "4821",
                            packageTitle = _selectedPackage.value.name,
                            totalPrice = totalPrice,
                            vehicleCategory = if (_selectedCategory.value == VehicleCategory.BIKE) "Bike" else "Car"
                        )
                    )
                }
            }
        }
    }

    fun cancelActiveBooking() {
        val current = _uiState.value
        if (current is BookingUiState.Matched) {
            viewModelScope.launch {
                UserWebSocketClient.disconnect()
                _uiState.value = BookingUiState.Idle
                _locationState.value = _locationState.value.copy(isLocked = false)
                _userMessage.emit("Booking cancelled successfully.")
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
