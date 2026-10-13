package com.example.waggoapp.data.network

import com.example.waggoapp.data.network.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Complete Retrofit interface mapping to live Spring Boot REST endpoints on Wash-Go backend.
 */
interface WashGoApiService {

    // ==========================================
    // AUTHENTICATION & PROFILE
    // ==========================================

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<AuthResponse>>

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthResponse>>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequest
    ): Response<ApiResponse<AuthResponse>>

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<Unit>

    @POST("api/v1/auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<Unit>

    @GET("api/v1/users/me")
    suspend fun getProfile(): Response<ApiResponse<ProfileResponse>>

    @PUT("api/v1/users/me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<ProfileResponse>>

    // ==========================================
    // CUSTOMER VEHICLES
    // ==========================================

    @GET("api/v1/vehicles")
    suspend fun getVehicles(): Response<ApiResponse<List<VehicleResponse>>>

    @POST("api/v1/vehicles")
    suspend fun createVehicle(
        @Body request: CreateVehicleRequest
    ): Response<ApiResponse<VehicleResponse>>

    @GET("api/v1/vehicles/{id}")
    suspend fun getVehicleById(
        @Path("id") id: String
    ): Response<ApiResponse<VehicleResponse>>

    @PUT("api/v1/vehicles/{id}")
    suspend fun updateVehicle(
        @Path("id") id: String,
        @Body request: UpdateVehicleRequest
    ): Response<ApiResponse<VehicleResponse>>

    @DELETE("api/v1/vehicles/{id}")
    suspend fun deleteVehicle(
        @Path("id") id: String
    ): Response<Unit>

    @PATCH("api/v1/vehicles/{id}/default")
    suspend fun setDefaultVehicle(
        @Path("id") id: String
    ): Response<ApiResponse<VehicleResponse>>

    // ==========================================
    // WASH SERVICES & ADD-ONS
    // ==========================================

    @GET("api/v1/services")
    suspend fun getActiveServices(
        @Query("vehicleType") vehicleType: VehicleType? = null,
        @Query("category") category: ServiceCategory? = null
    ): Response<ApiResponse<List<WashServiceResponse>>>

    @GET("api/v1/services/{id}")
    suspend fun getActiveServiceById(
        @Path("id") id: String
    ): Response<ApiResponse<WashServiceResponse>>

    @GET("api/v1/services/slug/{slug}")
    suspend fun getActiveServiceBySlug(
        @Path("slug") slug: String
    ): Response<ApiResponse<WashServiceResponse>>

    @GET("api/v1/addons")
    suspend fun getAddons(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<AddonResponse>>>

    // ==========================================
    // CUSTOMER ADDRESSES & SERVICEABILITY
    // ==========================================

    @GET("api/v1/customers/me/addresses")
    suspend fun getAddresses(): Response<ApiResponse<List<AddressResponse>>>

    @POST("api/v1/customers/me/addresses")
    suspend fun createAddress(
        @Body request: AddressRequest
    ): Response<ApiResponse<AddressResponse>>

    @GET("api/v1/customers/me/addresses/{id}")
    suspend fun getAddressById(
        @Path("id") id: String
    ): Response<ApiResponse<AddressResponse>>

    @PUT("api/v1/customers/me/addresses/{id}")
    suspend fun updateAddress(
        @Path("id") id: String,
        @Body request: AddressRequest
    ): Response<ApiResponse<AddressResponse>>

    @DELETE("api/v1/customers/me/addresses/{id}")
    suspend fun deleteAddress(
        @Path("id") id: String
    ): Response<Unit>

    @PATCH("api/v1/customers/me/addresses/{id}/default")
    suspend fun setDefaultAddress(
        @Path("id") id: String
    ): Response<ApiResponse<AddressResponse>>

    @GET("api/v1/serviceability/check")
    suspend fun checkServiceability(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<ApiResponse<ServiceAreaCheckResponse>>

    // ==========================================
    // BOOKINGS & LIFECYCLE
    // ==========================================

    @POST("api/v1/bookings")
    suspend fun createBooking(
        @Body request: CreateBookingRequest
    ): Response<ApiResponse<BookingResponse>>

    @GET("api/v1/bookings")
    suspend fun listCustomerBookings(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PageResponse<BookingResponse>>>

    @GET("api/v1/bookings/current")
    suspend fun getCurrentBookings(): Response<ApiResponse<List<BookingResponse>>>

    @GET("api/v1/bookings/{id}")
    suspend fun getBookingById(
        @Path("id") id: String
    ): Response<ApiResponse<BookingResponse>>

    @GET("api/v1/bookings/reference/{bookingReference}")
    suspend fun getBookingByReference(
        @Path("bookingReference") bookingReference: String
    ): Response<ApiResponse<BookingResponse>>

    @POST("api/v1/bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") id: String,
        @Body request: CancelBookingRequest? = null
    ): Response<ApiResponse<BookingResponse>>

    @GET("api/v1/customer/bookings/{id}/tracking")
    suspend fun getTracking(
        @Path("id") id: String
    ): Response<ApiResponse<TrackingResponse>>

    @GET("api/v1/bookings/{id}/history")
    suspend fun getBookingHistory(
        @Path("id") id: String
    ): Response<ApiResponse<List<StatusHistoryResponse>>>

    // ==========================================
    // PAYMENTS & RAZORPAY
    // ==========================================

    @POST("api/v1/bookings/{bookingId}/payments")
    suspend fun createPaymentOrder(
        @Path("bookingId") bookingId: String
    ): Response<ApiResponse<PaymentOrderResponse>>

    @GET("api/v1/bookings/{bookingId}/payments")
    suspend fun listBookingPayments(
        @Path("bookingId") bookingId: String
    ): Response<ApiResponse<List<PaymentResponse>>>

    @POST("api/v1/payments/{paymentId}/capture")
    suspend fun capturePayment(
        @Path("paymentId") paymentId: String,
        @Body request: CapturePaymentRequest
    ): Response<ApiResponse<PaymentResponse>>

    // ==========================================
    // COUPONS
    // ==========================================

    @POST("api/v1/coupons/validate")
    suspend fun validateCoupon(
        @Body request: ValidateCouponRequest
    ): Response<ApiResponse<CouponValidationResponse>>

    // ==========================================
    // REVIEWS
    // ==========================================

    @POST("api/v1/bookings/{id}/review")
    suspend fun createBookingReview(
        @Path("id") bookingId: String,
        @Body request: CreateReviewRequest
    ): Response<ApiResponse<ReviewResponse>>

    @GET("api/v1/bookings/{id}/review")
    suspend fun getBookingReview(
        @Path("id") bookingId: String
    ): Response<ApiResponse<ReviewResponse>>

    @GET("api/v1/services/{id}/reviews")
    suspend fun getServiceReviews(
        @Path("id") serviceId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<ReviewResponse>>>

    // ==========================================
    // NOTIFICATIONS & DEVICE TOKENS
    // ==========================================

    @GET("api/v1/customer/notifications")
    suspend fun getCustomerNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<NotificationResponse>>>

    @PATCH("api/v1/customer/notifications/{id}/read")
    suspend fun markNotificationRead(
        @Path("id") id: String
    ): Response<ApiResponse<NotificationResponse>>

    @PATCH("api/v1/customer/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<Unit>

    @POST("api/v1/users/me/device-tokens")
    suspend fun registerDeviceToken(
        @Body request: DeviceTokenRequest
    ): Response<ApiResponse<DeviceTokenResponse>>

    @DELETE("api/v1/users/me/device-tokens/{id}")
    suspend fun deleteDeviceToken(
        @Path("id") id: String
    ): Response<Unit>
}
