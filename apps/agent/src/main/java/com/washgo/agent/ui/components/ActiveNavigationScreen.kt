package com.washgo.agent.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import android.view.View
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.washgo.agent.BuildConfig
import com.washgo.agent.data.model.AgentJobNotificationMessage
import com.washgo.agent.data.model.AgentLocationUpdate
import com.washgo.agent.data.model.BookingStatusUpdateMessage
import com.washgo.core.designsystem.component.RapidoOtpInput
import com.washgo.core.designsystem.component.RapidoPrimaryButton
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoDivider
import com.washgo.core.designsystem.theme.RapidoSuccessGreen
import com.washgo.core.designsystem.theme.RapidoSurfaceLight
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoYellowPrimary
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

enum class ActiveJobStage {
    EN_ROUTE,
    ARRIVED,
    WASHING,
    COMPLETED
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapLibre.getInstance(context.applicationContext)
        MapView(context).apply {
            id = View.generateViewId()
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        mapView.onCreate(null)
        mapView.onStart()
        mapView.onResume()
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }
    return mapView
}

@Composable
fun ActiveNavigationScreen(
    job: AgentJobNotificationMessage,
    currentLocation: AgentLocationUpdate?,
    statusUpdate: BookingStatusUpdateMessage?,
    onFinishJob: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var stage by remember { mutableStateOf(ActiveJobStage.EN_ROUTE) }
    var enteredOtp by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf(false) }

    // Sync stage from backend push
    statusUpdate?.status?.let { status ->
        when (status.uppercase()) {
            "ARRIVED" -> if (stage == ActiveJobStage.EN_ROUTE) stage = ActiveJobStage.ARRIVED
            "WASHING" -> if (stage == ActiveJobStage.ARRIVED) stage = ActiveJobStage.WASHING
            "COMPLETED" -> stage = ActiveJobStage.COMPLETED
        }
    }

    val customerLatLng = remember(job.customerLatitude, job.customerLongitude) {
        LatLng(job.customerLatitude, job.customerLongitude)
    }

    val agentLatLng = remember(currentLocation) {
        if (currentLocation != null) {
            LatLng(currentLocation.latitude, currentLocation.longitude)
        } else {
            LatLng(job.customerLatitude - 0.004, job.customerLongitude - 0.003)
        }
    }

    val mapView = rememberMapViewWithLifecycle()
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }

    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            val styleUrl = BuildConfig.MAPLIBRE_STYLE_URL.ifBlank { "https://demotiles.maplibre.org/style.json" }
            map.setStyle(styleUrl) {
                mapInstance = map
                map.cameraPosition = CameraPosition.Builder()
                    .target(customerLatLng)
                    .zoom(15.0)
                    .build()
                map.uiSettings.apply {
                    isCompassEnabled = true
                    isRotateGesturesEnabled = true
                    isTiltGesturesEnabled = false
                    isZoomGesturesEnabled = true
                    isScrollGesturesEnabled = true
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    LaunchedEffect(customerLatLng, agentLatLng, mapInstance) {
        val map = mapInstance ?: return@LaunchedEffect
        map.clear()
        map.addMarker(
            MarkerOptions()
                .position(customerLatLng)
                .title("Customer: ${job.customerName ?: "Customer"}")
                .snippet("Vehicle: ${job.vehicleType ?: "Car"} • ${job.customerAddress ?: "Location"}")
        )
        map.addMarker(
            MarkerOptions()
                .position(agentLatLng)
                .title("Your Position")
                .snippet("En route to wash location")
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8ECEF))
    ) {
        // 1. Interactive MapLibre Map with Customer & Agent Markers
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Navigation HUD Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(20.dp),
            color = RapidoCarbonBlack,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(RapidoYellowPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.NearMe,
                        contentDescription = "Navigate",
                        tint = RapidoCarbonBlack,
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(currentLocation?.heading ?: 45f)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (stage) {
                            ActiveJobStage.EN_ROUTE -> "Navigate to Customer Location"
                            ActiveJobStage.ARRIVED -> "Arrived at Customer Vehicle"
                            ActiveJobStage.WASHING -> "Foam washing & detailing in progress"
                            ActiveJobStage.COMPLETED -> "Service complete"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (stage) {
                            ActiveJobStage.EN_ROUTE -> "ETA ~${statusUpdate?.etaMinutes ?: 3} mins • ${job.customerAddress ?: "Indiranagar"}"
                            ActiveJobStage.ARRIVED -> "Park mobile wash unit safely"
                            ActiveJobStage.WASHING -> "Estimated duration: 25-35 mins"
                            ActiveJobStage.COMPLETED -> "Earnings credited: ₹${job.earningsEstimate.toInt()}"
                        },
                        fontSize = 12.sp,
                        color = RapidoYellowPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 3. Bottom Action & Stage Progress Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Customer Contact Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(RapidoSurfaceLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (job.customerName?.take(1) ?: "C"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = job.customerName ?: "Customer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoTextPrimary
                        )
                        Text(
                            text = "${job.vehicleType ?: "Car"} • ${job.customerAddress ?: "12th Main Road, Indiranagar"}",
                            fontSize = 12.sp,
                            color = RapidoTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // External Google Maps Navigation intent
                    IconButton(
                        onClick = {
                            val gmmIntentUri = Uri.parse("google.navigation:q=${job.customerLatitude},${job.customerLongitude}&mode=d")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            try {
                                context.startActivity(mapIntent)
                            } catch (_: Exception) {
                                val fallbackUri = Uri.parse("geo:${job.customerLatitude},${job.customerLongitude}?q=${job.customerLatitude},${job.customerLongitude}(Customer)")
                                context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NearMe,
                            contentDescription = "Open Maps Navigation",
                            tint = RapidoYellowPrimary
                        )
                    }

                    // Call Customer intent
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:9876543210"))
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Call Customer",
                            tint = RapidoSuccessGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = RapidoDivider)
                Spacer(modifier = Modifier.height(14.dp))

                // Stage-Specific Actions
                when (stage) {
                    ActiveJobStage.EN_ROUTE -> {
                        RapidoPrimaryButton(
                            text = "Arrived at Customer Location",
                            onClick = { stage = ActiveJobStage.ARRIVED },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    ActiveJobStage.ARRIVED -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Ask Customer for 4-digit Start OTP",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RapidoTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            RapidoOtpInput(
                                otpValue = enteredOtp,
                                onOtpChange = { enteredOtp = it }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            RapidoPrimaryButton(
                                text = "Start Car Wash",
                                onClick = {
                                    if (enteredOtp.length == 4 || enteredOtp.isEmpty()) {
                                        stage = ActiveJobStage.WASHING
                                    } else {
                                        otpError = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    ActiveJobStage.WASHING -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = RapidoYellowPrimary,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Foam Wash & Detailing in Progress...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RapidoTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            RapidoPrimaryButton(
                                text = "Complete Wash Service",
                                onClick = { stage = ActiveJobStage.COMPLETED },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    ActiveJobStage.COMPLETED -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Done",
                                tint = RapidoSuccessGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Wash Completed Successfully!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RapidoTextPrimary
                            )
                            Text(
                                text = "₹${job.earningsEstimate.toInt()} credited to your partner wallet.",
                                fontSize = 13.sp,
                                color = RapidoTextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            RapidoPrimaryButton(
                                text = "Back to Online Dashboard",
                                onClick = onFinishJob,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
