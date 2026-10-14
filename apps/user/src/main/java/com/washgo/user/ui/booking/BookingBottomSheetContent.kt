package com.washgo.user.ui.booking

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoDivider
import com.washgo.core.designsystem.theme.RapidoSuccessGreen
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoYellowDark
import com.washgo.core.designsystem.theme.RapidoYellowPrimary
import com.washgo.core.model.VehicleCategory
import com.washgo.user.data.network.BookingMatchPush
import com.washgo.user.data.network.NearbyAgentDto

@Composable
fun BookingBottomSheetContent(
    uiState: BookingUiState,
    selectedCategory: VehicleCategory,
    packages: List<WashPackageItem>,
    selectedPackage: WashPackageItem,
    addOns: List<WashAddOnItem>,
    selectedAddOnIds: Set<String>,
    sortOption: AgentSortOption,
    totalPriceText: String,
    onCategorySelected: (VehicleCategory) -> Unit,
    onPackageSelected: (WashPackageItem) -> Unit,
    onAddOnToggled: (String) -> Unit,
    onSortOptionSelected: (AgentSortOption) -> Unit,
    onAgentSelected: (String) -> Unit,
    onConfirmBooking: (NearbyAgentDto) -> Unit,
    onCancelBooking: () -> Unit,
    onRetrySearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = RapidoSurfaceWhite,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle Bar
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFDDDDDD))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Only show category and package selector if not yet matched
            if (uiState !is BookingUiState.Matched) {
                // Vehicle Category Switcher (Bike vs Car)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryPill(
                        title = "Bike Wash",
                        price = "from ₹149",
                        icon = Icons.AutoMirrored.Filled.DirectionsBike,
                        isSelected = selectedCategory == VehicleCategory.BIKE,
                        onClick = { onCategorySelected(VehicleCategory.BIKE) },
                        modifier = Modifier.weight(1f)
                    )
                    CategoryPill(
                        title = "Car Wash",
                        price = "from ₹299",
                        icon = Icons.Filled.DirectionsCar,
                        isSelected = selectedCategory != VehicleCategory.BIKE,
                        onClick = { onCategorySelected(VehicleCategory.SEDAN) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Wash Packages Horizontal Selector
                Text(
                    text = "Select Wash Package",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RapidoTextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(packages, key = { it.id }) { pkg ->
                        val isSelected = pkg.id == selectedPackage.id
                        val price = if (selectedCategory == VehicleCategory.BIKE) pkg.priceBike else pkg.priceCar

                        Surface(
                            onClick = { onPackageSelected(pkg) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(0xFFFFFDE7) else Color(0xFFF8F9FA),
                            modifier = Modifier
                                .width(180.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) RapidoYellowPrimary else Color(0xFFEEEEEE),
                                    shape = RoundedCornerShape(14.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = pkg.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RapidoTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = RapidoCarbonBlack,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${pkg.durationMinutes} mins • ₹${price.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RapidoCarbonBlack,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = pkg.description,
                                    fontSize = 10.sp,
                                    color = RapidoTextSecondary,
                                    maxLines = 2,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Add-ons Chips
                Text(
                    text = "Optional Add-ons",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RapidoTextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(addOns, key = { it.id }) { addOn ->
                        val isChecked = selectedAddOnIds.contains(addOn.id)
                        FilterChip(
                            selected = isChecked,
                            onClick = { onAddOnToggled(addOn.id) },
                            label = {
                                Text(
                                    text = "${addOn.name} (+₹${addOn.price.toInt()})",
                                    fontSize = 11.sp,
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RapidoYellowPrimary,
                                selectedLabelColor = RapidoCarbonBlack
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = RapidoDivider)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Content based on BookingUiState
            when (uiState) {
                is BookingUiState.Idle -> {
                    IdleStateView()
                }

                is BookingUiState.Loading -> {
                    LoadingStateView()
                }

                is BookingUiState.Empty -> {
                    EmptyStateView(onRetry = onRetrySearch)
                }

                is BookingUiState.Error -> {
                    ErrorStateView(
                        message = uiState.message,
                        onRetry = onRetrySearch
                    )
                }

                is BookingUiState.Success -> {
                    SuccessStateView(
                        state = uiState,
                        sortOption = sortOption,
                        totalPriceText = totalPriceText,
                        onSortOptionSelected = onSortOptionSelected,
                        onAgentSelected = onAgentSelected,
                        onConfirmBooking = onConfirmBooking
                    )
                }

                is BookingUiState.Matched -> {
                    MatchedStateView(
                        match = uiState.match,
                        onCancelBooking = onCancelBooking
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessStateView(
    state: BookingUiState.Success,
    sortOption: AgentSortOption,
    totalPriceText: String,
    onSortOptionSelected: (AgentSortOption) -> Unit,
    onAgentSelected: (String) -> Unit,
    onConfirmBooking: (NearbyAgentDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available Washers Nearby",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = RapidoTextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = RapidoYellowPrimary
            ) {
                Text(
                    text = "${state.agents.size} ready",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RapidoCarbonBlack,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // Sorting Option Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AgentSortOption.entries.forEach { option ->
                val isSelected = option == sortOption
                Surface(
                    onClick = { onSortOptionSelected(option) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) RapidoCarbonBlack else Color(0xFFF1F3F5),
                    modifier = Modifier.height(28.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) RapidoYellowPrimary else RapidoTextSecondary
                        )
                    }
                }
            }
        }

        // LazyColumn of RapidoAgentRowCard
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(state.agents, key = { it.id }) { agent ->
                val isSelected = state.selectedAgentId == agent.id
                val isThisAgentBooking = state.isBookingInProgress && isSelected

                RapidoAgentRowCard(
                    agent = agent,
                    priceText = totalPriceText,
                    isSelected = isSelected,
                    isBookingInProgress = isThisAgentBooking,
                    onSelect = { onAgentSelected(agent.id) },
                    onConfirmBooking = { onConfirmBooking(agent) }
                )
            }
        }
    }
}

@Composable
private fun IdleStateView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.LocationSearching,
            contentDescription = "Search",
            tint = RapidoYellowPrimary,
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pin Vehicle Spot & Select Service",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = RapidoTextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Move the Google Map pin to your vehicle's parking spot, select your wash tier above, then lock location to view real nearby agents.",
            fontSize = 12.sp,
            color = RapidoTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun LoadingStateView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(44.dp),
            color = RapidoYellowPrimary,
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Finding Nearby Washers...",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = RapidoTextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Scanning active agents within 5 km radius via PostGIS spatial engine",
            fontSize = 12.sp,
            color = RapidoTextSecondary
        )
    }
}

@Composable
private fun EmptyStateView(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFF3E0),
            modifier = Modifier.size(52.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "No agents",
                tint = Color(0xFFF57C00),
                modifier = Modifier.padding(14.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "No Washers in Your Area",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = RapidoTextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "All mobile washing agents are currently committed or outside your 5 km radius. Please check back shortly.",
            fontSize = 12.sp,
            color = RapidoTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RapidoCarbonBlack,
                contentColor = RapidoYellowPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Retry",
                tint = RapidoYellowPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Retry Search",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ErrorStateView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFEBEE),
            modifier = Modifier.size(52.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Error",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.padding(14.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Unable to Load Washers",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = RapidoTextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            fontSize = 12.sp,
            color = RapidoTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Retry",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Try Again", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CategoryPill(
    title: String,
    price: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) RapidoYellowPrimary else Color(0xFFF7F8F9),
        shadowElevation = if (isSelected) 3.dp else 0.dp,
        modifier = modifier.height(52.dp)
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
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = RapidoCarbonBlack
                )
                Text(
                    text = price,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) RapidoCarbonBlack else RapidoTextSecondary
                )
            }
        }
    }
}

@Composable
private fun MatchedStateView(
    match: BookingMatchPush,
    onCancelBooking: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // En Route Status Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFF9C4),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = RapidoYellowPrimary,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ElectricBolt,
                        contentDescription = "En route",
                        tint = RapidoCarbonBlack,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Washer Assigned & En Route!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RapidoCarbonBlack
                    )
                    Text(
                        text = "Arriving in ~${match.etaMinutes} mins • ${match.packageTitle} (₹${match.totalPrice.toInt()})",
                        fontSize = 12.sp,
                        color = RapidoTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Washer Profile Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = RapidoSurfaceWhite,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(52.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = RapidoYellowPrimary,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = match.agentName.take(1).uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = RapidoCarbonBlack
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = RapidoSurfaceWhite,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Verified",
                            tint = RapidoSuccessGreen,
                            modifier = Modifier.padding(1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.agentName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RapidoTextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFF57F17),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", match.agentRating),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoCarbonBlack
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Mobile Washer",
                            fontSize = 12.sp,
                            color = RapidoTextSecondary
                        )
                    }
                }

                Surface(
                    onClick = {
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${match.agentPhone}"))
                        context.startActivity(callIntent)
                    },
                    shape = CircleShape,
                    color = RapidoCarbonBlack,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Call Washer",
                        tint = RapidoYellowPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Start OTP Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = RapidoCarbonBlack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YOUR START WASH OTP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFAAAAAA),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = match.startOtp,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RapidoYellowPrimary,
                    letterSpacing = 6.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Share this 4-digit code with washer only when they arrive at your vehicle.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cancel Booking Action
        OutlinedButton(
            onClick = onCancelBooking,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Cancel",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Cancel Booking",
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
