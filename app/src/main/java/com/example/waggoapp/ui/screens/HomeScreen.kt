package com.example.waggoapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waggoapp.ui.theme.*

enum class VehicleCategory(val label: String, val icon: ImageVector) {
    FOUR_WHEELER("4-Wheelers", Icons.Filled.DirectionsCar),
    TWO_WHEELER("2-Wheelers", Icons.Filled.TwoWheeler)
}

data class VehicleProfile(
    val id: String,
    val type: String,
    val model: String,
    val color: String,
    val category: VehicleCategory,
    val isSelected: Boolean = false
)

data class WashPackageItem(
    val id: String,
    val title: String,
    val badge: String?,
    val durationText: String,
    val originalPrice: String,
    val discountPrice: String,
    val priceValue: Double,
    val isFeatured: Boolean = false,
    val tags: List<String>,
    val recommendationText: String? = null
)

data class ScheduleSlot(
    val id: String,
    val title: String,
    val isAsap: Boolean = false
)

data class HomeUiState(
    val activeBay: String = "Locating Service Area...",
    val availableRigsCount: Int = 0,
    val averageEtaMinutes: Int = 0,
    val selectedCategory: VehicleCategory = VehicleCategory.FOUR_WHEELER,
    val vehicles: List<VehicleProfile> = emptyList(),
    val packages: List<WashPackageItem> = emptyList(),
    val selectedPackageId: String = "",
    val scheduleSlots: List<ScheduleSlot> = listOf(
        ScheduleSlot("s1", "Immediate / ASAP", isAsap = true),
        ScheduleSlot("s2", "Today Afternoon"),
        ScheduleSlot("s3", "Tomorrow Morning")
    ),
    val selectedScheduleSlotId: String = "s1"
)

