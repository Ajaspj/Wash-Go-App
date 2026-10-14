package com.washgo.agent

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.washgo.agent.data.local.AgentTokenManager
import com.washgo.agent.data.model.AgentProfileDto
import com.washgo.agent.data.model.AgentStatsDto
import com.washgo.agent.data.repository.AgentRepository
import com.washgo.agent.ui.AgentDashboardViewModel
import com.washgo.agent.ui.AgentDashboardViewModelFactory
import com.washgo.agent.ui.auth.AgentAuthStep
import com.washgo.agent.ui.auth.AgentAuthViewModel
import com.washgo.agent.ui.auth.AgentAuthViewModelFactory
import com.washgo.agent.ui.auth.AgentLoginScreen
import com.washgo.agent.ui.auth.AgentOnboardingScreen
import com.washgo.agent.ui.auth.AgentOtpScreen
import com.washgo.agent.ui.auth.AgentVerificationStatusScreen
import com.washgo.agent.ui.components.ActiveNavigationScreen
import com.washgo.agent.ui.components.IncomingJobRequestSheet
import com.washgo.core.designsystem.component.RapidoRatingBadge
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoDivider
import com.washgo.core.designsystem.theme.RapidoDangerRed
import com.washgo.core.designsystem.theme.RapidoSuccessGreen
import com.washgo.core.designsystem.theme.RapidoSurfaceLight
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoTheme
import com.washgo.core.designsystem.theme.RapidoYellowPrimary

import androidx.activity.viewModels

class AgentMainActivity : ComponentActivity() {
    private val tokenManager by lazy {
        AgentTokenManager.getInstance(applicationContext)
    }

    private val authViewModel: AgentAuthViewModel by viewModels {
        AgentAuthViewModelFactory(
            AgentRepository.getInstance(applicationContext),
            tokenManager
        )
    }

