package com.washgo.user.data.network

data class WashServiceDto(
    val id: String,
    val name: String,
    val slug: String,
    val category: String,
    val shortDescription: String? = null,
    val description: String? = null,
    val durationMinutes: Int = 30,
    val basePrice: Double = 0.0,
    val active: Boolean = true,
    val displayOrder: Int = 0,
    val vehicleTypes: List<String>? = null
)

data class VehicleDto(
    val id: String,
    val vehicleType: String,
    val make: String,
    val model: String,
    val year: Int,
    val registrationNumber: String,
    val color: String,
    val nickname: String? = null,
    val isDefault: Boolean = false
)

data class CreateVehicleRequest(
    val vehicleType: String,
    val make: String,
    val model: String,
    val year: Int,
    val registrationNumber: String,
    val color: String,
    val nickname: String? = null,
    val isDefault: Boolean = true
)

data class ServiceAddressDto(
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String = "Bengaluru",
    val state: String = "Karnataka",
    val postalCode: String = "560038",
    val country: String = "India",
    val latitude: Double,
    val longitude: Double
)

data class CreateBookingRequest(
    val vehicleId: String,
    val serviceId: String,
    val scheduledStartTime: String,
    val serviceAddress: ServiceAddressDto,
    val customerNotes: String? = null,
    val addOnIds: List<String> = emptyList(),
    val couponCode: String? = null
)

data class CancelBookingRequest(
    val reason: String? = null
)

data class CustomerSummaryDto(
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null
)

data class VehicleSummaryDto(
    val id: String,
    val registrationNumber: String? = null,
    val make: String? = null,
    val model: String? = null,
    val vehicleType: String? = null
)

data class ServiceSummaryDto(
    val id: String,
    val name: String? = null,
    val slug: String? = null,
    val category: String? = null,
    val durationMinutes: Int? = null
)

data class StaffSummaryDto(
    val id: String,
    val staffCode: String? = null,
    val displayName: String? = null
)

data class BookingResponseDto(
    val id: String,
    val bookingReference: String? = null,
    val status: String,
    val customer: CustomerSummaryDto? = null,
    val vehicle: VehicleSummaryDto? = null,
    val service: ServiceSummaryDto? = null,
    val servicePrice: Double = 0.0,
    val serviceNameSnapshot: String? = null,
    val scheduledStartTime: String? = null,
    val scheduledEndTime: String? = null,
    val serviceAddress: ServiceAddressDto? = null,
    val customerNotes: String? = null,
    val cancellationReason: String? = null,
    val assignedStaff: StaffSummaryDto? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class BookingPageResponse<T>(
    val content: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 10,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true
)
