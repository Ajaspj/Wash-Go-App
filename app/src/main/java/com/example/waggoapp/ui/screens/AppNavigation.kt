package com.example.waggoapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waggoapp.data.network.ApiClient
import com.example.waggoapp.data.network.dto.BookingResponse
import com.example.waggoapp.data.network.dto.PaymentOrderResponse
import com.example.waggoapp.ui.theme.*
import com.example.waggoapp.ui.viewmodel.*
import kotlinx.coroutines.flow.collectLatest

enum class AppDestination(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Home", Icons.Filled.DirectionsCar, Icons.Outlined.DirectionsCar),
    BOOKING("Book", Icons.Filled.AddLocationAlt, Icons.Outlined.AddLocationAlt),
    TRACKING("Tracking", Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

/**
 * Root Navigation & State Container orchestrating the authenticated customer flow.
 * Auth lives outside the main shell so login/signup never renders the bottom navigation.
 */
@Composable
fun HydroGlideApp(
    bookingViewModel: BookingViewModel,
    onLaunchRazorpay: (PaymentOrderResponse, BookingResponse) -> Unit = { _, _ -> },
    authViewModel: AuthViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    trackingViewModel: TrackingViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val homeDashboardState by homeViewModel.state.collectAsState()
    val bookingCheckoutState by bookingViewModel.state.collectAsState()
    val trackingUiState by trackingViewModel.state.collectAsState()

    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }

    // Listen to session expiry events from TokenAuthenticator (e.g., refresh failure)
    LaunchedEffect(Unit) {
        try {
            ApiClient.getTokenAuthenticator().sessionExpiredEvents.collectLatest {
                currentDestination = AppDestination.HOME
            }
        } catch (ignored: Exception) {
        }
    }

    // Listen to BookingViewModel flow events
    LaunchedEffect(bookingViewModel) {
        bookingViewModel.events.collectLatest { event ->
            when (event) {
                is BookingFlowEvent.LaunchRazorpay -> {
                    onLaunchRazorpay(event.paymentOrder, event.booking)
                }
                is BookingFlowEvent.BookingAndPaymentSuccess -> {
                    trackingViewModel.loadTracking(
                        bookingId = event.booking.id,
                        reference = event.booking.bookingReference
                    )
                    currentDestination = AppDestination.TRACKING
                }
                is BookingFlowEvent.BookingFailed -> {
                    // Handled in BookingPlacementScreen UI state error message
                }
            }
        }
    }

    // Auth screen renders exclusively when user is not logged in
    if (!isLoggedIn) {
        AuthScreen(
            state = authState,
            onToggleAuthMode = authViewModel::toggleAuthMode,
            onFullNameChange = authViewModel::updateFullName,
            onEmailChange = authViewModel::updateEmail,
            onPhoneChange = authViewModel::updatePhone,
            onPasswordChange = authViewModel::updatePassword,
            onTogglePasswordVisibility = authViewModel::togglePasswordVisibility,
            onRememberMeChange = authViewModel::updateRememberMe,
            onSubmit = {
                authViewModel.submit()
            }
        )
        return
    }

    // Authenticated Shell with NavigationBar
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceDark,
        bottomBar = {
            Surface(
                color = SurfaceContainerLowest.copy(alpha = 0.95f),
                shadowElevation = 16.dp,
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(OutlineVariant.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    AppDestination.values().forEach { destination ->
                        val isSelected = currentDestination == destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.label
                                )
                            },
                            label = {
                                Text(
                                    text = destination.label,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SurfaceContainerLowest,
                                selectedTextColor = PrimaryContainerCyan,
                                indicatorColor = PrimaryContainerCyan,
                                unselectedIconColor = OnSurfaceVariantSlate,
                                unselectedTextColor = OnSurfaceVariantSlate
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (currentDestination) {
                AppDestination.HOME -> {
                    // Map real backend state to HomeScreen representation
                    val mappedVehicles = homeDashboardState.vehicles.map { veh ->
                        VehicleProfile(
                            id = veh.id,
                            type = veh.vehicleType.name,
                            model = "${veh.make} ${veh.model}",
                            color = veh.color,
                            category = if (veh.vehicleType.name.contains("BIKE") || veh.vehicleType.name.contains("TWO")) VehicleCategory.TWO_WHEELER else VehicleCategory.FOUR_WHEELER,
                            isSelected = (veh.id == homeDashboardState.selectedVehicleId)
                        )
                    }

                    val mappedPackages = homeDashboardState.services.map { svc ->
                        WashPackageItem(
                            id = svc.id,
                            title = svc.name,
                            badge = svc.category.name.replace("_", " "),
                            durationText = "${svc.durationMinutes} mins mobile service • On-site waterless eco setup",
                            originalPrice = "$${(svc.basePrice * 1.25).toInt()}",
                            discountPrice = "$${svc.basePrice.toInt()}",
                            priceValue = svc.basePrice,
                            isFeatured = svc.category.name == "FULL_WASH" || svc.category.name == "DETAILING",
                            tags = listOf("Doorstep Detail", "Eco Nanomats", "Deionized Rinse")
                        )
                    }

                    val activeBayText = homeDashboardState.defaultAddress?.let {
                        "${it.city}, ${it.state} • ${it.label}"
                    } ?: (homeDashboardState.serviceAreaName ?: "Austin, TX • Mobile Detailing")

                    val homeUiState = HomeUiState(
                        activeBay = activeBayText,
                        availableRigsCount = 4,
                        averageEtaMinutes = 22,
                        vehicles = mappedVehicles,
                        packages = mappedPackages,
                        selectedPackageId = homeDashboardState.selectedServiceId ?: mappedPackages.firstOrNull()?.id ?: ""
                    )

                    HomeScreen(
                        state = homeUiState,
                        onCategorySelect = { category ->
                            // Optional category filter
                        },
                        onVehicleSelect = { vehicleId ->
                            homeViewModel.selectVehicle(vehicleId)
                        },
                        onPackageSelect = { pkgId ->
                            homeViewModel.selectService(pkgId)
                        },
                        onScheduleSlotSelect = { slotId ->
                            // Time slot selection
                        },
                        onProceedToBooking = {
                            val activeVeh = homeDashboardState.vehicles.find { it.id == homeDashboardState.selectedVehicleId }
                                ?: homeDashboardState.vehicles.firstOrNull()
                            val activeSvc = homeDashboardState.services.find { it.id == homeDashboardState.selectedServiceId }
                                ?: homeDashboardState.services.firstOrNull()

                            if (activeVeh != null && activeSvc != null) {
                                bookingViewModel.initCheckout(
                                    vehicle = activeVeh,
                                    service = activeSvc,
                                    defaultAddress = homeDashboardState.defaultAddress
                                )
                                currentDestination = AppDestination.BOOKING
                            } else if (activeVeh == null) {
                                // If user has no vehicles yet, open add vehicle dialog
                                homeViewModel.showAddVehicleDialog(true)
                            }
                        },
                        onProfileClick = { currentDestination = AppDestination.PROFILE },
                        onNotificationsClick = { currentDestination = AppDestination.PROFILE }
                    )
                }

                AppDestination.BOOKING -> {
                    // Map real BookingCheckoutState to BookingPlacementScreen representation
                    val mappedAddons = bookingCheckoutState.availableAddons.map { addon ->
                        AddonUpgrade(
                            id = addon.id,
                            title = addon.name,
                            subtitle = addon.description ?: "Professional vehicle enhancement",
                            price = addon.price,
                            icon = Icons.Filled.WaterDrop,
                            isSelected = bookingCheckoutState.selectedAddonIds.contains(addon.id)
                        )
                    }

                    val selectedVeh = bookingCheckoutState.selectedVehicle
                    val selectedSvc = bookingCheckoutState.selectedService

                    val bookingPlacementUiState = BookingPlacementUiState(
                        address = bookingCheckoutState.addressLine1.ifBlank { "Enter service location" },
                        selectedParkingType = bookingCheckoutState.selectedParkingType,
                        slotNumber = bookingCheckoutState.slotNumber,
                        gateCode = bookingCheckoutState.gateCode,
                        vehicleName = selectedVeh?.let { "${it.make} ${it.model}" } ?: "Vehicle",
                        vehicleSubtext = selectedVeh?.let { "${it.color} • ${it.registrationNumber}" } ?: "",
                        packageName = selectedSvc?.name ?: "Mobile Detailing Spa",
                        packagePrice = selectedSvc?.basePrice ?: 0.0,
                        arrivalTimeText = "Today, Immediate (~25 min)",
                        reservationTimer = "10:00",
                        addons = mappedAddons,
                        isLoading = bookingCheckoutState.isLoading,
                        errorMessage = bookingCheckoutState.errorMessage
                    )

                    BookingPlacementScreen(
                        state = bookingPlacementUiState,
                        onAddressChange = { newAddr ->
                            bookingViewModel.updateAddress(
                                addressLine1 = newAddr,
                                addressLine2 = bookingCheckoutState.addressLine2,
                                city = bookingCheckoutState.city,
                                state = bookingCheckoutState.state,
                                postalCode = bookingCheckoutState.postalCode,
                                country = bookingCheckoutState.country,
                                latitude = bookingCheckoutState.latitude,
                                longitude = bookingCheckoutState.longitude
                            )
                        },
                        onParkingTypeChange = bookingViewModel::updateParkingType,
                        onSlotNumberChange = bookingViewModel::updateSlotNumber,
                        onGateCodeChange = bookingViewModel::updateGateCode,
                        onToggleAddon = bookingViewModel::toggleAddon,
                        onConfirmBooking = {
                            bookingViewModel.bookAndProceedToPayment()
                        },
                        onBackClick = {
                            currentDestination = AppDestination.HOME
                        }
                    )
                }

                AppDestination.TRACKING -> {
                    // Real Tracking Screen backed by TrackingViewModel
                    val orderTrackingUiState = OrderTrackingUiState(
                        orderId = trackingUiState.bookingReference.ifBlank { "BK-PENDING" },
                        statusHeadline = trackingUiState.headlineText,
                        etaMinutes = 15,
                        technician = TechnicianProfile(
                            name = trackingUiState.staffDisplayName ?: "Mobile Detail Team",
                            title = "Certified Detailing Specialist",
                            vanId = trackingUiState.staffCode ?: "Wash-Go Mobile Rig #01"
                        )
                    )

                    OrderTrackingScreen(
                        state = orderTrackingUiState,
                        onCallTechnician = {},
                        onMessageTechnician = {},
                        onEmergencyHold = {
                            if (trackingUiState.isCancellable) {
                                trackingViewModel.cancelBooking("Customer requested cancellation")
                            }
                        },
                        onBackClick = {
                            currentDestination = AppDestination.HOME
                        }
                    )
                }

                AppDestination.PROFILE -> {
                    ProfileScreen(
                        userFullName = ApiClient.getTokenManager().getUserFullName() ?: "Valued Customer",
                        userEmail = ApiClient.getTokenManager().getUserEmail() ?: "user@washgo.com",
                        vehiclesCount = homeDashboardState.vehicles.size,
                        onLogout = {
                            authViewModel.logout()
                            currentDestination = AppDestination.HOME
                        },
                        onBackClick = {
                            currentDestination = AppDestination.HOME
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    userFullName: String,
    userEmail: String,
    vehiclesCount: Int,
    onLogout: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = SurfaceDark,
        topBar = {
            Surface(
                color = SurfaceContainerLowest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Account Profile",
                        color = OnSurfaceWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onBackClick) {
                        Text("Back", color = PrimaryContainerCyan)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Bio Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(userFullName, color = OnSurfaceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(userEmail, color = OnSurfaceVariantSlate, fontSize = 14.sp)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceContainerHighest
                    ) {
                        Text(
                            text = "Verified Customer Account • Active Session",
                            color = PrimaryContainerCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Stats / Account summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("CONNECTED SERVICES", color = PrimaryContainerCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Registered Vehicles", color = OnSurfaceWhite, fontSize = 15.sp)
                        Text("$vehiclesCount vehicle(s)", color = PrimaryContainerCyan, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(color = OutlineVariant.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Payment Integration", color = OnSurfaceWhite, fontSize = 15.sp)
                        Text("Razorpay Active", color = SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(color = OutlineVariant.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Backend Host", color = OnSurfaceWhite, fontSize = 15.sp)
                        Text("127.0.0.1:8080", color = OnSurfaceVariantSlate, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout Action Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed.copy(alpha = 0.2f),
                    contentColor = ErrorRed
                ),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
