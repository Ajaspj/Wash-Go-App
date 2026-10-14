package com.washgo.user.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.core.designsystem.component.RapidoOtpInput
import com.washgo.core.designsystem.component.RapidoPrimaryButton
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoCarbonDark
import com.washgo.core.designsystem.theme.RapidoDangerRed
import com.washgo.core.designsystem.theme.RapidoSurfaceLight
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextMuted
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoYellowDark
import com.washgo.core.designsystem.theme.RapidoYellowLight
import com.washgo.core.designsystem.theme.RapidoYellowPrimary

@Composable
fun OtpVerificationScreen(
    uiState: AuthUiState,
    onOtpChanged: (String) -> Unit,
    onVerifyClicked: () -> Unit,
    onResendClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        color = RapidoSurfaceWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                // Navigation Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RapidoSurfaceLight)
                            .clickable { onBackClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = RapidoCarbonBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Step 2 of 2",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RapidoTextMuted,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Title & Masked Phone
                Text(
                    text = "Verify OTP",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RapidoTextPrimary,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter the 6-digit code sent to your number",
                    fontSize = 14.sp,
                    color = RapidoTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Masked Phone Row with Edit Action
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(RapidoSurfaceLight)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = uiState.maskedPhone,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RapidoCarbonBlack,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        modifier = Modifier.clickable { onBackClicked() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit phone",
                            tint = RapidoCarbonDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Edit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoCarbonDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Error message banner
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEBEE))
                            .border(1.dp, RapidoDangerRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = RapidoDangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            fontSize = 13.sp,
                            color = RapidoDangerRed,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = RapidoDangerRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 6-digit OTP Input
                RapidoOtpInput(
                    otpValue = uiState.otpInput,
                    onOtpChange = onOtpChanged,
                    otpLength = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Dev OTP helper banner (if debugOtp returned)
                if (!uiState.debugOtp.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RapidoYellowLight)
                            .clickable { onOtpChanged(uiState.debugOtp) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ Dev Auto-Fill: ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoCarbonBlack
                        )
                        Text(
                            text = uiState.debugOtp,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RapidoYellowDark,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(Tap to fill)",
                            fontSize = 11.sp,
                            color = RapidoTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Resend Timer Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.resendCountdown > 0) {
                        Text(
                            text = "Resend OTP in ",
                            fontSize = 13.sp,
                            color = RapidoTextSecondary
                        )
                        Text(
                            text = "${uiState.resendCountdown}s",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoCarbonBlack
                        )
                    } else {
                        Text(
                            text = "Didn't receive the code? ",
                            fontSize = 13.sp,
                            color = RapidoTextSecondary
                        )
                        Text(
                            text = "Resend OTP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoCarbonBlack,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(enabled = uiState.canResend) { onResendClicked() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Bottom Section: Verify CTA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RapidoPrimaryButton(
                    text = "Verify & Proceed",
                    onClick = {
                        keyboardController?.hide()
                        onVerifyClicked()
                    },
                    enabled = uiState.isOtpValid,
                    isLoading = uiState.isLoading
                )
            }
        }
    }
}
