package com.washgo.user.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CustomerApi {

    @GET("api/v1/services")
    suspend fun getServices(
        @Query("vehicleType") vehicleType: String? = null,
        @Query("category") category: String? = null
    ): Response<ApiResponse<List<WashServiceDto>>>

    @GET("api/v1/vehicles")
    suspend fun getVehicles(): Response<ApiResponse<List<VehicleDto>>>

    @POST("api/v1/vehicles")
    suspend fun createVehicle(
        @Body request: CreateVehicleRequest
    ): Response<ApiResponse<VehicleDto>>

    @POST("api/v1/bookings")
    suspend fun createBooking(
        @Body request: CreateBookingRequest
    ): Response<ApiResponse<BookingResponseDto>>

    @GET("api/v1/bookings")
    suspend fun listBookings(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<BookingPageResponse<BookingResponseDto>>>

    @GET("api/v1/bookings/{id}")
    suspend fun getBookingById(
        @Path("id") id: String
    ): Response<ApiResponse<BookingResponseDto>>

    @POST("api/v1/bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") id: String,
        @Body request: CancelBookingRequest? = null
    ): Response<ApiResponse<BookingResponseDto>>
}