    private val dashboardViewModel: AgentDashboardViewModel by viewModels {
        AgentDashboardViewModelFactory(
            AgentRepository.getInstance(applicationContext),
            tokenManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            handleAuditIntent(intent)
        }
        setContent {
            RapidoTheme {
                AgentApp(
                    dashboardViewModel = dashboardViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (BuildConfig.DEBUG) {
            handleAuditIntent(intent)
        }
    }

    private fun handleAuditIntent(intent: android.content.Intent?) {
        if (intent == null) return
        val step = intent.getStringExtra("AUDIT_AUTH_STEP")
        val status = intent.getStringExtra("AUDIT_AGENT_STATUS")
        val isMockLogin = intent.getBooleanExtra("AUDIT_MOCK_LOGIN", false)
        val isLogout = intent.getBooleanExtra("AUDIT_LOGOUT", false)

        if (isLogout) {
            tokenManager.clear()
            authViewModel.setAuditStep(AgentAuthStep.PHONE_INPUT)
        } else if (step != null) {
            tokenManager.clear()
            when (step) {
                "OTP" -> authViewModel.setAuditStep(AgentAuthStep.OTP_INPUT)
                "ONBOARDING" -> authViewModel.setAuditStep(AgentAuthStep.ONBOARDING)
                "VERIFICATION_STATUS" -> authViewModel.setAuditStep(AgentAuthStep.VERIFICATION_STATUS)
                else -> authViewModel.setAuditStep(AgentAuthStep.PHONE_INPUT)
            }
        } else if (status != null || isMockLogin) {
            val agentStatus = status ?: "APPROVED"
            tokenManager.saveAgentSession(
                accessToken = "audit-agent-token",
                refreshToken = "audit-agent-refresh-token",
                agentId = "agt-audit-01",
                agentCode = "AGT-7821",
                phone = "+91 98765 12340",
                displayName = "Vikram Patel",
                status = agentStatus
            )
            tokenManager.updateAgentStatus(agentStatus)
        }

        if (intent.hasExtra("AUDIT_ONLINE")) {
            dashboardViewModel.setAuditOnline(intent.getBooleanExtra("AUDIT_ONLINE", false))
        }
        if (intent.getBooleanExtra("AUDIT_SIMULATE_JOB", false)) {
            dashboardViewModel.simulateIncomingOffer()
        }
        if (intent.hasExtra("AUDIT_ACTIVE_JOB")) {
            dashboardViewModel.setAuditActiveJob(intent.getBooleanExtra("AUDIT_ACTIVE_JOB", false))
        }
    }
}

@Composable
fun AgentApp(
    dashboardViewModel: AgentDashboardViewModel = viewModel(
        factory = AgentDashboardViewModelFactory(
            AgentRepository.getInstance(LocalContext.current),
            AgentTokenManager.getInstance(LocalContext.current)
        )
    ),
    authViewModel: AgentAuthViewModel = viewModel(
        factory = AgentAuthViewModelFactory(
            AgentRepository.getInstance(LocalContext.current),
            AgentTokenManager.getInstance(LocalContext.current)
        )
    )
) {
    val context = LocalContext.current
    val tokenManager = remember { AgentTokenManager.getInstance(context) }
    val isLoggedIn by tokenManager.isLoggedIn.collectAsState()
    val agentStatus by tokenManager.agentStatus.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()

    var showVerificationScreen by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        when (authUiState.step) {
            AgentAuthStep.PHONE_INPUT -> AgentLoginScreen(viewModel = authViewModel)
            AgentAuthStep.OTP_INPUT -> AgentOtpScreen(viewModel = authViewModel)
            AgentAuthStep.ONBOARDING -> AgentOnboardingScreen(viewModel = authViewModel)
            AgentAuthStep.VERIFICATION_STATUS -> AgentVerificationStatusScreen(
                viewModel = authViewModel,
                onEnterDashboard = {
                    showVerificationScreen = false
                    dashboardViewModel.loadDashboardData()
                }
            )
        }
    } else if (!tokenManager.isApproved() || showVerificationScreen) {
        AgentVerificationStatusScreen(
            viewModel = authViewModel,
            onEnterDashboard = {
                if (tokenManager.isApproved()) {
                    showVerificationScreen = false
                    dashboardViewModel.loadDashboardData()
                }
            }
        )
    } else {
        AgentDashboardScreen(
            viewModel = dashboardViewModel,
            onOpenVerification = { showVerificationScreen = true }
        )
    }
}

@Composable
fun AgentDashboardScreen(
    viewModel: AgentDashboardViewModel,
    onOpenVerification: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isOnline by viewModel.isOnline.collectAsState()
    val profile by viewModel.agentProfile.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val incomingJob by viewModel.incomingJob.collectAsState()
    val countdownSeconds by viewModel.countdownSeconds.collectAsState()
    val activeJob by viewModel.activeJob.collectAsState()
    val latestStatusUpdate by viewModel.latestStatusUpdate.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isWsConnected by viewModel.isWebSocketConnected.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            viewModel.toggleOnline(context, true)
        } else {
            // Permission denied
            viewModel.toggleOnline(context, false)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (activeJob != null) {
                ActiveNavigationScreen(
                    job = activeJob!!,
                    currentLocation = currentLocation,
                    statusUpdate = latestStatusUpdate,
                    onFinishJob = { viewModel.finishActiveJob() }
                )
            } else {
                AgentDashboardContent(
                    isOnline = isOnline,
                    isWsConnected = isWsConnected,
                    profile = profile,
                    stats = stats,
                    onToggleOnline = { wantOnline ->
                        if (wantOnline) {
                            val fineLoc = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (fineLoc) {
                                viewModel.toggleOnline(context, true)
                            } else {
                                val perms = mutableListOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                locationPermissionLauncher.launch(perms.toTypedArray())
                            }
                        } else {
                            viewModel.toggleOnline(context, false)
                        }
                    },
                    onOpenVerification = onOpenVerification,
                    onRequestLogout = { showLogoutDialog = true },
                    onSimulateJob = { viewModel.simulateIncomingOffer() }
                )
            }

            // Incoming Job Request Bottom Sheet Overlay
            AnimatedVisibility(
                visible = incomingJob != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                incomingJob?.let { job ->
                    IncomingJobRequestSheet(
                        job = job,
                        countdownSeconds = countdownSeconds,
                        onAccept = { viewModel.acceptJob() },
                        onDecline = { viewModel.declineJob() }
                    )
                }
            }
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out Partner Session") },
            text = { Text("Going offline and signing out will pause all dispatch offer notifications.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(context)
                    }
                ) {
                    Text("Log Out", color = RapidoDangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = RapidoTextSecondary)
                }
            }
        )
    }
}

