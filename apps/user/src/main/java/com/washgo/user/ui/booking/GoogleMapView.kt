package com.washgo.user.ui.booking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoSuccessGreen
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoYellowPrimary
import com.washgo.user.BuildConfig
import com.washgo.user.data.network.NearbyAgentDto
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun GoogleMapView(
    locationState: LocationState,
    nearbyAgents: List<NearbyAgentDto> = emptyList(),
    onPinMoved: (Double, Double) -> Unit,
    onPinDragStateChanged: (Boolean) -> Unit,
    onSnapToGps: (Double, Double) -> Unit,
    onLockLocation: () -> Unit,
    onUnlockLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val initialPosition = remember {
        LatLng(locationState.latitude, locationState.longitude)
    }

    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 15f)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            fetchGpsLocation(context) { lat, lng ->
                coroutineScope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 16f)
                    )
                }
                onSnapToGps(lat, lng)
            }
        }
    }

    // React to camera movements when user pans the map
    LaunchedEffect(cameraPositionState.isMoving) {
        onPinDragStateChanged(cameraPositionState.isMoving)
        if (!cameraPositionState.isMoving && !locationState.isLocked) {
            val target = cameraPositionState.position.target
            onPinMoved(target.latitude, target.longitude)
        }
    }

    // Pin elevation lift animation during dragging
    val pinElevationOffset by animateDpAsState(
        targetValue = if (locationState.isDragging) (-22).dp else 0.dp,
        animationSpec = tween(160),
        label = "pinElevationOffset"
    )
    val shadowScale by animateDpAsState(
        targetValue = if (locationState.isDragging) 18.dp else 10.dp,
        animationSpec = tween(160),
        label = "shadowScale"
    )

    // Free-tier API key state check
    val isApiKeyConfigured = remember {
        val key = BuildConfig.MAPS_API_KEY
        key.isNotBlank() && key != "DEFAULT_KEY"
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Real Interactive Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
                compassEnabled = true,
                rotationGesturesEnabled = true,
                tiltGesturesEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            // Render Real Markers for nearby agents returned from PostGIS
            nearbyAgents.forEach { agent ->
                Marker(
                    state = MarkerState(position = LatLng(agent.latitude, agent.longitude)),
                    title = agent.name,
                    snippet = "${agent.formattedEta} • ⭐ ${String.format("%.1f", agent.rating)} • ${String.format("%.1f", agent.distanceKm)} km",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }
        }

        // 2. Free-tier API Key Status Banner (if key not configured)
        AnimatedVisibility(
            visible = !isApiKeyConfigured,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 74.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = RapidoCarbonBlack.copy(alpha = 0.90f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Maps Info",
                        tint = RapidoYellowPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google Maps SDK active • Set MAPS_API_KEY in gradle.properties for custom tiles. GPS pin & PostGIS search fully operational.",
                        fontSize = 11.sp,
                        color = Color.White,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // 3. Center Fixed Anchor Pin (Rapido style center pin-drop)
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
                            text = if (locationState.isLocked) "LOCATION LOCKED" else "DRAG MAP TO CENTER VEHICLE",
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

        // 4. Floating Action Button: Snap back to current GPS location
        FloatingActionButton(
            onClick = {
                if (hasLocationPermission) {
                    fetchGpsLocation(context) { lat, lng ->
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 16f)
                            )
                        }
                        onSnapToGps(lat, lng)
                    }
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
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
                contentDescription = "Snap to Current GPS Location",
                tint = RapidoCarbonBlack,
                modifier = Modifier.size(24.dp)
            )
        }

        // 5. Manual Lock / Unlock Toggle Button on Map
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
                .padding(top = 130.dp)
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

private fun fetchGpsLocation(context: Context, onLocation: (Double, Double) -> Unit) {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                onLocation(loc.latitude, loc.longitude)
            } else {
                // Fallback to central urban coordinate
                onLocation(12.9716, 77.5946)
            }
        }.addOnFailureListener {
            onLocation(12.9716, 77.5946)
        }
    } else {
        onLocation(12.9716, 77.5946)
    }
}
