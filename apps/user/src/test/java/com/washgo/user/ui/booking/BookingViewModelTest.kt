package com.washgo.user.ui.booking

import com.washgo.core.model.VehicleCategory
import com.washgo.user.data.network.NearbyAgentDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: BookingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = BookingViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun defaultLocationState_isInitializedCorrectly() {
        val state = viewModel.locationState.value
        assertEquals(12.9716, state.latitude, 0.0001)
        assertEquals(77.5946, state.longitude, 0.0001)
        assertFalse(state.isLocked)
        assertFalse(state.isDragging)
        assertEquals(BookingUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun updatePinLocation_updatesCoordinatesAndAddress() {
        viewModel.updatePinLocation(12.9800, 77.6000)
        val state = viewModel.locationState.value
        assertEquals(12.9800, state.latitude, 0.0001)
        assertEquals(77.6000, state.longitude, 0.0001)
        assertTrue(state.address.isNotEmpty())
    }

    @Test
    fun categoryAndPackageSelection_updatesCalculatedPricing() {
        // Default: SEDAN, Express Wash (299)
        assertEquals(VehicleCategory.SEDAN, viewModel.selectedCategory.value)
        assertEquals(299.0, viewModel.calculateBasePrice(), 0.01)
        assertEquals(0.0, viewModel.calculateAddOnsTotal(), 0.01)
        assertEquals(299.0, viewModel.calculateTotalAmount(), 0.01)

        // Switch to Bike: Express Wash should be 149
        viewModel.selectCategory(VehicleCategory.BIKE)
        assertEquals(149.0, viewModel.calculateBasePrice(), 0.01)

        // Select Deluxe package (Bike: 249)
        val deluxe = viewModel.packages.first { it.id == "pkg-deluxe" }
        viewModel.selectPackage(deluxe)
        assertEquals(249.0, viewModel.calculateBasePrice(), 0.01)

        // Toggle Tire Shine add-on (49)
        viewModel.toggleAddOn("addon-tire")
        assertEquals(49.0, viewModel.calculateAddOnsTotal(), 0.01)
        assertEquals(298.0, viewModel.calculateTotalAmount(), 0.01)

        // Untoggle Tire Shine
        viewModel.toggleAddOn("addon-tire")
        assertEquals(0.0, viewModel.calculateAddOnsTotal(), 0.01)
        assertEquals(249.0, viewModel.calculateTotalAmount(), 0.01)
    }

    @Test
    fun setSortOption_sortsAgentsProperly() {
        val agent1 = NearbyAgentDto(
            id = "1",
            name = "Ramesh",
            phone = "123",
            isAvailable = true,
            rating = 4.9,
            latitude = 12.97,
            longitude = 77.59,
            distanceMeters = 800.0,
            distanceKm = 0.8,
            etaMinutes = 6,
            formattedEta = "6 mins"
        )
        val agent2 = NearbyAgentDto(
            id = "2",
            name = "Suresh",
            phone = "456",
            isAvailable = true,
            rating = 4.6,
            latitude = 12.97,
            longitude = 77.59,
            distanceMeters = 400.0,
            distanceKm = 0.4,
            etaMinutes = 3,
            formattedEta = "3 mins"
        )

        // Test Fastest ETA sorting
        viewModel.setSortOption(AgentSortOption.FASTEST_ETA)
        assertEquals(AgentSortOption.FASTEST_ETA, viewModel.sortOption.value)

        // Test Top Rated sorting
        viewModel.setSortOption(AgentSortOption.TOP_RATED)
        assertEquals(AgentSortOption.TOP_RATED, viewModel.sortOption.value)
    }

    @Test
    fun unlockLocation_resetsLockStateAndUiState() {
        viewModel.unlockLocation()
        assertFalse(viewModel.locationState.value.isLocked)
        assertEquals(BookingUiState.Idle, viewModel.uiState.value)
    }
}
