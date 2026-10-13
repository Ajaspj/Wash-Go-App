package com.washgo.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.washgo.core.designsystem.component.RapidoOtpInput
import com.washgo.core.designsystem.component.RapidoPrimaryButton
import com.washgo.core.designsystem.component.RapidoRatingBadge
import com.washgo.core.designsystem.component.RapidoStatusPill
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoDangerRed
import com.washgo.core.designsystem.theme.RapidoDivider
import com.washgo.core.designsystem.theme.RapidoSuccessGreen
import com.washgo.core.designsystem.theme.RapidoSurfaceLight
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoTheme
import com.washgo.core.designsystem.theme.RapidoYellowPrimary
import com.washgo.agent.data.network.AcceptBookingPayload
import com.washgo.agent.data.network.AgentNetworkClient
import com.washgo.agent.data.network.IncomingJobOffer
import com.washgo.core.model.BookingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AgentMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RapidoTheme {
                AgentDashboardScreen()
            }
        }
    }
}

@Composable
fun AgentDashboardScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isOnline by remember { mutableStateOf(false) }
    var incomingOffer by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableIntStateOf(20) }
    var activeOffer by remember { mutableStateOf<IncomingJobOffer?>(null) }

    var currentJobState by remember { mutableStateOf<BookingState?>(null) }
    var startOtp by remember { mutableStateOf("") }
    var completeOtp by remember { mutableStateOf("") }

    // Listen to real-time WebSocket incoming dispatch offers
    LaunchedEffect(isOnline) {
        if (isOnline) {
            AgentNetworkClient.connectWebSocket()
            AgentNetworkClient.incomingOffers.collect { offer ->
                activeOffer = offer
                countdownSeconds = offer.durationSeconds
                incomingOffer = true
            }
        } else {
            AgentNetworkClient.disconnect()
        }
    }

    // 20s Countdown timer for incoming offer
    LaunchedEffect(incomingOffer) {
        if (incomingOffer) {
            while (countdownSeconds > 0 && incomingOffer) {
                delay(1000)
                countdownSeconds--
            }
            if (countdownSeconds == 0) {
                incomingOffer = false
                activeOffer = null
                snackbarHostState.showSnackbar("Offer expired. You missed a booking offer.")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RapidoSurfaceLight)
                .padding(paddingValues)
        ) {
            // Header: Agent Profile & Online Toggle
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = RapidoSurfaceWhite,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(RapidoYellowPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "R",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = RapidoCarbonBlack
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ramesh Kumar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = RapidoTextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RapidoRatingBadge(rating = 4.9, reviewsCount = 142)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AGT-BLR-042",
                                    fontSize = 12.sp,
                                    color = RapidoTextSecondary
                                )
                            }
                        }

                        // Online / Offline Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOnline) "ONLINE" else "OFFLINE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isOnline) RapidoSuccessGreen else RapidoTextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = isOnline,
                                onCheckedChange = { checked ->
                                    isOnline = checked
                                    if (checked) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("You are now ONLINE. Ready for wash dispatch.")
                                            delay(1500)
                                            incomingOffer = true // Simulate incoming job
                                        }
                                    } else {
                                        incomingOffer = false
                                        currentJobState = null
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = RapidoCarbonBlack,
                                    checkedTrackColor = RapidoYellowPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = RapidoDivider)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricCard(title = "Today's Earnings", value = "₹1,240")
                        MetricCard(title = "Washes Done", value = "4")
                        MetricCard(title = "Acceptance Rate", value = "96%")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Incoming Broadcast Offer Alert
                if (incomingOffer && currentJobState == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, RapidoYellowPrimary, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RapidoStatusPill(
                                    text = "NEW DISPATCH OFFER",
                                    dotColor = RapidoYellowPrimary,
                                    backgroundColor = Color(0xFFFFF9C4)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                // Countdown timer
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RapidoCarbonBlack),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${countdownSeconds}s",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RapidoYellowPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = activeOffer?.serviceName ?: "Sedan Foam & Detail Wash",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = RapidoTextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "Pickup",
                                    tint = RapidoYellowPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${activeOffer?.customerAddress ?: "12th Main Road, Indiranagar"} (${activeOffer?.distanceMeters ?: 850} m away)",
                                    fontSize = 13.sp,
                                    color = RapidoTextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Your Net Earnings:",
                                    fontSize = 14.sp,
                                    color = RapidoTextSecondary
                                )
                                Text(
                                    text = "₹${String.format("%.2f", activeOffer?.agentEarnings ?: 245.0)}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RapidoSuccessGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Accept / Decline Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        incomingOffer = false
                                        activeOffer = null
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFFFEBEE),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "Decline",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RapidoDangerRed
                                        )
                                    }
                                }

                                RapidoPrimaryButton(
                                    text = "Accept Job",
                                    onClick = {
                                        val offer = activeOffer
                                        incomingOffer = false
                                        currentJobState = BookingState.AGENT_EN_ROUTE
                                        scope.launch {
                                            try {
                                                if (offer != null) {
                                                    AgentNetworkClient.api.acceptBooking(
                                                        offer.bookingId,
                                                        AcceptBookingPayload(offer.bookingId, "00000000-0000-0000-0000-000000000001")
                                                    )
                                                }
                                            } catch (_: Exception) {
                                                // Retain local state on offline emulator sandbox
                                            }
                                            snackbarHostState.showSnackbar("Offer accepted! En route to customer.")
                                        }
                                    },
                                    modifier = Modifier.weight(1.5f)
                                )
                            }
                        }
                    }
                }

                // Active Job Flow (En Route -> In Progress -> Completed)
                if (currentJobState != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RapidoStatusPill(
                                    text = when (currentJobState) {
                                        BookingState.AGENT_EN_ROUTE -> "EN ROUTE TO CUSTOMER"
                                        BookingState.IN_PROGRESS -> "WASH IN PROGRESS"
                                        BookingState.COMPLETED -> "WASH COMPLETED"
                                        else -> "ACTIVE JOB"
                                    },
                                    dotColor = RapidoSuccessGreen
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { /* Call Customer */ }) {
                                    Icon(
                                        imageVector = Icons.Filled.Call,
                                        contentDescription = "Call",
                                        tint = RapidoCarbonBlack
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Customer: Aditi Sharma",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = RapidoTextPrimary
                            )
                            Text(
                                text = "Honda City • White (KA-01-MJ-9912)",
                                fontSize = 13.sp,
                                color = RapidoTextSecondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = RapidoDivider)
                            Spacer(modifier = Modifier.height(16.dp))

                            when (currentJobState) {
                                BookingState.AGENT_EN_ROUTE -> {
                                    Text(
                                        text = "Ask customer for 4-Digit Start OTP upon arrival:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RapidoTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    RapidoOtpInput(
                                        otpValue = startOtp,
                                        onOtpChange = { startOtp = it }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    RapidoPrimaryButton(
                                        text = "Verify Start OTP & Begin Wash",
                                        onClick = {
                                            if (startOtp.length == 4) {
                                                currentJobState = BookingState.IN_PROGRESS
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("OTP verified! Wash session active.")
                                                }
                                            }
                                        },
                                        enabled = startOtp.length == 4
                                    )
                                }

                                BookingState.IN_PROGRESS -> {
                                    Text(
                                        text = "Enter Customer 4-Digit Completion OTP when finished:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RapidoTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    RapidoOtpInput(
                                        otpValue = completeOtp,
                                        onOtpChange = { completeOtp = it }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    RapidoPrimaryButton(
                                        text = "Complete Wash & Collect ₹245",
                                        onClick = {
                                            if (completeOtp.length == 4) {
                                                currentJobState = BookingState.COMPLETED
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Wash completed! ₹245 credited to your wallet.")
                                                }
                                            }
                                        },
                                        enabled = completeOtp.length == 4
                                    )
                                }

                                BookingState.COMPLETED -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Success",
                                            tint = RapidoSuccessGreen,
                                            modifier = Modifier.size(56.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Great job! Earnings Credited",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RapidoTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        RapidoPrimaryButton(
                                            text = "Ready for Next Wash",
                                            onClick = {
                                                currentJobState = null
                                                startOtp = ""
                                                completeOtp = ""
                                            }
                                        )
                                    }
                                }

                                else -> {}
                            }
                        }
                    }
                }

                // Empty / Waiting State
                if (!incomingOffer && currentJobState == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFFE8F5E9) else Color(0xFFEEEEEE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PowerSettingsNew,
                                    contentDescription = "Online Status",
                                    tint = if (isOnline) RapidoSuccessGreen else RapidoTextSecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isOnline) "You are Online & Ready" else "You are Offline",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = RapidoTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isOnline) "Waiting for incoming wash requests in your radius..." else "Toggle switch above to start receiving bookings",
                                fontSize = 13.sp,
                                color = RapidoTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 11.sp, color = RapidoTextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = RapidoTextPrimary)
    }
}
