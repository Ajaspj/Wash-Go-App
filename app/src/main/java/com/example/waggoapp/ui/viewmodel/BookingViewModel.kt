package com.example.waggoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waggoapp.data.network.dto.*
import com.example.waggoapp.data.repository.AddressRepository
import com.example.waggoapp.data.repository.BookingRepository
import com.example.waggoapp.data.repository.PaymentRepository
import com.example.waggoapp.data.repository.WashServiceRepository
import com.example.waggoapp.ui.screens.ParkingType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

sealed interface BookingFlowEvent {
    data class LaunchRazorpay(val paymentOrder: PaymentOrderResponse, val booking: BookingResponse) : BookingFlowEvent
    data class BookingAndPaymentSuccess(val booking: BookingResponse, val payment: PaymentResponse) : BookingFlowEvent
    data class BookingFailed(val errorMessage: String) : BookingFlowEvent
}

data class BookingCheckoutState(
    val selectedVehicle: VehicleResponse? = null,
    val selectedService: WashServiceResponse? = null,
    val availableAddons: List<AddonResponse> = emptyList(),
    val selectedAddonIds: Set<String> = emptySet(),
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "Austin",
    val state: String = "TX",
    val postalCode: String = "78704",
    val country: String = "USA",
    val latitude: Double = 30.2672,
    val longitude: Double = -97.7431,
    val selectedParkingType: ParkingType = ParkingType.GARAGE,
    val slotNumber: String = "",
    val gateCode: String = "",
    val couponCode: String = "",
    val isServiceable: Boolean? = null,
    val serviceAreaName: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createdBooking: BookingResponse? = null,
    val activePaymentOrder: PaymentOrderResponse? = null
) {
    // Client-side visual estimate (authoritative payable amount is computed by backend)
    val estimatedBasePrice: Double
        get() = selectedService?.basePrice ?: 0.0

    val estimatedAddonsTotal: Double
        get() = availableAddons
            .filter { selectedAddonIds.contains(it.id) }
            .sumOf { it.price }

    val estimatedTotal: Double
        get() = estimatedBasePrice + estimatedAddonsTotal
}

