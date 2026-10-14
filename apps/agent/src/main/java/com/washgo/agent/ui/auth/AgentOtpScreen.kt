package com.washgo.agent.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.core.designsystem.component.RapidoOtpInput
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
fun AgentOtpScreen(
    viewModel: AgentAuthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = RapidoSurfaceLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp)
        ) {
            // Header with Back Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBackToPhone() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(RapidoSurfaceWhite, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = RapidoCarbonBlack
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Verify Phone",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RapidoCarbonBlack
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Icon Emblem
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(RapidoYellowPrimary)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Security Code",
                    tint = RapidoCarbonBlack,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Enter 6-Digit Code",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = RapidoCarbonBlack,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Phone display with Change option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sent to ${uiState.maskedPhone}",
                    fontSize = 14.sp,
                    color = RapidoTextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = RapidoCarbonBlack,
                    modifier = Modifier.clickable { viewModel.navigateBackToPhone() }
                )
            }

            // Dev Debug Auto-Fill Chip
            if (!uiState.debugOtp.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(Color(0xFFFFF9C4), RoundedCornerShape(20.dp))
                        .clickable { viewModel.onOtpChanged(uiState.debugOtp!!) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Dev Code: ${uiState.debugOtp} (Tap to autofill)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6-digit OTP Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RapidoOtpInput(
                        otpValue = uiState.otpInput,
                        onOtpChange = { viewModel.onOtpChanged(it) },
                        otpLength = 6,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error Message
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            fontSize = 13.sp,
                            color = RapidoDangerRed,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Verify Button
                    RapidoPrimaryButton(
                        text = "Verify Code",
                        onClick = { viewModel.verifyOtp() },
                        enabled = uiState.isOtpValid && !uiState.isLoading,
                        isLoading = uiState.isLoading
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Resend Timer / Action
                    if (uiState.canResend) {
                        Text(
                            text = "Didn't receive the code? Resend OTP",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoCarbonBlack,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable { viewModel.sendOtp() }
                        )
                    } else {
                        Text(
                            text = "Resend OTP in ${uiState.resendCountdown}s",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = RapidoTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
