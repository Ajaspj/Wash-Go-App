package com.washgo.agent.ui.auth

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.core.designsystem.component.RapidoPrimaryButton
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoDangerRed
import com.washgo.core.designsystem.theme.RapidoSuccessGreen
import com.washgo.core.designsystem.theme.RapidoSurfaceLight
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoYellowPrimary

@Composable
fun AgentLoginScreen(
    viewModel: AgentAuthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = RapidoSurfaceLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // Brand Emblem
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(RapidoYellowPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Wash-Go Partner",
                        tint = RapidoCarbonBlack,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Partner Title
                Text(
                    text = "Wash-Go Partner",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = RapidoCarbonBlack,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = "Professional Mobile Detailing Dispatch",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = RapidoTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Feature Highlights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PartnerFeatureBadge(icon = Icons.Default.Speed, label = "Instant Dispatch")
                    PartnerFeatureBadge(icon = Icons.Default.ElectricCar, label = "Flexible Earnings")
                    PartnerFeatureBadge(icon = Icons.Default.Security, label = "Verified KYC")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Mobile Number Input Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Enter Mobile Number",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RapidoTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Phone Number Input with +91 Badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    color = Color(0xFFF7F7F7),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (uiState.isPhoneValid) RapidoSuccessGreen else Color(0xFFE2E2E2),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Country Code Chip
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        color = RapidoYellowPrimary.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🇮🇳 +91",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RapidoCarbonBlack
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Phone TextField
                            BasicTextField(
                                value = uiState.phoneInput,
                                onValueChange = { viewModel.onPhoneChanged(it) },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RapidoCarbonBlack,
                                    letterSpacing = 1.5.sp
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (uiState.isPhoneValid) viewModel.sendOtp()
                                    }
                                ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (uiState.phoneInput.isEmpty()) {
                                        Text(
                                            text = "10-digit number",
                                            fontSize = 16.sp,
                                            color = Color(0xFFAAAAAA),
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            if (uiState.isPhoneValid) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Valid",
                                    tint = RapidoSuccessGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Error Banner
                        if (uiState.errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                fontSize = 12.sp,
                                color = RapidoDangerRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Get OTP Button
                        RapidoPrimaryButton(
                            text = "Get Verification OTP",
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.sendOtp()
                            },
                            enabled = uiState.isPhoneValid && !uiState.isLoading,
                            isLoading = uiState.isLoading
                        )
                    }
                }
            }

            // Footer Disclaimer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "By continuing, you agree to Wash-Go Partner Agreement",
                    fontSize = 12.sp,
                    color = RapidoTextSecondary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Verified Captain Portal • Safe & Compliant",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RapidoTextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PartnerFeatureBadge(
    icon: ImageVector,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(RapidoSurfaceWhite),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = RapidoCarbonBlack,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = RapidoTextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}