/**
 * Production-ready Home Dashboard screen based on the HydroGlide Stitch design.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onCategorySelect: (VehicleCategory) -> Unit,
    onVehicleSelect: (String) -> Unit,
    onPackageSelect: (String) -> Unit,
    onScheduleSlotSelect: (String) -> Unit,
    onProceedToBooking: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onManageGarageClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedVehicle = state.vehicles.find { it.isSelected } ?: state.vehicles.firstOrNull()
    val selectedPkg = state.packages.find { it.id == state.selectedPackageId } ?: state.packages.firstOrNull()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceDark,
        topBar = {
            HomeTopBar(
                activeBay = state.activeBay,
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            HomeBottomStickyBar(
                selectedPackage = selectedPkg,
                onProceed = onProceedToBooking
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Live Telemetry Status Banner
            item {
                TelemetryStatusBanner(
                    rigsCount = state.availableRigsCount,
                    avgEtaMinutes = state.averageEtaMinutes
                )
            }

            // Vehicle Category Selector
            item {
                VehicleCategorySection(
                    selectedCategory = state.selectedCategory,
                    onCategorySelect = onCategorySelect,
                    onManageGarage = onManageGarageClick
                )
            }

            // Filtered Vehicles Grid/Row
            item {
                val filteredVehicles = state.vehicles.filter { it.category == state.selectedCategory }
                if (filteredVehicles.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onManageGarageClick() },
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceContainerLowest,
                        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DirectionsCar,
                                contentDescription = null,
                                tint = PrimaryCyan,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "No registered vehicles found",
                                color = OnSurfaceWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tap to add your car/bike to your garage",
                                color = OnSurfaceVariantSlate,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        filteredVehicles.forEach { vehicle ->
                            VehicleCard(
                                vehicle = vehicle,
                                onSelect = { onVehicleSelect(vehicle.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Self-Sustaining Unit Guarantee Banner
            item {
                SelfSustainingVanBanner()
            }

            // Doorstep Care Packages Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "DOORSTEP CARE PACKAGES",
                            color = PrimaryContainerCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Engineered Clean",
                            color = OnSurfaceWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Includes ${selectedVehicle?.type ?: "4-Wheel"} Rate",
                        color = OnSurfaceVariantSlate,
                        fontSize = 12.sp
                    )
                }
            }

            // Packages List
            if (state.packages.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PrimaryCyan,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Loading detailing packages from catalog...",
                                color = OnSurfaceVariantSlate,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(state.packages, key = { it.id }) { pkg ->
                    WashPackageCard(
                        pkg = pkg,
                        isSelected = pkg.id == state.selectedPackageId,
                        onSelect = { onPackageSelect(pkg.id) }
                    )
                }
            }

            // Timing Selector
            item {
                ScheduleTimingSection(
                    slots = state.scheduleSlots,
                    selectedSlotId = state.selectedScheduleSlotId,
                    onSlotSelect = onScheduleSlotSelect
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun HomeTopBar(
    activeBay: String,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
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
            // Brand Logo & Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = null,
                        tint = PrimaryContainerCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "HydroGlide",
                            color = PrimaryCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainerCyan)
                        )
                    }
                    Text(
                        text = "DOORSTEP CAR WASH",
                        color = OnSurfaceVariantSlate,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Bay Pill & Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceContainerHigh,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(OutlineVariant, OutlineVariant))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NearMe,
                            contentDescription = null,
                            tint = PrimaryContainerCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = activeBay,
                            color = OnSurfaceWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainer)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = OnSurfaceVariantSlate,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, PrimaryContainerCyan.copy(alpha = 0.5f), CircleShape)
                        .background(SurfaceContainerHighest)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = PrimaryContainerCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryStatusBanner(
    rigsCount: Int,
    avgEtaMinutes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(PrimaryContainerCyan.copy(alpha = 0.4f), Color.Transparent)
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalShipping,
                            contentDescription = null,
                            tint = PrimaryContainerCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ACTIVE FLEET AUSTIN",
                                color = PrimaryContainerCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(OnSurfaceVariantSlate)
                            )
                            Text(
                                text = "Live",
                                color = OnSurfaceVariantSlate,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "$rigsCount Mobile Rigs Ready",
                            color = OnSurfaceWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceContainerLowest
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "AVG. ETA",
                            color = OnSurfaceVariantSlate,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$avgEtaMinutes mins",
                            color = PrimaryCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = SurfaceContainer.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = SecondaryAqua,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Onboard 100% solar power & deionized tank",
                            color = OnSurfaceVariantSlate,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "Ready now",
                        color = PrimaryContainerCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleCategorySection(
    selectedCategory: VehicleCategory,
    onCategorySelect: (VehicleCategory) -> Unit,
    onManageGarage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VEHICLE PROFILE",
                color = OnSurfaceVariantSlate,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier.clickable { onManageGarage() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manage Garage",
                    color = PrimaryContainerCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = PrimaryContainerCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceContainerLow)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            VehicleCategory.values().forEach { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) SurfaceContainerHigh else Color.Transparent)
                        .then(
                            if (isSelected) Modifier.border(
                                1.dp,
                                PrimaryContainerCyan.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            ) else Modifier
                        )
                        .clickable { onCategorySelect(category) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = if (isSelected) PrimaryContainerCyan else OnSurfaceVariantSlate,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = category.label,
                            color = if (isSelected) PrimaryContainerCyan else OnSurfaceVariantSlate,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: VehicleProfile,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onSelect() }
            .then(
                if (vehicle.isSelected) {
                    Modifier.border(
                        1.5.dp,
                        PrimaryContainerCyan,
                        RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (vehicle.isSelected) SurfaceContainerHigh else SurfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (vehicle.isSelected) PrimaryContainerCyan else SurfaceContainerHighest
                ) {
                    Text(
                        text = if (vehicle.isSelected) "ACTIVE" else "SAVED",
                        color = if (vehicle.isSelected) SurfaceContainerLowest else OnSurfaceVariantSlate,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (vehicle.isSelected) PrimaryContainerCyan else SurfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (vehicle.isSelected) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = null,
                        tint = if (vehicle.isSelected) SurfaceContainerLowest else OnSurfaceVariantSlate,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (vehicle.category == VehicleCategory.FOUR_WHEELER) Icons.Filled.DirectionsCar else Icons.Filled.TwoWheeler,
                    contentDescription = null,
                    tint = if (vehicle.isSelected) PrimaryContainerCyan else OnSurfaceVariantSlate.copy(alpha = 0.6f),
                    modifier = Modifier.size(36.dp)
                )
            }

            Column {
                Text(
                    text = vehicle.type,
                    color = if (vehicle.isSelected) PrimaryCyan else OnSurfaceWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${vehicle.model} • ${vehicle.color}",
                    color = OnSurfaceVariantSlate,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SelfSustainingVanBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceContainerLow,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(listOf(OutlineVariant.copy(alpha = 0.4f), Color.Transparent))
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SecondaryAqua.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    tint = PrimaryContainerCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = "Mobile detail setup",
                    color = OnSurfaceWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "We arrive fully equipped with filtered water, power, and premium detailing tools—no driveway setup needed.",
                    color = OnSurfaceVariantSlate,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun WashPackageCard(
    pkg: WashPackageItem,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .then(
                if (isSelected) {
                    Modifier.border(1.5.dp, PrimaryContainerCyan, RoundedCornerShape(18.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceContainerHigh else SurfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Title & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = pkg.title,
                            color = if (isSelected) PrimaryCyan else OnSurfaceWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (pkg.badge != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (pkg.isFeatured) PrimaryContainerCyan else SurfaceContainerHighest
                            ) {
                                Text(
                                    text = pkg.badge,
                                    color = if (pkg.isFeatured) SurfaceContainerLowest else SecondaryAqua,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = pkg.durationText,
                        color = OnSurfaceVariantSlate,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = pkg.discountPrice,
                        color = PrimaryCyan,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pkg.originalPrice,
                        color = OnSurfaceVariantSlate,
                        fontSize = 12.sp,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }

            // Feature Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                pkg.tags.take(3).forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SurfaceContainerLowest
                    ) {
                        Text(
                            text = tag,
                            color = PrimaryCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Recommendation or Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pkg.recommendationText != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TaskAlt,
                            contentDescription = null,
                            tint = PrimaryContainerCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = pkg.recommendationText,
                            color = PrimaryContainerCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(
                        text = "Inspect 7-Point Checklist",
                        color = OnSurfaceVariantSlate,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onSelect,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) PrimaryContainerCyan else SurfaceContainerHighest,
                        contentColor = if (isSelected) SurfaceContainerLowest else PrimaryCyan
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isSelected) "Selected" else "Select Tier",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleTimingSection(
    slots: List<ScheduleSlot>,
    selectedSlotId: String,
    onSlotSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
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
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = PrimaryContainerCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Service Schedule",
                        color = OnSurfaceWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "Today Available",
                    color = PrimaryContainerCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                slots.forEach { slot ->
                    val isSelected = slot.id == selectedSlotId
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSlotSelect(slot.id) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SurfaceContainerHigh else SurfaceContainerLow,
                        border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(PrimaryContainerCyan, PrimaryContainerCyan))
                        ) else null
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = slot.title,
                                color = if (isSelected) PrimaryContainerCyan else OnSurfaceVariantSlate,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeBottomStickyBar(
    selectedPackage: WashPackageItem?,
    onProceed: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceContainer.copy(alpha = 0.98f),
        shadowElevation = 16.dp,
        border = BorderStroke(
            1.dp,
            Brush.verticalGradient(
                listOf(OutlineVariant.copy(alpha = 0.3f), Color.Transparent)
            )
        )
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
                    text = "TOTAL DISPATCH",
                    color = OnSurfaceVariantSlate,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = selectedPackage?.discountPrice ?: "--",
                        color = PrimaryCyan,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedPackage?.title ?: "Select service",
                        color = OnSurfaceVariantSlate,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Button(
                onClick = onProceed,
                enabled = selectedPackage != null,
                modifier = Modifier
                    .height(48.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = PrimaryContainerCyan),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryContainerCyan,
                    contentColor = SurfaceContainerLowest
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Instant Dispatch",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF051424)
@Composable
fun HomeScreenPreview() {
    HydroGlideTheme {
        HomeScreen(
            state = HomeUiState(),
            onCategorySelect = {},
            onVehicleSelect = {},
            onPackageSelect = {},
            onScheduleSlotSelect = {},
            onProceedToBooking = {}
        )
    }
}
