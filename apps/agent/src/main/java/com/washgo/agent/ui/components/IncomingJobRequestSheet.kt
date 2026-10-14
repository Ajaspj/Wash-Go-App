package com.washgo.agent.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.agent.data.model.AgentJobNotificationMessage
import com.washgo.core.designsystem.component.RapidoStatusPill
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoDangerRed
import com.washgo.core.designsystem.theme.RapidoDivider
import com.washgo.core.designsystem.theme.RapidoSuccessGreen
import com.washgo.core.designsystem.theme.RapidoSurfaceLight
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoYellowPrimary

@Composable
fun IncomingJobRequestSheet(
    job: AgentJobNotificationMessage,
    countdownSeconds: Int,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Trigger audio ping alert immediately upon incoming job dispatch
    LaunchedEffect(job.bookingId) {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 380)
        } catch (_: Exception) {
            // Audio service gracefully ignored if device restricted
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.5.dp, RapidoYellowPrimary, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCCCCCC))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Header: Flash dispatch banner & Circular Countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RapidoStatusPill(
                    text = "RAPIDO AUTO-DISPATCH",
                    dotColor = RapidoYellowPrimary,
                    backgroundColor = Color(0xFFFFFDE7)
                )

                Spacer(modifier = Modifier.weight(1f))

                CountdownCircularIndicator(
                    remainingSeconds = countdownSeconds,
                    totalSeconds = job.timeoutSeconds,
                    size = 54.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Wash Package Tier & Vehicle Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RapidoCarbonBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (job.vehicleType?.contains("Bike", ignoreCase = true) == true)
                            Icons.Filled.TwoWheeler else Icons.Filled.DirectionsCar,
                        contentDescription = "Vehicle",
                        tint = RapidoYellowPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Premium Foam + Exterior Wash",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RapidoTextPrimary
                    )
                    Text(
                        text = "${job.vehicleType ?: "SUV / Sedan"} • Waterless Detailing",
                        fontSize = 13.sp,
                        color = RapidoTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = RapidoDivider)
            Spacer(modifier = Modifier.height(14.dp))

            // Customer Location, Distance & ETA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Customer Location",
                    tint = RapidoYellowPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.customerAddress ?: "12th Main Road, Indiranagar, Bengaluru",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RapidoTextPrimary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RapidoSurfaceLight
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = RapidoTextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "3 mins arrival • 850m away",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RapidoTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pricing & Earnings Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF0FDF4)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ESTIMATED NET PAYOUT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534),
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Instant credit on job completion",
                            fontSize = 12.sp,
                            color = RapidoTextSecondary
                        )
                    }

                    Text(
                        text = "₹${String.format("%.2f", job.earningsEstimate)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = RapidoSuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Electric-Green Swipe To Accept Slider
            SwipeToAcceptButton(
                text = "SWIPE TO ACCEPT WASH",
                onAccept = onAccept
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Decline Option
            TextButton(
                onClick = onDecline,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Decline Request",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = RapidoDangerRed
                )
            }
        }
    }
}
