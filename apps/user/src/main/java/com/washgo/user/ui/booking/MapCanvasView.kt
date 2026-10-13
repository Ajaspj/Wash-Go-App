package com.washgo.user.ui.booking

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoSuccessGreen
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoYellowPrimary
import kotlin.math.roundToInt

@Composable
fun MapCanvasView(
    locationState: LocationState,
    onPinMoved: (Double, Double) -> Unit,
    onPinDragStateChanged: (Boolean) -> Unit,
    onSnapToGps: () -> Unit,
    onLockLocation: () -> Unit,
    onUnlockLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing animation for the user's GPS signal beacon
    val infiniteTransition = rememberInfiniteTransition(label = "GpsBeaconPulse")
    val beaconPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beaconPulseScale"
    )
    val beaconPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beaconPulseAlpha"
    )

    // Lift animation for the pin when dragging
    val pinElevationOffset by animateDpAsState(
        targetValue = if (locationState.isDragging) (-24).dp else 0.dp,
        animationSpec = tween(180),
        label = "pinElevationOffset"
    )
    val shadowScale by animateDpAsState(
        targetValue = if (locationState.isDragging) 18.dp else 10.dp,
        animationSpec = tween(180),
        label = "shadowScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(locationState.isLocked) {
                if (!locationState.isLocked) {
                    detectDragGestures(
                        onDragStart = {
                            onPinDragStateChanged(true)
                        },
                        onDragEnd = {
                            onPinDragStateChanged(false)
                        },
                        onDragCancel = {
                            onPinDragStateChanged(false)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            panOffsetX += dragAmount.x
                            panOffsetY += dragAmount.y

                            // Convert pixel deltas into simulated GPS degree adjustments
                            val latDelta = -dragAmount.y * 0.00004
                            val lngDelta = dragAmount.x * 0.00004
                            val newLat = locationState.latitude + latDelta
                            val newLng = locationState.longitude + lngDelta
                            onPinMoved(newLat, newLng)
                        }
                    )
                }
            }
    ) {
        // 1. Stylized Google Map Background Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Base ground color
            drawRect(color = Color(0xFFF1F3F4))

            // Green park blocks
            drawRoundRect(
                color = Color(0xFFD6E9D5),
                topLeft = Offset(canvasWidth * 0.05f + (panOffsetX * 0.4f), canvasHeight * 0.15f + (panOffsetY * 0.4f)),
                size = Size(180.dp.toPx(), 220.dp.toPx()),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFFD6E9D5),
                topLeft = Offset(canvasWidth * 0.65f + (panOffsetX * 0.4f), canvasHeight * 0.60f + (panOffsetY * 0.4f)),
                size = Size(140.dp.toPx(), 160.dp.toPx()),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )

            // Water body / lake
            val waterPath = Path().apply {
                moveTo(canvasWidth * 0.75f + (panOffsetX * 0.3f), 0f)
                cubicTo(
                    canvasWidth * 0.70f + (panOffsetX * 0.3f), canvasHeight * 0.18f + (panOffsetY * 0.3f),
                    canvasWidth * 0.95f + (panOffsetX * 0.3f), canvasHeight * 0.25f + (panOffsetY * 0.3f),
                    canvasWidth * 0.85f + (panOffsetX * 0.3f), canvasHeight * 0.45f + (panOffsetY * 0.3f)
                )
                lineTo(canvasWidth, canvasHeight * 0.45f)
                lineTo(canvasWidth, 0f)
                close()
            }
            drawPath(path = waterPath, color = Color(0xFFC7E2F8))

            // Secondary street grid
            val gridSpacing = 90.dp.toPx()
            val startX = (panOffsetX % gridSpacing)
            val startY = (panOffsetY % gridSpacing)

            var x = startX
            while (x < canvasWidth) {
                drawLine(
                    color = Color(0xFFE2E6E9),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight),
                    strokeWidth = 3.dp.toPx()
                )
                x += gridSpacing
            }

            var y = startY
            while (y < canvasHeight) {
                drawLine(
                    color = Color(0xFFE2E6E9),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 3.dp.toPx()
                )
                y += gridSpacing
            }

            // Primary Arterial Roads (100 Ft Rd & CMH Rd style)
            // Major Road 1: Diagonal Boulevard
            drawLine(
                color = Color.White,
                start = Offset(0f + (panOffsetX * 0.8f), canvasHeight * 0.28f + (panOffsetY * 0.8f)),
                end = Offset(canvasWidth + (panOffsetX * 0.8f), canvasHeight * 0.75f + (panOffsetY * 0.8f)),
                strokeWidth = 14.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFFD54F),
                start = Offset(0f + (panOffsetX * 0.8f), canvasHeight * 0.28f + (panOffsetY * 0.8f)),
                end = Offset(canvasWidth + (panOffsetX * 0.8f), canvasHeight * 0.75f + (panOffsetY * 0.8f)),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Major Road 2: Cross Arterial
            drawLine(
                color = Color.White,
                start = Offset(canvasWidth * 0.32f + (panOffsetX * 0.8f), 0f + (panOffsetY * 0.8f)),
                end = Offset(canvasWidth * 0.45f + (panOffsetX * 0.8f), canvasHeight + (panOffsetY * 0.8f)),
                strokeWidth = 12.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Active Washer Agent Indicators (simulated nearby positions)
            val agent1Offset = Offset(canvasWidth * 0.38f + panOffsetX, canvasHeight * 0.42f + panOffsetY)
            val agent2Offset = Offset(canvasWidth * 0.62f + panOffsetX, canvasHeight * 0.48f + panOffsetY)
            val agent3Offset = Offset(canvasWidth * 0.44f + panOffsetX, canvasHeight * 0.64f + panOffsetY)

            listOf(agent1Offset, agent2Offset, agent3Offset).forEach { agentPos ->
                drawCircle(
                    color = Color(0x33121212),
                    radius = 14.dp.toPx(),
                    center = agentPos
                )
                drawCircle(
                    color = RapidoYellowPrimary,
                    radius = 9.dp.toPx(),
                    center = agentPos
                )
                drawCircle(
                    color = RapidoCarbonBlack,
                    radius = 4.dp.toPx(),
                    center = agentPos
                )
            }
        }

        // 2. Center Draggable Anchor Pin
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset { IntOffset(0, pinElevationOffset.roundToPx()) },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tooltip Pill above pin
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (locationState.isLocked) RapidoSuccessGreen else RapidoCarbonBlack,
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (locationState.isLocked) Icons.Filled.Lock else Icons.Filled.NearMe,
                            contentDescription = "Pin Status",
                            tint = if (locationState.isLocked) Color.White else RapidoYellowPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (locationState.isLocked) "LOCATION LOCKED" else "DRAG PIN TO VEHICLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (locationState.isLocked) Color.White else RapidoYellowPrimary
                        )
                    }
                }

                // Teardrop Anchor Pin
                Box(
                    modifier = Modifier.size(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Vehicle Anchor Pin",
                        tint = if (locationState.isLocked) RapidoSuccessGreen else RapidoCarbonBlack,
                        modifier = Modifier.size(52.dp)
                    )
                    // Inner glowing dot
                    Box(
                        modifier = Modifier
                            .offset(y = (-6).dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(RapidoYellowPrimary)
                    )
                }

                // Ground drop shadow
                Box(
                    modifier = Modifier
                        .size(width = shadowScale * 2, height = shadowScale * 0.6f)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = if (locationState.isDragging) 0.18f else 0.35f))
                )
            }
        }

        // 3. Floating Action Button: Snap back to current GPS location
        FloatingActionButton(
            onClick = {
                panOffsetX = 0f
                panOffsetY = 0f
                onSnapToGps()
            },
            containerColor = RapidoSurfaceWhite,
            contentColor = RapidoCarbonBlack,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 140.dp)
                .size(52.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "Snap to GPS Location",
                tint = RapidoCarbonBlack,
                modifier = Modifier.size(24.dp)
            )
        }

        // 4. Manual Lock / Unlock Toggle Button on Map
        Surface(
            onClick = {
                if (locationState.isLocked) {
                    onUnlockLocation()
                } else {
                    onLockLocation()
                }
            },
            shape = RoundedCornerShape(24.dp),
            color = if (locationState.isLocked) RapidoSurfaceWhite else RapidoYellowPrimary,
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (locationState.isLocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    contentDescription = "Lock Spot",
                    tint = RapidoCarbonBlack,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (locationState.isLocked) "Re-adjust Pin Position" else "Lock Vehicle Location & Find Washers",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RapidoCarbonBlack
                )
            }
        }
    }
}