@Composable
fun AgentDashboardContent(
    isOnline: Boolean,
    isWsConnected: Boolean,
    profile: AgentProfileDto?,
    stats: AgentStatsDto,
    onToggleOnline: (Boolean) -> Unit,
    onOpenVerification: () -> Unit,
    onRequestLogout: () -> Unit,
    onSimulateJob: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RapidoSurfaceLight)
    ) {
        // Top Partner Profile Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = RapidoSurfaceWhite,
            shadowElevation = 4.dp
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
                    // Profile Initial Avatar
                    val initial = profile?.displayName?.take(1)?.uppercase() ?: "P"
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(RapidoYellowPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoCarbonBlack
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile?.displayName ?: "Wash-Go Partner",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoTextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RapidoRatingBadge(
                                rating = profile?.averageRating ?: stats.averageRating,
                                reviewsCount = profile?.totalReviewsCount ?: stats.totalReviewsCount
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = profile?.agentCode ?: "AGT-PARTNER",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RapidoTextSecondary
                            )
                        }
                    }

                    // Online Availability Switch
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isOnline) "ONLINE" else "OFFLINE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isOnline) RapidoSuccessGreen else RapidoTextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = isOnline,
                            onCheckedChange = onToggleOnline,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RapidoCarbonBlack,
                                checkedTrackColor = RapidoYellowPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = RapidoDivider)
                Spacer(modifier = Modifier.height(14.dp))

                // Daily Metrics from real backend stats!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val earningsDisplay = if (stats.todayEarnings > 0) {
                        "₹${String.format("%.0f", stats.todayEarnings)}"
                    } else {
                        "₹0"
                    }
                    MetricCard(title = "Today's Earnings", value = earningsDisplay)
                    MetricCard(title = "Washes Done", value = stats.todayCompletedWashes.toString())
                    MetricCard(title = "Acceptance Rate", value = "${stats.acceptanceRatePercentage}%")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dispatch Status Radar Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) Color(0xFFE8F5E9) else RapidoSurfaceLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Filled.Sensors else Icons.Filled.MyLocation,
                            contentDescription = null,
                            tint = if (isOnline) RapidoSuccessGreen else RapidoTextSecondary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isOnline) "Searching for Nearby Wash Bookings..." else "You are currently Offline",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RapidoTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isOnline)
                            "Foreground GPS tracking active. Live location streaming to dispatch engine."
                        else
                            "Turn on the switch above to start broadcasting your location and receiving high-paying car & bike wash requests.",
                        fontSize = 12.sp,
                        color = RapidoTextSecondary,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    if (isOnline) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isWsConnected) RapidoSuccessGreen else Color.LightGray)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isWsConnected) "WebSocket Connected (/ws/booking)" else "Connecting WebSocket...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isWsConnected) RapidoSuccessGreen else RapidoTextSecondary
                            )
                        }

                        // Simulation button: Strictly BuildConfig.DEBUG only!
                        if (BuildConfig.DEBUG) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onSimulateJob,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = null,
                                    tint = RapidoCarbonBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Simulate Job Offer (Dev Only)",
                                    color = RapidoCarbonBlack,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Partner Quick Actions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Partner Account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RapidoCarbonBlack
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenVerification,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RapidoCarbonBlack)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("KYC Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onRequestLogout,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RapidoDangerRed)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp), tint = RapidoDangerRed)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log Out", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RapidoDangerRed)
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
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = RapidoTextPrimary
        )
        Text(
            text = title,
            fontSize = 11.sp,
            color = RapidoTextSecondary
        )
    }
}
