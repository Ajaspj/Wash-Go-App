package com.washgo.user.ui.booking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.washgo.core.designsystem.component.RapidoStatusPill
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoSuccessGreen
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoYellowPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookingScreen(
    viewModel: BookingViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val locationState by viewModel.locationState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    // Automatically slide up bottom sheet when location is locked
    LaunchedEffect(locationState.isLocked) {
        if (locationState.isLocked) {
            sheetState.show()
        } else {
            if (sheetState.isVisible) {
                sheetState.hide()
            }
        }
    }

    // Collect snackbar announcements
    LaunchedEffect(viewModel) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetElevation = 18.dp,
        sheetBackgroundColor = RapidoSurfaceWhite,
        sheetContent = {
            BookingBottomSheetContent(
                uiState = uiState,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) },
                onAgentSelected = { viewModel.selectAgent(it) },
                onConfirmBooking = { viewModel.confirmWashBooking(it) },
                onRetrySearch = { viewModel.lockLocationAndSearch() }
            )
        },
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 1. Full-screen Google Map background with draggable anchor pin & GPS FAB
                MapCanvasView(
                    locationState = locationState,
                    onPinMoved = { lat, lng ->
                        viewModel.updatePinLocation(lat, lng)
                    },
                    onPinDragStateChanged = { isDragging ->
                        viewModel.setPinDragging(isDragging)
                    },
                    onSnapToGps = {
                        viewModel.snapToGps()
                    },
                    onLockLocation = {
                        viewModel.lockLocationAndSearch()
                    },
                    onUnlockLocation = {
                        viewModel.unlockLocation()
                    }
                )

                // 2. Top Header: Navigation & Address Search Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = RapidoYellowPrimary,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "Pin",
                                    tint = RapidoCarbonBlack,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Vehicle Wash Spot",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RapidoTextSecondary
                                )
                                Text(
                                    text = locationState.address,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RapidoTextPrimary,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            RapidoStatusPill(
                                text = if (locationState.isLocked) "LOCKED" else "GPS READY",
                                dotColor = RapidoSuccessGreen
                            )
                        }
                    }
                }

                // 3. Bottom Quick Action Bar when sheet is collapsed / hidden
                if (!sheetState.isVisible) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = RapidoSurfaceWhite,
                        shadowElevation = 10.dp
                    ) {
                        if (!locationState.isLocked) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Pin Your Vehicle",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RapidoTextPrimary
                                    )
                                    Text(
                                        text = "Drag map to center wash spot",
                                        fontSize = 12.sp,
                                        color = RapidoTextSecondary
                                    )
                                }

                                Button(
                                    onClick = { viewModel.lockLocationAndSearch() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RapidoYellowPrimary,
                                        contentColor = RapidoCarbonBlack
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FlashOn,
                                        contentDescription = "Lock",
                                        tint = RapidoCarbonBlack,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Find Washers",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RapidoCarbonBlack
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Location Locked",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RapidoTextPrimary
                                    )
                                    Text(
                                        text = "Washers found nearby",
                                        fontSize = 12.sp,
                                        color = RapidoTextSecondary
                                    )
                                }

                                Button(
                                    onClick = {
                                        scope.launch { sheetState.show() }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RapidoYellowPrimary,
                                        contentColor = RapidoCarbonBlack
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ListAlt,
                                        contentDescription = "Show Washers",
                                        tint = RapidoCarbonBlack,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "View Washers",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RapidoCarbonBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
