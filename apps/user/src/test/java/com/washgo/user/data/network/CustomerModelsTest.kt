package com.washgo.user.data.network

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomerModelsTest {

    private val gson = Gson()

    @Test
    fun washServiceDto_deserializesFromBackendResponse() {
        val json = """
            {
                "id": "a0000000-0000-0000-0000-000000000001",
                "name": "Basic Car Wash",
                "slug": "basic-car-wash",
                "category": "EXTERIOR",
                "shortDescription": "Exterior foam wash, tire dressing, and rinse",
                "description": "Complete exterior pressure rinse...",
                "durationMinutes": 30,
                "basePrice": 399.00,
                "active": true,
                "displayOrder": 1,
                "vehicleTypes": ["HATCHBACK", "SEDAN", "SUV"]
            }
        """.trimIndent()

        val service = gson.fromJson(json, WashServiceDto::class.java)

        assertNotNull(service)
        assertEquals("a0000000-0000-0000-0000-000000000001", service.id)
        assertEquals("Basic Car Wash", service.name)
        assertEquals(399.00, service.basePrice, 0.01)
        assertEquals(30, service.durationMinutes)
        assertTrue(service.active)
        assertEquals(3, service.vehicleTypes?.size)
    }

    @Test
    fun vehicleDto_deserializesFromBackendResponse() {
        val json = """
            {
                "id": "veh-1234-uuid",
                "vehicleType": "SEDAN",
                "make": "Hyundai",
                "model": "Verna",
                "year": 2023,
                "registrationNumber": "KA-01-MJ-5566",
                "color": "Starry Night",
                "nickname": "Daily Driver",
                "isDefault": true
            }
        """.trimIndent()

        val vehicle = gson.fromJson(json, VehicleDto::class.java)

        assertNotNull(vehicle)
        assertEquals("veh-1234-uuid", vehicle.id)
        assertEquals("SEDAN", vehicle.vehicleType)
        assertEquals("Hyundai", vehicle.make)
        assertEquals("KA-01-MJ-5566", vehicle.registrationNumber)
        assertTrue(vehicle.isDefault)
    }

    @Test
    fun createBookingRequest_serializesMatchingBackendDto() {
        val request = CreateBookingRequest(
            vehicleId = "veh-uuid-1",
            serviceId = "svc-uuid-2",
            scheduledStartTime = "2026-10-14T15:00:00Z",
            serviceAddress = ServiceAddressDto(
                addressLine1 = "12th Main Road, Indiranagar",
                city = "Bengaluru",
                state = "Karnataka",
                postalCode = "560038",
                country = "India",
                latitude = 12.9716,
                longitude = 77.5946
            ),
            customerNotes = "Please bring extra microfiber cloth"
        )

        val json = gson.toJson(request)

        assertTrue(json.contains("\"vehicleId\":\"veh-uuid-1\""))
        assertTrue(json.contains("\"serviceId\":\"svc-uuid-2\""))
        assertTrue(json.contains("\"scheduledStartTime\":\"2026-10-14T15:00:00Z\""))
        assertTrue(json.contains("\"addressLine1\":\"12th Main Road, Indiranagar\""))
        assertTrue(json.contains("\"latitude\":12.9716"))
        assertTrue(json.contains("\"longitude\":77.5946"))
    }

    @Test
    fun bookingResponseDto_deserializesWithNestedObjects() {
        val json = """
            {
                "id": "bk-999-uuid",
                "bookingReference": "WG-20261014-9999",
                "status": "CREATED",
                "servicePrice": 499.00,
                "serviceNameSnapshot": "Premium Car Wash",
                "scheduledStartTime": "2026-10-14T15:00:00Z",
                "customer": {
                    "id": "cust-1",
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "john@example.com"
                },
                "serviceAddress": {
                    "addressLine1": "Indiranagar",
                    "city": "Bengaluru",
                    "state": "Karnataka",
                    "postalCode": "560038",
                    "country": "India",
                    "latitude": 12.9716,
                    "longitude": 77.5946
                }
            }
        """.trimIndent()

        val response = gson.fromJson(json, BookingResponseDto::class.java)

        assertNotNull(response)
        assertEquals("bk-999-uuid", response.id)
        assertEquals("WG-20261014-9999", response.bookingReference)
        assertEquals("CREATED", response.status)
        assertEquals(499.00, response.servicePrice, 0.01)
        assertEquals("John", response.customer?.firstName)
        assertEquals(12.9716, response.serviceAddress?.latitude ?: 0.0, 0.0001)
    }
}
