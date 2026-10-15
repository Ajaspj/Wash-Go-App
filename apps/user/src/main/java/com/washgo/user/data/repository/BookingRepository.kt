package com.washgo.user.data.repository

import com.washgo.core.model.VehicleCategory
import com.washgo.user.data.network.ApiClient
import com.washgo.user.data.network.BookingResponseDto
import com.washgo.user.data.network.CancelBookingRequest
import com.washgo.user.data.network.CreateBookingRequest
import com.washgo.user.data.network.CreateDispatchRequest
import com.washgo.user.data.network.CreateVehicleRequest
import com.washgo.user.data.network.CustomerApi
import com.washgo.user.data.network.DispatchOfferResponse
import com.washgo.user.data.network.NearbyAgentApi
import com.washgo.user.data.network.ServiceAddressDto
import com.washgo.user.data.network.VehicleDto
import com.washgo.user.data.network.WashServiceDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.Instant
import java.time.temporal.ChronoUnit

class BookingRepository(
    private val customerApi: CustomerApi = ApiClient.customerApi,
    private val nearbyAgentApi: NearbyAgentApi = ApiClient.nearbyAgentApi
) {

    suspend fun getServices(vehicleType: String? = null): Result<List<WashServiceDto>> = withContext(Dispatchers.IO) {
        try {
            val response = customerApi.getServices(vehicleType = vehicleType)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to load services catalog"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomerVehicles(): Result<List<VehicleDto>> = withContext(Dispatchers.IO) {
        try {
            val response = customerApi.getVehicles()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to load vehicles"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createVehicle(
        category: VehicleCategory,
        make: String = if (category == VehicleCategory.BIKE) "Honda" else "Hyundai",
        model: String = if (category == VehicleCategory.BIKE) "Activa 6G" else "Creta",
        regNumber: String = "KA-01-MJ-${(1000..9999).random()}",
        color: String = "Pearl White"
    ): Result<VehicleDto> = withContext(Dispatchers.IO) {
        try {
            val typeStr = if (category == VehicleCategory.BIKE) "SCOOTER" else "SEDAN"
            val request = CreateVehicleRequest(
                vehicleType = typeStr,
                make = make,
                model = model,
                year = 2023,
                registrationNumber = regNumber,
                color = color,
                nickname = if (category == VehicleCategory.BIKE) "My Bike" else "Primary Car",
                isDefault = true
            )
            val response = customerApi.createVehicle(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to create vehicle"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrCreateDefaultVehicle(category: VehicleCategory): Result<VehicleDto> = withContext(Dispatchers.IO) {
        val existingResult = getCustomerVehicles()
        if (existingResult.isSuccess) {
            val vehicles = existingResult.getOrNull() ?: emptyList()
            val matched = vehicles.firstOrNull { it.isDefault } ?: vehicles.firstOrNull()
            if (matched != null) {
                return@withContext Result.success(matched)
            }
        }
        createVehicle(category)
    }

    suspend fun createBooking(
        vehicleId: String,
        serviceId: String,
        latitude: Double,
        longitude: Double,
        address: String,
        customerNotes: String? = null,
        scheduledStartTime: Instant = Instant.now().plus(15, ChronoUnit.MINUTES)
    ): Result<BookingResponseDto> = withContext(Dispatchers.IO) {
        try {
            val serviceAddress = ServiceAddressDto(
                addressLine1 = address.take(250),
                addressLine2 = null,
                city = "Bengaluru",
                state = "Karnataka",
                postalCode = "560038",
                country = "India",
                latitude = latitude,
                longitude = longitude
            )
            val request = CreateBookingRequest(
                vehicleId = vehicleId,
                serviceId = serviceId,
                scheduledStartTime = scheduledStartTime.toString(),
                serviceAddress = serviceAddress,
                customerNotes = customerNotes
            )
            val response = customerApi.createBooking(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to create booking"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBooking(id: String): Result<BookingResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = customerApi.getBookingById(id)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to get booking details"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelBooking(id: String, reason: String? = "Cancelled by customer"): Result<BookingResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = customerApi.cancelBooking(id, CancelBookingRequest(reason))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Failed to cancel booking"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun dispatchBookingOffer(request: CreateDispatchRequest): Result<DispatchOfferResponse> = withContext(Dispatchers.IO) {
        try {
            val response = nearbyAgentApi.dispatchBooking(request)
            if (response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to dispatch offer to agent"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val json = JSONObject(errorBody)
            when {
                json.has("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}
