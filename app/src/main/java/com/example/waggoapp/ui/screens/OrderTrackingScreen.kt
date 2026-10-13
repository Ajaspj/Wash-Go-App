package com.example.waggoapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waggoapp.ui.theme.*

enum class WashStepStatus {
    COMPLETED,
    IN_PROGRESS,
    UPCOMING
}

data class TrackingStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val status: WashStepStatus,
    val timestamp: String? = null
)

data class TechnicianProfile(
    val name: String = "Mobile Detail Team",
    val title: String = "Certified Detailing Specialist",
    val rating: Double = 5.0,
    val completedWashesCount: Int = 0,
    val vanId: String = "Eco Rig #01",
    val licensePlate: String = "Wash-Go Unit",
    val equipmentNote: String = "EcoSteamer Pro • Zero-Runoff"
)

data class OrderTrackingUiState(
    val orderId: String = "",
    val statusHeadline: String = "Order Status Pending",
    val etaMinutes: Int = 15,
    val distanceMiles: Double = 0.0,
    val speedMph: Int = 0,
    val technician: TechnicianProfile = TechnicianProfile(),
    val currentBay: String = "Customer Bay",
    val waterSavedGallons: Int = 45,
    val steps: List<TrackingStep> = listOf(
        TrackingStep(1, "Booking Confirmed", "Mobile rig scheduled and verified", WashStepStatus.COMPLETED),
        TrackingStep(2, "Partner Assigned", "Specialist allocated to job", WashStepStatus.IN_PROGRESS),
        TrackingStep(3, "Partner En Route", "Approaching destination", WashStepStatus.UPCOMING),
        TrackingStep(4, "Arrival & Setup", "Eco equipment positioned on-site", WashStepStatus.UPCOMING),
        TrackingStep(5, "Wash & Detailing", "Comprehensive cleaning process", WashStepStatus.UPCOMING),
        TrackingStep(6, "Completion & Handover", "Quality inspection and sign-off", WashStepStatus.UPCOMING)
    )
)

/**
 * Production-ready Real-Time Order Tracking screen based on the HydroGlide Stitch design.
 */
