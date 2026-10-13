package com.example.waggoapp

import com.example.waggoapp.data.network.dto.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DtoSerializationTest {

    private val gson = Gson()

    @Test
    fun testApiResponseEnvelopeDeserialization() {
        val json = """
            {
                "data": {
                    "accessToken": "eyJh...access",
                    "refreshToken": "refresh-uuid-1234",
                    "tokenType": "Bearer",
                    "expiresIn": 900
                }
            }
        """.trimIndent()

        val type = object : TypeToken<ApiResponse<AuthResponse>>() {}.type
        val envelope: ApiResponse<AuthResponse> = gson.fromJson(json, type)

        assertNotNull(envelope.data)
        assertEquals("refresh-uuid-1234", envelope.data.refreshToken)
        assertEquals("Bearer", envelope.data.tokenType)
    }

    @Test
    fun testVehicleResponseDeserialization() {
        val json = """
            {
                "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                "vehicleType": "SEDAN",
                "make": "Tesla",
                "model": "Model 3",
                "year": 2024,
                "registrationNumber": "CA8XYZ49",
                "color": "Midnight Silver",
                "nickname": "Daily Driver",
                "isDefault": true,
                "createdAt": "2026-10-12T07:00:00Z",
                "updatedAt": "2026-10-12T07:00:00Z"
            }
        """.trimIndent()

        val vehicle = gson.fromJson(json, VehicleResponse::class.java)
        assertEquals("3fa85f64-5717-4562-b3fc-2c963f66afa6", vehicle.id)
        assertEquals(VehicleType.SEDAN, vehicle.vehicleType)
        assertEquals("Tesla", vehicle.make)
        assertEquals(true, vehicle.isDefault)
    }

    @Test
    fun testWashServiceResponseDeserialization() {
        val json = """
            {
                "id": "srv-001",
                "name": "Foam & Interior Spa",
                "slug": "foam-interior-spa",
                "description": "Premium multi-stage exterior preservation",
                "category": "FULL_WASH",
                "basePrice": 59.0,
                "durationMinutes": 60,
                "active": true,
                "displayOrder": 1
            }
        """.trimIndent()

        val service = gson.fromJson(json, WashServiceResponse::class.java)
        assertEquals("srv-001", service.id)
        assertEquals(ServiceCategory.FULL_WASH, service.category)
        assertEquals(59.0, service.basePrice, 0.001)
        assertEquals(60, service.durationMinutes)
    }

    @Test
    fun testBookingResponseDeserialization() {
        val json = """
            {
                "id": "b-100",
                "bookingReference": "WG-2026-8942",
                "status": "CONFIRMED",
                "servicePrice": 59.0,
                "serviceNameSnapshot": "Foam & Interior Spa",
                "scheduledStartTime": "2026-10-12T10:00:00Z"
            }
        """.trimIndent()

        val booking = gson.fromJson(json, BookingResponse::class.java)
        assertEquals("b-100", booking.id)
        assertEquals("WG-2026-8942", booking.bookingReference)
        assertEquals(BookingStatus.CONFIRMED, booking.status)
        assertEquals(59.0, booking.servicePrice ?: 0.0, 0.001)
    }
}
