package com.example.waggoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waggoapp.data.network.dto.*
import com.example.waggoapp.data.repository.AddressRepository
import com.example.waggoapp.data.repository.BookingRepository
import com.example.waggoapp.data.repository.VehicleRepository
import com.example.waggoapp.data.repository.WashServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeDashboardState(
    val isLoading: Boolean = false,
    val vehicles: List<VehicleResponse> = emptyList(),
    val selectedVehicleId: String? = null,
    val services: List<WashServiceResponse> = emptyList(),
    val selectedServiceId: String? = null,
    val selectedCategory: ServiceCategory? = null,
    val defaultAddress: AddressResponse? = null,
    val currentBookings: List<BookingResponse> = emptyList(),
    val isServiceable: Boolean? = null,
    val serviceAreaName: String? = null,
    val errorMessage: String? = null,
    val showAddVehicleDialog: Boolean = false
)

class HomeViewModel(
    private val vehicleRepository: VehicleRepository = VehicleRepository(),
    private val washServiceRepository: WashServiceRepository = WashServiceRepository(),
    private val addressRepository: AddressRepository = AddressRepository(),
    private val bookingRepository: BookingRepository = BookingRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeDashboardState())
    val state: StateFlow<HomeDashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    fun refresh() {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            // Load vehicles
            val vehiclesRes = vehicleRepository.getVehicles()
            val vehicleList = vehiclesRes.getOrDefault(emptyList())

            val defaultVeh = vehicleList.firstOrNull { it.isDefault } ?: vehicleList.firstOrNull()
            val selectedVehId = _state.value.selectedVehicleId ?: defaultVeh?.id

            // Load services matching selected vehicle type
            val servicesRes = washServiceRepository.getServices(
                vehicleType = defaultVeh?.vehicleType,
                category = _state.value.selectedCategory
            )
            val serviceList = servicesRes.getOrDefault(emptyList())
            val defaultServiceId = _state.value.selectedServiceId ?: serviceList.firstOrNull()?.id

            // Load addresses
            val addressesRes = addressRepository.getAddresses()
            val addressList = addressesRes.getOrDefault(emptyList())
            val defAddress = addressList.firstOrNull { it.defaultAddress } ?: addressList.firstOrNull()

            // Check serviceability if address coordinates exist
            var isServiceable: Boolean? = null
            var areaName: String? = null
            if (defAddress?.latitude != null && defAddress.longitude != null) {
                val serviceCheck = addressRepository.checkServiceability(defAddress.latitude, defAddress.longitude)
                serviceCheck.getOrNull()?.let {
                    isServiceable = it.serviceable
                    areaName = it.serviceAreaName
                }
            }

            // Load current active bookings
            val bookingsRes = bookingRepository.getCurrentBookings()
            val activeBookings = bookingsRes.getOrDefault(emptyList())

            _state.update {
                it.copy(
                    isLoading = false,
                    vehicles = vehicleList,
                    selectedVehicleId = selectedVehId,
                    services = serviceList,
                    selectedServiceId = defaultServiceId,
                    defaultAddress = defAddress,
                    currentBookings = activeBookings,
                    isServiceable = isServiceable,
                    serviceAreaName = areaName
                )
            }
        }
    }

    fun selectVehicle(vehicleId: String) {
        _state.update { it.copy(selectedVehicleId = vehicleId) }
        val selectedVeh = _state.value.vehicles.find { it.id == vehicleId }
        // Reload services applicable to selected vehicle type
        viewModelScope.launch {
            val servicesRes = washServiceRepository.getServices(
                vehicleType = selectedVeh?.vehicleType,
                category = _state.value.selectedCategory
            )
            val serviceList = servicesRes.getOrDefault(emptyList())
            _state.update {
                it.copy(
                    services = serviceList,
                    selectedServiceId = serviceList.firstOrNull()?.id
                )
            }
        }
    }

    fun selectCategory(category: ServiceCategory?) {
        _state.update { it.copy(selectedCategory = category) }
        val selectedVeh = _state.value.vehicles.find { it.id == _state.value.selectedVehicleId }
        viewModelScope.launch {
            val servicesRes = washServiceRepository.getServices(
                vehicleType = selectedVeh?.vehicleType,
                category = category
            )
            val serviceList = servicesRes.getOrDefault(emptyList())
            _state.update {
                it.copy(
                    services = serviceList,
                    selectedServiceId = serviceList.firstOrNull()?.id
                )
            }
        }
    }

    fun selectService(serviceId: String) {
        _state.update { it.copy(selectedServiceId = serviceId) }
    }

    fun showAddVehicleDialog(show: Boolean) {
        _state.update { it.copy(showAddVehicleDialog = show) }
    }

    fun addNewVehicle(
        type: VehicleType,
        make: String,
        model: String,
        year: Int,
        regNumber: String,
        color: String,
        nickname: String?
    ) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = vehicleRepository.createVehicle(
                type = type,
                make = make,
                model = model,
                year = year,
                registrationNumber = regNumber,
                color = color,
                nickname = nickname,
                isDefault = _state.value.vehicles.isEmpty()
            )
            result.fold(
                onSuccess = { newVeh ->
                    _state.update { it.copy(showAddVehicleDialog = false) }
                    loadDashboardData()
                },
                onFailure = { err ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message ?: "Failed to add vehicle"
                        )
                    }
                }
            )
        }
    }
}
