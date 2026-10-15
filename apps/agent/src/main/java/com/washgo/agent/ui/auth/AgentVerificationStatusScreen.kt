package com.washgo.agent.ui.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.agent.data.model.AgentDocumentDto
import com.washgo.agent.data.model.DocumentType
import com.washgo.core.designsystem.component.RapidoPrimaryButton
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
fun AgentVerificationStatusScreen(
    viewModel: AgentAuthViewModel,
    onEnterDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.agentProfile
    val status = profile?.status ?: "PENDING_VERIFICATION"

    LaunchedEffect(Unit) {
        viewModel.loadDocuments()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadDocument(context, uri)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = RapidoSurfaceLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Profile Avatar Initial
                val initial = profile?.displayName?.take(1)?.uppercase() ?: "P"
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(RapidoYellowPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = RapidoCarbonBlack
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = profile?.displayName ?: "Partner Applicant",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RapidoCarbonBlack
                )

                if (profile?.agentCode != null) {
                    Text(
                        text = "Partner ID: ${profile.agentCode}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RapidoTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Status Card
                when (status) {
                    "APPROVED" -> {
                        StatusCard(
                            icon = Icons.Default.CheckCircle,
                            iconColor = RapidoSuccessGreen,
                            badgeColor = Color(0xFFE8F5E9),
                            statusTitle = "Account Approved",
                            statusDescription = "Your KYC and mobile detailing equipment are fully approved. You can now go online to receive live customer booking dispatch offers.",
                            actionButton = {
                                RapidoPrimaryButton(
                                    text = "Open Partner Dashboard",
                                    onClick = onEnterDashboard
                                )
                            }
                        )
                    }
                    "UNDER_REVIEW" -> {
                        StatusCard(
                            icon = Icons.Default.HourglassTop,
                            iconColor = Color(0xFF1976D2),
                            badgeColor = Color(0xFFE3F2FD),
                            statusTitle = "Application Under Review",
                            statusDescription = "Our operations team is currently reviewing your application and detailing credentials. Only verified and approved partners may activate availability.",
                            actionButton = {
                                OutlinedButton(
                                    onClick = { viewModel.refreshProfile() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RapidoCarbonBlack)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Check Verification Status", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                    "REJECTED" -> {
                        StatusCard(
                            icon = Icons.Default.Cancel,
                            iconColor = RapidoDangerRed,
                            badgeColor = Color(0xFFFFEBEE),
                            statusTitle = "Application Needs Attention",
                            statusDescription = profile?.rejectionReason ?: "Your application could not be approved at this time. Please upload the requested documents below and re-submit.",
                            actionButton = {
                                RapidoPrimaryButton(
                                    text = "Re-submit for Review",
                                    onClick = { viewModel.submitForReview() },
                                    isLoading = uiState.isLoading
                                )
                            }
                        )
                    }
                    else -> { // PENDING_VERIFICATION / DRAFT
                        StatusCard(
                            icon = Icons.Default.PendingActions,
                            iconColor = Color(0xFFF57C00),
                            badgeColor = Color(0xFFFFF3E0),
                            statusTitle = "Verification Pending",
                            statusDescription = "Upload your verification documents below (Government ID, Driving License) and submit your profile for administrator review.",
                            actionButton = {
                                RapidoPrimaryButton(
                                    text = "Submit Application for Review",
                                    onClick = { viewModel.submitForReview() },
                                    isLoading = uiState.isLoading
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // KYC Document Upload Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = RapidoYellowPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "KYC Document Verification",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = RapidoCarbonBlack
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Select Document Type:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = RapidoTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Document Type Selection Chips
                        val docTypes = listOf(
                            DocumentType.GOVERNMENT_ID to "Govt ID / Aadhaar",
                            DocumentType.DRIVING_LICENSE to "Driving License",
                            DocumentType.VEHICLE_REGISTRATION to "Vehicle RC",
                            DocumentType.INSURANCE to "Insurance"
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(docTypes) { (type, label) ->
                                val isSelected = uiState.selectedDocumentType == type.name
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onDocumentTypeSelected(type.name) },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RapidoYellowPrimary,
                                        selectedLabelColor = RapidoCarbonBlack
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Optional Document Number
                        OutlinedTextField(
                            value = uiState.documentNumberInput,
                            onValueChange = { viewModel.onDocumentNumberChanged(it) },
                            label = { Text("Document Number (Optional)") },
                            placeholder = { Text("e.g. DL-0420110012345") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RapidoCarbonBlack,
                                cursorColor = RapidoCarbonBlack
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Upload Button
                        OutlinedButton(
                            onClick = { filePickerLauncher.launch("*/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isUploadingDocument,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RapidoCarbonBlack)
                        ) {
                            if (uiState.isUploadingDocument) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = RapidoCarbonBlack,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Uploading Document...", fontWeight = FontWeight.SemiBold)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = "Upload",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Choose File to Upload", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Success / Error messages
                        if (uiState.successMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = uiState.successMessage!!,
                                color = RapidoSuccessGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (uiState.errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                color = RapidoDangerRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Uploaded Documents List
                        if (uiState.documents.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = RapidoDivider)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Uploaded Documents (${uiState.documents.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = RapidoCarbonBlack
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            uiState.documents.forEach { doc ->
                                DocumentItemRow(doc)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // Bottom Actions: Logout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RapidoDangerRed)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = RapidoDangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Log Out Partner Session", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DocumentItemRow(doc: AgentDocumentDto) {
    val (statusColor, badgeBg) = when (doc.verificationStatus) {
        "VERIFIED" -> Pair(RapidoSuccessGreen, Color(0xFFE8F5E9))
        "REJECTED" -> Pair(RapidoDangerRed, Color(0xFFFFEBEE))
        else -> Pair(Color(0xFFF57C00), Color(0xFFFFF3E0))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RapidoSurfaceLight)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = RapidoTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = doc.documentType.replace('_', ' '),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RapidoCarbonBlack
                )
                if (doc.documentNumber != null) {
                    Text(
                        text = doc.documentNumber,
                        fontSize = 11.sp,
                        color = RapidoTextSecondary
                    )
                } else if (doc.fileName != null) {
                    Text(
                        text = doc.fileName,
                        fontSize = 11.sp,
                        color = RapidoTextSecondary
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeBg)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = doc.verificationStatus,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
private fun StatusCard(
    icon: ImageVector,
    iconColor: Color,
    badgeColor: Color,
    statusTitle: String,
    statusDescription: String,
    actionButton: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = statusTitle,
                    tint = iconColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = statusTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = RapidoCarbonBlack,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = statusDescription,
                fontSize = 14.sp,
                color = RapidoTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            actionButton()
        }
    }
}
