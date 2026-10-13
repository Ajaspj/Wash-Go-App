package com.example.waggoapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waggoapp.ui.theme.*

enum class ParkingType(val label: String, val icon: ImageVector) {
    DRIVEWAY("Driveway", Icons.Filled.Home),
    STREET("Street Lot", Icons.Filled.Commute),
    GARAGE("Garage / Deck", Icons.Filled.Garage)
}

data class AddonUpgrade(
    val id: String,
    val title: String,
    val subtitle: String,
    val price: Double,
    val icon: ImageVector,
    val isSelected: Boolean = false
)

data class BookingPlacementUiState(
    val address: String = "",
    val selectedParkingType: ParkingType = ParkingType.DRIVEWAY,
    val slotNumber: String = "",
    val gateCode: String = "",
    val vehicleName: String = "",
    val vehicleSubtext: String = "",
    val packageName: String = "",
    val packagePrice: Double = 0.0,
    val arrivalTimeText: String = "Immediate / Scheduled",
    val reservationTimer: String = "10:00",
    val addons: List<AddonUpgrade> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Production-ready Booking Placement screen based on the HydroGlide Stitch design.
 */
@Composable
fun BookingPlacementScreen(
    state: BookingPlacementUiState,
    onAddressChange: (String) -> Unit,
    onParkingTypeChange: (ParkingType) -> Unit,
    onSlotNumberChange: (String) -> Unit,
    onGateCodeChange: (String) -> Unit,
    onToggleAddon: (String) -> Unit,
    onConfirmBooking: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalAddons = state.addons.filter { it.isSelected }.sumOf { it.price }
    val totalPrice = state.packagePrice + totalAddons

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceDark,
        topBar = {
            BookingTopBar(
                title = "Checkout Booking",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            BookingBottomBar(
                totalPrice = totalPrice,
                onConfirm = onConfirmBooking,
                isLoading = state.isLoading
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // Live Reservation Lock Pill
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainerCyan)
                        )
                        Text(
                            text = "LIVE RESERVATION LOCK",
                            color = PrimaryContainerCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerHigh
                    ) {
                        Text(
                            text = "Expires in ${state.reservationTimer}",
                            color = SecondaryAqua,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Radar Map Canvas Component
            item {
                InteractiveMapCanvas()
            }

            // Address Search & GPS Auto-Detect
            item {
                AddressLocationSection(
                    address = state.address,
                    onAddressChange = onAddressChange
                )
            }

            // Parking & Spot Details
            item {
                ParkingDetailsSection(
                    selectedType = state.selectedParkingType,
                    slotNumber = state.slotNumber,
                    gateCode = state.gateCode,
                    onParkingTypeChange = onParkingTypeChange,
                    onSlotNumberChange = onSlotNumberChange,
                    onGateCodeChange = onGateCodeChange
                )
            }

            // Confirmed Spec Summary Card
            item {
                OrderSpecSummaryCard(
                    vehicleName = state.vehicleName,
                    vehicleSubtext = state.vehicleSubtext,
                    packageName = state.packageName,
                    packagePrice = state.packagePrice,
                    address = state.address,
                    slotNumber = state.slotNumber,
                    arrivalTime = state.arrivalTimeText
                )
            }

            // Eco Impact Banner
            item {
                EcoImpactBanner()
            }

            // Add-on Enhancements
            item {
                Text(
                    text = "ADD-ON UPGRADES",
                    color = OnSurfaceVariantSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(state.addons, key = { it.id }) { addon ->
                AddonUpgradeCard(
                    addon = addon,
                    onToggle = { onToggleAddon(addon.id) }
                )
            }

            // Price Breakdown Section
            item {
                PriceSummaryCard(
                    packagePrice = state.packagePrice,
                    addonsPrice = totalAddons,
                    totalPrice = totalPrice
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun BookingTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceDark.copy(alpha = 0.95f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainer)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OnSurfaceWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    color = OnSurfaceWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = PrimaryContainerCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun InteractiveMapCanvas() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Road Network Lines
                val roadColor = Color(0xFF1C2B3C)
                drawLine(roadColor, Offset(0f, h * 0.25f), Offset(w, h * 0.25f), strokeWidth = 8f)
                drawLine(roadColor, Offset(0f, h * 0.65f), Offset(w, h * 0.65f), strokeWidth = 10f)
                drawLine(roadColor, Offset(w * 0.3f, 0f), Offset(w * 0.3f, h), strokeWidth = 8f)
                drawLine(roadColor, Offset(w * 0.7f, 0f), Offset(w * 0.7f, h), strokeWidth = 8f)

                // Route vector line in Electric Cyan
                val path = Path().apply {
                    moveTo(w * 0.3f, h * 0.85f)
                    lineTo(w * 0.3f, h * 0.65f)
                    lineTo(w * 0.58f, h * 0.65f)
                    lineTo(w * 0.58f, h * 0.45f)
                }
                drawPath(
                    path = path,
                    color = PrimaryContainerCyan,
                    style = Stroke(
                        width = 5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f), 0f)
                    )
                )

                // Van Beacon (Source)
                drawCircle(
                    color = SecondaryAqua.copy(alpha = 0.3f),
                    radius = 20f,
                    center = Offset(w * 0.3f, h * 0.85f)
                )
                drawCircle(
                    color = PrimaryContainerCyan,
                    radius = 8f,
                    center = Offset(w * 0.3f, h * 0.85f)
                )

                // Doorstep Target Pin (Destination)
                drawCircle(
                    color = PrimaryContainerCyan.copy(alpha = 0.25f),
                    radius = 28f,
                    center = Offset(w * 0.58f, h * 0.45f)
                )
                drawCircle(
                    color = SecondaryAqua.copy(alpha = 0.5f),
                    radius = 16f,
                    center = Offset(w * 0.58f, h * 0.45f)
                )
                drawCircle(
                    color = PrimaryContainerCyan,
                    radius = 7f,
                    center = Offset(w * 0.58f, h * 0.45f)
                )
            }

            // Floating Controls HUD
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = SurfaceContainerHigh.copy(alpha = 0.9f),
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "Recenter",
                        tint = PrimaryContainerCyan,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(6.dp)
                    )
                }
            }

            // Hub perimeter pill
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceContainerLowest.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainerCyan)
                    )
                    Text(
                        text = "HydroGlide Hub #03 Active",
                        color = PrimaryContainerCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressLocationSection(
    address: String,
    onAddressChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = PrimaryContainerCyan
                )
            },
            placeholder = { Text("Enter doorstep address or parking bay...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryContainerCyan,
                unfocusedBorderColor = OutlineVariant,
                focusedTextColor = OnSurfaceWhite,
                unfocusedTextColor = OnSurfaceWhite,
                focusedContainerColor = SurfaceContainerLow,
                unfocusedContainerColor = SurfaceContainerLow
            )
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Radar,
                            contentDescription = null,
                            tint = PrimaryContainerCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Auto-Detect Precision GPS",
                                color = PrimaryCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SurfaceContainerLowest
                            ) {
                                Text(
                                    text = "±1.2m",
                                    color = PrimaryContainerCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Pinned to customer mobile telemetry",
                            color = OnSurfaceVariantSlate,
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = PrimaryContainerCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ParkingDetailsSection(
    selectedType: ParkingType,
    slotNumber: String,
    gateCode: String,
    onParkingTypeChange: (ParkingType) -> Unit,
    onSlotNumberChange: (String) -> Unit,
    onGateCodeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalParking,
                        contentDescription = null,
                        tint = PrimaryContainerCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Parking & Spot Details",
                        color = OnSurfaceWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "STEP 1 OF 2",
                    color = OnSurfaceVariantSlate,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Parking Type Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ParkingType.values().forEach { type ->
                    val isSelected = type == selectedType
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onParkingTypeChange(type) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SurfaceContainerHighest else SurfaceContainer,
                        border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(PrimaryContainerCyan, PrimaryContainerCyan))
                        ) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = type.icon,
                                contentDescription = null,
                                tint = if (isSelected) PrimaryContainerCyan else OnSurfaceVariantSlate,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = type.label,
                                color = if (isSelected) PrimaryCyan else OnSurfaceVariantSlate,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Slot and Gate Code
            OutlinedTextField(
                value = slotNumber,
                onValueChange = onSlotNumberChange,
                label = { Text("Level / Bay / Slot #") },
                leadingIcon = {
                    Icon(Icons.Filled.GridView, contentDescription = null, tint = OnSurfaceVariantSlate)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryContainerCyan,
                    unfocusedBorderColor = OutlineVariant,
                    focusedTextColor = OnSurfaceWhite,
                    unfocusedTextColor = OnSurfaceWhite,
                    focusedContainerColor = SurfaceContainer,
                    unfocusedContainerColor = SurfaceContainer
                )
            )

            OutlinedTextField(
                value = gateCode,
                onValueChange = onGateCodeChange,
                label = { Text("Gate Code / Arrival Instructions") },
                leadingIcon = {
                    Icon(Icons.Filled.VpnKey, contentDescription = null, tint = OnSurfaceVariantSlate)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryContainerCyan,
                    unfocusedBorderColor = OutlineVariant,
                    focusedTextColor = OnSurfaceWhite,
                    unfocusedTextColor = OnSurfaceWhite,
                    focusedContainerColor = SurfaceContainer,
                    unfocusedContainerColor = SurfaceContainer
                )
            )
        }
    }
}

