package com.washgo.agent.data.network

import com.washgo.agent.data.model.AcceptBookingRequest
import com.washgo.agent.data.model.AgentApiResponse
import com.washgo.agent.data.model.AgentProfileDto
import com.washgo.agent.data.model.AgentStatsDto
import com.washgo.agent.data.model.AvailabilityToggleRequest
import com.washgo.agent.data.model.BookingMatchResult
import com.washgo.agent.data.model.LocationUpdateRequest
import com.washgo.agent.data.model.OnlineToggleRequest
import com.washgo.agent.data.model.RefreshTokenRequest
import com.washgo.agent.data.model.RejectBookingRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AgentApi {

    @GET("api/v1/agent/me")
    suspend fun getSelf(): Response<AgentApiResponse<AgentProfileDto>>

    @POST("api/v1/agent/me/submit-review")
    suspend fun submitForReview(): Response<AgentApiResponse<AgentProfileDto>>

    @GET("api/v1/agent/me/stats")
    suspend fun getStats(): Response<AgentApiResponse<AgentStatsDto>>

    @PATCH("api/v1/agent/me/online")
    suspend fun toggleOnline(
        @Body request: OnlineToggleRequest
    ): Response<AgentApiResponse<AgentProfileDto>>

    @PATCH("api/v1/agent/me/availability")
    suspend fun toggleAvailability(
        @Body request: AvailabilityToggleRequest
    ): Response<AgentApiResponse<AgentProfileDto>>

    @PATCH("api/v1/agent/me/location")
    suspend fun updateLocation(
        @Body request: LocationUpdateRequest
    ): Response<AgentApiResponse<AgentProfileDto>>

    @POST("api/bookings/{bookingId}/accept")
    suspend fun acceptBooking(
        @Path("bookingId") bookingId: String,
        @Body request: AcceptBookingRequest
    ): Response<AgentApiResponse<BookingMatchResult>>

    @POST("api/bookings/{bookingId}/reject")
    suspend fun rejectBooking(
        @Path("bookingId") bookingId: String,
        @Body request: RejectBookingRequest
    ): Response<AgentApiResponse<String>>

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body request: RefreshTokenRequest
    ): Response<Unit>
}
