package com.washgo.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.core.designsystem.component.RapidoAgentBottomSheet
import com.washgo.core.designsystem.component.RapidoStatusPill
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoSuccessGreen
import com.washgo.core.designsystem.theme.RapidoSurfaceLight
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoTheme
import com.washgo.core.designsystem.theme.RapidoYellowPrimary
import com.washgo.core.model.AgentCandidate
import com.washgo.core.model.VehicleCategory
import com.washgo.core.model.VehicleSpecialty
import com.washgo.user.ui.booking.BookingScreen
import kotlinx.coroutines.launch

class UserMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RapidoTheme {
                BookingScreen()
            }
        }
    }
}

@Composable
fun UserHomeScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(VehicleCategory.SEDAN) }
    var pinnedAddress by remember { mutableStateOf("12th Main Road, Indiranagar, Bengaluru") }

    // Mock candidates (as returned by PostGIS discovery query)
    val candidateAgents = remember(selectedCategory) {
        listOf(
            AgentCandidate(
                agentId = "agt-1",
                displayName = "Ramesh Kumar",
                phone = "+91 98765 43210",
                currentLatitude = 12.9716,
                currentLongitude = 77.5946,
                averageRating = 4.9,
                totalCompletedWashes = 142,
                vehicleSpecialty = if (selectedCategory == VehicleCategory.BIKE) VehicleSpecialty.BIKE_ONLY else VehicleSpecialty.BOTH,
                distanceMeters = 850.0,
                estimatedEtaMinutes = 5
            ),
            AgentCandidate(
                agentId = "agt-2",
                displayName = "Suresh Patel",
                phone = "+91 98765 43211",
                currentLatitude = 12.9750,
                currentLongitude = 77.5980,
                averageRating = 4.8,
                totalCompletedWashes = 98,
                vehicleSpecialty = VehicleSpecialty.BOTH,
                distanceMeters = 1400.0,
                estimatedEtaMinutes = 7
            ),
            AgentCandidate(
                agentId = "agt-3",
                displayName = "Anand Rao",
                phone = "+91 98765 43212",
                currentLatitude = 12.9690,
                currentLongitude = 77.5910,
                averageRating = 4.7,
                totalCompletedWashes = 65,
                vehicleSpecialty = VehicleSpecialty.CAR_ONLY,
                distanceMeters = 2100.0,
                estimatedEtaMinutes = 10
            )
        )
    }

    var selectedAgent by remember { mutableStateOf<AgentCandidate?>(candidateAgents.firstOrNull()) }
    var isBookingPending by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map Canvas Simulation Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE8ECEF))
            ) {
                // Center Pin Icon
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Vehicle Pin",
                        tint = RapidoCarbonBlack,
                        modifier = Modifier.size(44.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RapidoCarbonBlack,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "Wash Here",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoYellowPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }

                // Locate Me Button
                IconButton(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("GPS Location refreshed to current coordinates")
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp, bottom = 120.dp)
                        .background(RapidoSurfaceWhite, CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "My Location",
                        tint = RapidoCarbonBlack
                    )
                }
            }

            // Top Header: Address & Vehicle Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Address Bar Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Location",
                            tint = RapidoYellowPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Service Location",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RapidoTextSecondary
                            )
                            Text(
                                text = pinnedAddress,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RapidoTextPrimary,
                                maxLines = 1
                            )
                        }
                        RapidoStatusPill(
                            text = "LIVE",
                            dotColor = RapidoSuccessGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Vehicle Category Toggle (Bike vs Car)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VehicleCategoryTab(
                        title = "Bike / Two Wheeler",
                        isSelected = selectedCategory == VehicleCategory.BIKE,
                        icon = Icons.AutoMirrored.Filled.DirectionsBike,
                        onSelect = {
                            selectedCategory = VehicleCategory.BIKE
                            selectedAgent = candidateAgents.firstOrNull()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    VehicleCategoryTab(
                        title = "Car / Four Wheeler",
                        isSelected = selectedCategory != VehicleCategory.BIKE,
                        icon = Icons.Filled.DirectionsCar,
                        onSelect = {
                            selectedCategory = VehicleCategory.SEDAN
                            selectedAgent = candidateAgents.firstOrNull()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Bottom Sheet with Candidate Agents
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                RapidoAgentBottomSheet(
                    agents = candidateAgents,
                    selectedAgent = selectedAgent,
                    servicePriceText = if (selectedCategory == VehicleCategory.BIKE) "₹149" else "₹299",
                    onAgentSelected = { agent ->
                        selectedAgent = agent
                    },
                    onConfirmBooking = { agent ->
                        isBookingPending = true
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Booking broadcasted to ${agent.displayName}! Offer valid for 20s."
                            )
                            isBookingPending = false
                        }
                    },
                    isLoading = isBookingPending
                )
            }
        }
    }
}

@Composable
fun VehicleCategoryTab(
    title: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) RapidoYellowPrimary else RapidoSurfaceWhite,
        shadowElevation = 3.dp,
        modifier = modifier.height(46.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = RapidoCarbonBlack,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = RapidoCarbonBlack
            )
        }
    }
}