@Composable
private fun OrderSpecSummaryCard(
    vehicleName: String,
    vehicleSubtext: String,
    packageName: String,
    packagePrice: Double,
    address: String,
    slotNumber: String,
    arrivalTime: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "CONFIRMED SPEC",
                        color = PrimaryContainerCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = vehicleName,
                        color = OnSurfaceWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = vehicleSubtext,
                        color = OnSurfaceVariantSlate,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        tint = PrimaryContainerCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.4f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Package", color = OnSurfaceVariantSlate, fontSize = 12.sp)
                    Text(text = "$packageName ($${packagePrice.toInt()})", color = OnSurfaceWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Dispatch Target", color = OnSurfaceVariantSlate, fontSize = 12.sp)
                    Text(text = slotNumber, color = OnSurfaceWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Estimated Arrival", color = OnSurfaceVariantSlate, fontSize = 12.sp)
                    Text(text = arrivalTime, color = PrimaryContainerCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EcoImpactBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainerCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = null,
                    tint = PrimaryContainerCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "HYDROECO METRIC",
                        color = PrimaryContainerCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SurfaceContainerLowest
                    ) {
                        Text(
                            text = "-48 GAL",
                            color = SecondaryAqua,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = "You're saving 48 Gallons of municipal water via closed-loop vacuum wash.",
                    color = OnSurfaceWhite,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun AddonUpgradeCard(
    addon: AddonUpgrade,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        color = if (addon.isSelected) SurfaceContainerHigh else SurfaceContainer,
        border = if (addon.isSelected) ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(listOf(PrimaryContainerCyan.copy(alpha = 0.6f), PrimaryContainerCyan.copy(alpha = 0.6f)))
        ) else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = addon.icon,
                        contentDescription = null,
                        tint = if (addon.isSelected) PrimaryContainerCyan else OnSurfaceVariantSlate,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = addon.title,
                        color = OnSurfaceWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = addon.subtitle,
                        color = OnSurfaceVariantSlate,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "+$${addon.price.toInt()}.00",
                    color = if (addon.isSelected) PrimaryCyan else OnSurfaceVariantSlate,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (addon.isSelected) PrimaryContainerCyan else SurfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (addon.isSelected) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = null,
                        tint = if (addon.isSelected) SurfaceContainerLowest else OnSurfaceVariantSlate,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceSummaryCard(
    packagePrice: Double,
    addonsPrice: Double,
    totalPrice: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "PRICE BREAKDOWN",
                color = OnSurfaceVariantSlate,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Service Package", color = OnSurfaceVariantSlate, fontSize = 13.sp)
                Text(text = "$${packagePrice.toInt()}.00", color = OnSurfaceWhite, fontSize = 13.sp)
            }

            if (addonsPrice > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Selected Add-ons", color = OnSurfaceVariantSlate, fontSize = 13.sp)
                    Text(text = "+$${addonsPrice.toInt()}.00", color = PrimaryCyan, fontSize = 13.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Eco Environmental Compliance", color = OnSurfaceVariantSlate, fontSize = 13.sp)
                Text(text = "$0.00 (Exempt)", color = SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total Investment", color = OnSurfaceWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = "$${totalPrice.toInt()}.00", color = PrimaryCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BookingBottomBar(
    totalPrice: Double,
    onConfirm: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceContainer.copy(alpha = 0.98f),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TOTAL DUE",
                    color = OnSurfaceVariantSlate,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$${totalPrice.toInt()}.00",
                    color = PrimaryCyan,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                modifier = Modifier
                    .height(50.dp)
                    .shadow(12.dp, RoundedCornerShape(25.dp), spotColor = PrimaryContainerCyan),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryContainerCyan,
                    contentColor = SurfaceContainerLowest
                ),
                contentPadding = PaddingValues(horizontal = 28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SurfaceContainerLowest,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Book Wash & Dispatch",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF051424)
@Composable
fun BookingPlacementScreenPreview() {
    HydroGlideTheme {
        BookingPlacementScreen(
            state = BookingPlacementUiState(),
            onAddressChange = {},
            onParkingTypeChange = {},
            onSlotNumberChange = {},
            onGateCodeChange = {},
            onToggleAddon = {},
            onConfirmBooking = {},
            onBackClick = {}
        )
    }
}