@Composable
fun OrderTrackingScreen(
    state: OrderTrackingUiState,
    onCallTechnician: () -> Unit,
    onMessageTechnician: () -> Unit,
    onEmergencyHold: () -> Unit,
    onBackClick: () -> Unit,
    onSupportClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceDark,
        topBar = {
            TrackingTopBar(
                orderId = state.orderId,
                onBackClick = onBackClick
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

            // Live Telemetry Banner
            item {
                TelemetryLiveBanner(
                    headline = state.statusHeadline,
                    etaMinutes = state.etaMinutes
                )
            }

            // Real-Time Animated Map Canvas
            item {
                LiveTrackingMap(
                    vanId = state.technician.vanId,
                    licensePlate = state.technician.licensePlate,
                    distanceMiles = state.distanceMiles,
                    speedMph = state.speedMph,
                    bay = state.currentBay
                )
            }

            // Technician Profile Card
            item {
                TechnicianCard(
                    tech = state.technician,
                    onCall = onCallTechnician,
                    onMessage = onMessageTechnician,
                    onHold = onEmergencyHold
                )
            }

            // Multi-Stage Progress Stepper
            item {
                Text(
                    text = "SERVICE PROGRESSION",
                    color = OnSurfaceVariantSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        state.steps.forEachIndexed { index, step ->
                            StepProgressItem(
                                step = step,
                                isLast = index == state.steps.lastIndex
                            )
                        }
                    }
                }
            }

            // Eco Savings Live Metric Card
            item {
                EcoSavingsCard(gallonsSaved = state.waterSavedGallons)
            }

            // Support & Help Trigger
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSupportClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HelpOutline,
                                contentDescription = null,
                                tint = OnSurfaceVariantSlate,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Need help with your appointment?",
                                color = OnSurfaceWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = OnSurfaceVariantSlate,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun TrackingTopBar(
    orderId: String,
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
                Column {
                    Text(
                        text = "Live Wash Tracker",
                        color = OnSurfaceWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = orderId,
                        color = OnSurfaceVariantSlate,
                        fontSize = 11.sp
                    )
                }
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
private fun TelemetryLiveBanner(
    headline: String,
    etaMinutes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainerCyan)
                )
                Text(
                    text = headline.uppercase(),
                    color = OnSurfaceWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceContainerLowest
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = PrimaryContainerCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$etaMinutes mins",
                        color = PrimaryCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveTrackingMap(
    vanId: String,
    licensePlate: String,
    distanceMiles: Double,
    speedMph: Int,
    bay: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Jetpack Compose Canvas Drawing simulated city map
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // City blocks
                val blockColor = Color(0xFF0D1C2D)
                drawRect(blockColor, topLeft = Offset(w * 0.05f, h * 0.1f), size = Size(w * 0.22f, h * 0.35f))
                drawRect(blockColor, topLeft = Offset(w * 0.35f, h * 0.1f), size = Size(w * 0.35f, h * 0.3f))
                drawRect(blockColor, topLeft = Offset(w * 0.78f, h * 0.08f), size = Size(w * 0.18f, h * 0.45f))
                drawRect(blockColor, topLeft = Offset(w * 0.08f, h * 0.65f), size = Size(w * 0.32f, h * 0.28f))
                drawRect(blockColor, topLeft = Offset(w * 0.48f, h * 0.6f), size = Size(w * 0.45f, h * 0.32f))

                // Arterial Roadways
                val roadColor = Color(0xFF162536)
                drawLine(roadColor, Offset(0f, h * 0.52f), Offset(w, h * 0.52f), strokeWidth = 24f)
                drawLine(roadColor, Offset(w * 0.32f, 0f), Offset(w * 0.32f, h), strokeWidth = 18f)
                drawLine(roadColor, Offset(w * 0.74f, 0f), Offset(w * 0.74f, h), strokeWidth = 20f)

                // Neon cyan route path
                val routePath = Path().apply {
                    moveTo(w * 0.15f, h * 0.3f)
                    lineTo(w * 0.32f, h * 0.3f)
                    lineTo(w * 0.32f, h * 0.52f)
                    lineTo(w * 0.74f, h * 0.52f)
                    lineTo(w * 0.74f, h * 0.75f)
                }

                // Ambient route glow
                drawPath(
                    path = routePath,
                    color = PrimaryContainerCyan.copy(alpha = 0.35f),
                    style = Stroke(width = 12f)
                )

                // Dashed active route line
                drawPath(
                    path = routePath,
                    color = PrimaryContainerCyan,
                    style = Stroke(
                        width = 4.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                    )
                )

                // Van Beacon (moving marker)
                drawCircle(
                    color = PrimaryContainerCyan.copy(alpha = 0.25f),
                    radius = 24f,
                    center = Offset(w * 0.15f, h * 0.3f)
                )
                drawCircle(
                    color = PrimaryContainerCyan,
                    radius = 12f,
                    center = Offset(w * 0.15f, h * 0.3f)
                )
                drawCircle(
                    color = SurfaceContainerLowest,
                    radius = 5f,
                    center = Offset(w * 0.15f, h * 0.3f)
                )

                // Destination Car Target Pin
                drawCircle(
                    color = SecondaryAqua.copy(alpha = 0.2f),
                    radius = 32f,
                    center = Offset(w * 0.74f, h * 0.75f)
                )
                drawCircle(
                    color = SecondaryAqua.copy(alpha = 0.4f),
                    radius = 18f,
                    center = Offset(w * 0.74f, h * 0.75f)
                )
                drawCircle(
                    color = PrimaryContainerCyan,
                    radius = 8f,
                    center = Offset(w * 0.74f, h * 0.75f)
                )
            }

            // Telemetry overlay pill (top)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                shape = RoundedCornerShape(10.dp),
                color = SurfaceContainerLow.copy(alpha = 0.92f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ElectricBolt,
                        contentDescription = null,
                        tint = PrimaryContainerCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = vanId,
                            color = OnSurfaceWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lic: $licensePlate",
                            color = OnSurfaceVariantSlate,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Bay target label (near destination)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 48.dp),
                shape = RoundedCornerShape(8.dp),
                color = SurfaceContainerHigh
            ) {
                Text(
                    text = bay,
                    color = OnSurfaceWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Speed & Distance badge (bottom left)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                shape = RoundedCornerShape(8.dp),
                color = SurfaceContainerLow.copy(alpha = 0.92f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainerCyan)
                    )
                    Text(
                        text = "$distanceMiles mi away • $speedMph mph avg",
                        color = OnSurfaceWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TechnicianCard(
    tech: TechnicianProfile,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onHold: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHighest)
                        .border(1.5.dp, PrimaryContainerCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = PrimaryContainerCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tech.name,
                            color = OnSurfaceWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = PrimaryContainerCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = tech.rating.toString(),
                                    color = OnSurfaceWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "${tech.title} • ${tech.completedWashesCount}+ doorstep washes",
                        color = OnSurfaceVariantSlate,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Text(
                            text = tech.equipmentNote,
                            color = PrimaryContainerCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceContainerHigh,
                        contentColor = OnSurfaceWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = null,
                        tint = PrimaryContainerCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Call Tech", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onMessage,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceContainerHigh,
                        contentColor = OnSurfaceWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Chat,
                        contentDescription = null,
                        tint = PrimaryContainerCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Message Bay", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onHold,
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ErrorContainerRed.copy(alpha = 0.2f),
                        contentColor = ErrorRed
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(ErrorRed.copy(alpha = 0.4f), ErrorRed.copy(alpha = 0.4f)))
                    )
                ) {
                    Text(text = "Pause service", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StepProgressItem(
    step: TrackingStep,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when (step.status) {
                            WashStepStatus.COMPLETED -> PrimaryContainerCyan
                            WashStepStatus.IN_PROGRESS -> SurfaceContainerHighest
                            WashStepStatus.UPCOMING -> SurfaceContainerLow
                        }
                    )
                    .then(
                        if (step.status == WashStepStatus.IN_PROGRESS) {
                            Modifier.border(2.dp, PrimaryContainerCyan, CircleShape)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (step.status) {
                    WashStepStatus.COMPLETED -> Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = SurfaceContainerLowest,
                        modifier = Modifier.size(14.dp)
                    )
                    WashStepStatus.IN_PROGRESS -> Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainerCyan)
                    )
                    WashStepStatus.UPCOMING -> Text(
                        text = step.stepNumber.toString(),
                        color = OnSurfaceVariantSlate,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(
                            if (step.status == WashStepStatus.COMPLETED) PrimaryContainerCyan.copy(alpha = 0.7f)
                            else OutlineVariant.copy(alpha = 0.4f)
                        )
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = step.title,
                    color = if (step.status == WashStepStatus.UPCOMING) OnSurfaceVariantSlate else OnSurfaceWhite,
                    fontSize = 14.sp,
                    fontWeight = if (step.status == WashStepStatus.IN_PROGRESS) FontWeight.Bold else FontWeight.SemiBold
                )
                if (step.timestamp != null) {
                    Text(
                        text = step.timestamp,
                        color = if (step.status == WashStepStatus.IN_PROGRESS) PrimaryContainerCyan else OnSurfaceVariantSlate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = step.description,
                color = OnSurfaceVariantSlate,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun EcoSavingsCard(gallonsSaved: Int) {
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainerCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = PrimaryContainerCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = "$gallonsSaved Gallons Water Saved",
                    color = PrimaryCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "This doorstep booking prevents runoff pollutants and municipal reservoir depletion.",
                    color = OnSurfaceVariantSlate,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF051424)
@Composable
fun OrderTrackingScreenPreview() {
    HydroGlideTheme {
        OrderTrackingScreen(
            state = OrderTrackingUiState(),
            onCallTechnician = {},
            onMessageTechnician = {},
            onEmergencyHold = {},
            onBackClick = {}
        )
    }
}
