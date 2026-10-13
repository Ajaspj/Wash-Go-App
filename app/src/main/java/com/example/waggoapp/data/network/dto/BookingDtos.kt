package com.example.waggoapp.data.network.dto

import com.google.gson.annotations.SerializedName

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    ASSIGNED,
    STAFF_ACCEPTED,
    EN_ROUTE,
    ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW;

    fun isTerminal(): Boolean = this == COMPLETED || this == CANCELLED || this == NO_SHOW

    fun isCancellable(): Boolean = this == PENDING || this == CONFIRMED || this == ASSIGNED || this == EN_ROUTE
}

data class ServiceAddressDto(
    @SerializedName("addressLine1")
    val addressLine1: String,

    @SerializedName("addressLine2")
    val addressLine2: String? = null,

    @SerializedName("city")
    val city: String,

    @SerializedName("state")
    val state: String,

    @SerializedName("postalCode")
    val postalCode: String,

    @SerializedName("country")
    val country: String = "USA",

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
)

data class CreateBookingRequest(
    @SerializedName("vehicleId")
    val vehicleId: String,

    @SerializedName("serviceId")
    val serviceId: String,

    @SerializedName("scheduledStartTime")
    val scheduledStartTime: String, // ISO-8601 Instant e.g. 2026-10-12T12:00:00Z

    @SerializedName("serviceAddress")
    val serviceAddress: ServiceAddressDto,

    @SerializedName("customerNotes")
    val customerNotes: String? = null,

    @SerializedName("addOnIds")
    val addOnIds: List<String> = emptyList(),

    @SerializedName("couponCode")
    val couponCode: String? = null
)

data class CancelBookingRequest(
    @SerializedName("reason")
    val reason: String? = null
)

data class CustomerSummaryResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("firstName")
    val firstName: String?,

    @SerializedName("lastName")
    val lastName: String?,

    @SerializedName("email")
    val email: String?
)

data class VehicleSummaryResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("registrationNumber")
    val registrationNumber: String,

    @SerializedName("make")
    val make: String,

    @SerializedName("model")
    val model: String,

    @SerializedName("vehicleType")
    val vehicleType: VehicleType
)

data class ServiceSummaryResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("category")
    val category: ServiceCategory,

    @SerializedName("durationMinutes")
    val durationMinutes: Int
)

data class StaffSummaryResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("staffCode")
    val staffCode: String?,

    @SerializedName("displayName")
    val displayName: String?
)

data class BookingResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("bookingReference")
    val bookingReference: String,

    @SerializedName("status")
    val status: BookingStatus,

    @SerializedName("customer")
    val customer: CustomerSummaryResponse?,

    @SerializedName("vehicle")
    val vehicle: VehicleSummaryResponse?,

    @SerializedName("service")
    val service: ServiceSummaryResponse?,

    @SerializedName("servicePrice")
    val servicePrice: Double?,

    @SerializedName("serviceNameSnapshot")
    val serviceNameSnapshot: String?,

    @SerializedName("scheduledStartTime")
    val scheduledStartTime: String?,

    @SerializedName("scheduledEndTime")
    val scheduledEndTime: String?,

    @SerializedName("serviceAddress")
    val serviceAddress: ServiceAddressDto?,

    @SerializedName("customerNotes")
    val customerNotes: String?,

    @SerializedName("cancellationReason")
    val cancellationReason: String?,

    @SerializedName("assignedStaff")
    val assignedStaff: StaffSummaryResponse?,

    @SerializedName("assignedAt")
    val assignedAt: String?,

    @SerializedName("acceptedAt")
    val acceptedAt: String?,

    @SerializedName("enRouteAt")
    val enRouteAt: String?,

    @SerializedName("arrivedAt")
    val arrivedAt: String?,

    @SerializedName("washingStartedAt")
    val washingStartedAt: String?,

    @SerializedName("completedAt")
    val completedAt: String?,

    @SerializedName("cancelledAt")
    val cancelledAt: String?,

    @SerializedName("noShowAt")
    val noShowAt: String?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)

data class TrackingResponse(
    @SerializedName("bookingReference")
    val bookingReference: String,

    @SerializedName("status")
    val status: BookingStatus,

    @SerializedName("scheduledStartTime")
    val scheduledStartTime: String?,

    @SerializedName("scheduledEndTime")
    val scheduledEndTime: String?,

    @SerializedName("assignedStaff")
    val assignedStaff: StaffSummaryResponse?,

    @SerializedName("assignedAt")
    val assignedAt: String?,

    @SerializedName("acceptedAt")
    val acceptedAt: String?,

    @SerializedName("enRouteAt")
    val enRouteAt: String?,

    @SerializedName("arrivedAt")
    val arrivedAt: String?,

    @SerializedName("washingStartedAt")
    val washingStartedAt: String?,

    @SerializedName("completedAt")
    val completedAt: String?
)

data class StatusHistoryResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("previousStatus")
    val previousStatus: BookingStatus,

    @SerializedName("newStatus")
    val newStatus: BookingStatus,

    @SerializedName("changedByUserId")
    val changedByUserId: String?,

    @SerializedName("reason")
    val reason: String?,

    @SerializedName("changedAt")
    val changedAt: String?
)