class BookingViewModel(
    private val bookingRepository: BookingRepository = BookingRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository(),
    private val washServiceRepository: WashServiceRepository = WashServiceRepository(),
    private val addressRepository: AddressRepository = AddressRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(BookingCheckoutState())
    val state: StateFlow<BookingCheckoutState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<BookingFlowEvent>()
    val events: SharedFlow<BookingFlowEvent> = _events.asSharedFlow()

    fun initCheckout(
        vehicle: VehicleResponse?,
        service: WashServiceResponse?,
        defaultAddress: AddressResponse? = null
    ) {
        _state.update {
            it.copy(
                selectedVehicle = vehicle,
                selectedService = service,
                addressLine1 = defaultAddress?.addressLine1 ?: it.addressLine1,
                addressLine2 = defaultAddress?.addressLine2 ?: it.addressLine2,
                city = defaultAddress?.city ?: it.city,
                state = defaultAddress?.state ?: it.state,
                postalCode = defaultAddress?.postalCode ?: it.postalCode,
                country = defaultAddress?.country ?: it.country,
                latitude = defaultAddress?.latitude ?: it.latitude,
                longitude = defaultAddress?.longitude ?: it.longitude,
                errorMessage = null
            )
        }

        // Fetch real available add-ons from backend
        viewModelScope.launch {
            val addonsRes = washServiceRepository.getAddons()
            addonsRes.getOrNull()?.let { list ->
                _state.update { it.copy(availableAddons = list.filter { addon -> addon.active }) }
            }

            // Check serviceability for current coordinates
            val currentLat = _state.value.latitude
            val currentLng = _state.value.longitude
            val checkRes = addressRepository.checkServiceability(currentLat, currentLng)
            checkRes.getOrNull()?.let { check ->
                _state.update {
                    it.copy(
                        isServiceable = check.serviceable,
                        serviceAreaName = check.serviceAreaName
                    )
                }
            }
        }
    }

    fun toggleAddon(addonId: String) {
        _state.update { current ->
            val newSelection = current.selectedAddonIds.toMutableSet()
            if (newSelection.contains(addonId)) {
                newSelection.remove(addonId)
            } else {
                newSelection.add(addonId)
            }
            current.copy(selectedAddonIds = newSelection)
        }
    }

    fun updateAddress(
        addressLine1: String,
        addressLine2: String,
        city: String,
        state: String,
        postalCode: String,
        country: String,
        latitude: Double,
        longitude: Double
    ) {
        _state.update {
            it.copy(
                addressLine1 = addressLine1,
                addressLine2 = addressLine2,
                city = city,
                state = state,
                postalCode = postalCode,
                country = country,
                latitude = latitude,
                longitude = longitude
            )
        }

        viewModelScope.launch {
            val checkRes = addressRepository.checkServiceability(latitude, longitude)
            checkRes.getOrNull()?.let { check ->
                _state.update {
                    it.copy(
                        isServiceable = check.serviceable,
                        serviceAreaName = check.serviceAreaName
                    )
                }
            }
        }
    }

    fun updateParkingType(type: ParkingType) {
        _state.update { it.copy(selectedParkingType = type) }
    }

    fun updateSlotNumber(slot: String) {
        _state.update { it.copy(slotNumber = slot) }
    }

    fun updateGateCode(code: String) {
        _state.update { it.copy(gateCode = code) }
    }

    fun updateCouponCode(coupon: String) {
        _state.update { it.copy(couponCode = coupon.trim().uppercase()) }
    }

    /**
     * Executes the booking creation and initiates server-side Razorpay payment order.
     * Absolutely NO fallback fake bookings.
     */
    fun bookAndProceedToPayment() {
        val currentState = _state.value
        val vehicle = currentState.selectedVehicle
        val service = currentState.selectedService

        if (vehicle == null) {
            _state.update { it.copy(errorMessage = "Please select a vehicle") }
            return
        }
        if (service == null) {
            _state.update { it.copy(errorMessage = "Please select a wash package") }
            return
        }
        if (currentState.addressLine1.isBlank()) {
            _state.update { it.copy(errorMessage = "Service location address is required") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // ISO-8601 UTC Instant format for scheduledStartTime (30 mins in the future)
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val scheduledTimeIso = isoFormat.format(Date(System.currentTimeMillis() + 1800_000))

                val addressDto = ServiceAddressDto(
                    addressLine1 = currentState.addressLine1.trim(),
                    addressLine2 = currentState.addressLine2.trim().ifBlank { null },
                    city = currentState.city.trim(),
                    state = currentState.state.trim(),
                    postalCode = currentState.postalCode.trim(),
                    country = currentState.country.trim(),
                    latitude = currentState.latitude,
                    longitude = currentState.longitude
                )

                val customerNotes = buildString {
                    append("Parking: ${currentState.selectedParkingType.label}. ")
                    if (currentState.slotNumber.isNotBlank()) append("Slot: ${currentState.slotNumber}. ")
                    if (currentState.gateCode.isNotBlank()) append("Access: ${currentState.gateCode}")
                }.trim()

                val request = CreateBookingRequest(
                    vehicleId = vehicle.id,
                    serviceId = service.id,
                    scheduledStartTime = scheduledTimeIso,
                    serviceAddress = addressDto,
                    customerNotes = customerNotes.ifBlank { null },
                    addOnIds = currentState.selectedAddonIds.toList(),
                    couponCode = currentState.couponCode.ifBlank { null }
                )

                // 1. Create booking on backend (locks immutable prices)
                val bookingResult = bookingRepository.createBooking(request)
                bookingResult.fold(
                    onSuccess = { booking ->
                        _state.update { it.copy(createdBooking = booking) }

                        // 2. Create Razorpay Payment Order on backend
                        val paymentOrderResult = paymentRepository.createPaymentOrder(booking.id)
                        paymentOrderResult.fold(
                            onSuccess = { paymentOrder ->
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        activePaymentOrder = paymentOrder
                                    )
                                }
                                _events.emit(BookingFlowEvent.LaunchRazorpay(paymentOrder, booking))
                            },
                            onFailure = { paymentError ->
                                val err = paymentError.message ?: "Failed to initiate payment"
                                _state.update { it.copy(isLoading = false, errorMessage = err) }
                                _events.emit(BookingFlowEvent.BookingFailed(err))
                            }
                        )
                    },
                    onFailure = { bookingError ->
                        val err = bookingError.message ?: "Failed to schedule appointment"
                        _state.update { it.copy(isLoading = false, errorMessage = err) }
                        _events.emit(BookingFlowEvent.BookingFailed(err))
                    }
                )
            } catch (e: Exception) {
                val err = e.message ?: "An unexpected error occurred"
                _state.update { it.copy(isLoading = false, errorMessage = err) }
                _events.emit(BookingFlowEvent.BookingFailed(err))
            }
        }
    }

    /**
     * Called by MainActivity Razorpay callback upon successful client payment.
     * Verifies the signature with the backend before completing flow.
     */
    fun onRazorpayPaymentSuccess(razorpayPaymentId: String, razorpaySignature: String) {
        val paymentOrder = _state.value.activePaymentOrder
        val booking = _state.value.createdBooking

        if (paymentOrder == null || booking == null) {
            _events.tryEmit(BookingFlowEvent.BookingFailed("Missing active payment order session"))
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val captureResult = paymentRepository.capturePayment(
                paymentId = paymentOrder.paymentId,
                razorpayPaymentId = razorpayPaymentId,
                razorpaySignature = razorpaySignature
            )
            _state.update { it.copy(isLoading = false) }
            captureResult.fold(
                onSuccess = { paymentResponse ->
                    _events.emit(BookingFlowEvent.BookingAndPaymentSuccess(booking, paymentResponse))
                },
                onFailure = { err ->
                    val msg = "Payment verification error: ${err.message}"
                    _state.update { it.copy(errorMessage = msg) }
                    _events.emit(BookingFlowEvent.BookingFailed(msg))
                }
            )
        }
    }

    fun onRazorpayPaymentFailed(code: Int, description: String) {
        _state.update { it.copy(isLoading = false, errorMessage = "Payment failed ($code): $description") }
        viewModelScope.launch {
            _events.emit(BookingFlowEvent.BookingFailed("Payment cancelled or failed: $description"))
        }
    }
}
