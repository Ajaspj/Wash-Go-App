package com.washgo.user.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalCarWash
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.core.designsystem.component.RapidoPrimaryButton
import com.washgo.core.designsystem.theme.RapidoBorder
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoCarbonDark
import com.washgo.core.designsystem.theme.RapidoDangerRed
import com.washgo.core.designsystem.theme.RapidoSelectedBorder
import com.washgo.core.designsystem.theme.RapidoSurfaceLight
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextMuted
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoYellowDark
import com.washgo.core.designsystem.theme.RapidoYellowLight
import com.washgo.core.designsystem.theme.RapidoYellowPrimary

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onPhoneChanged: (String) -> Unit,
    onContinueClicked: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

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
            // Top Section: Branding & Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp)
            ) {
                // Wash-Go Rapido Brand Card
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(RapidoYellowPrimary)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RapidoCarbonBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalCarWash,
                            contentDescription = "Wash-Go Logo",
                            tint = RapidoYellowPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "WASH-GO",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = RapidoCarbonBlack
                        )
                        Text(
                            text = "DOORSTEP DETAILING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = RapidoCarbonDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Welcome Typography
                Text(
                    text = "Explore Wash-Go",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RapidoTextPrimary,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Doorstep car & bike washes in under 15 minutes. Enter your mobile number to get started.",
                    fontSize = 14.sp,
                    color = RapidoTextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

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

                // Phone Input Component
                Text(
                    text = "MOBILE NUMBER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RapidoTextSecondary,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RapidoSurfaceLight)
                        .border(
                            width = if (uiState.phoneInput.isNotEmpty()) 1.5.dp else 1.dp,
                            color = if (uiState.phoneInput.isNotEmpty()) RapidoSelectedBorder else RapidoBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country Code Pill (Default India +91)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(RapidoSurfaceWhite)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🇮🇳",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+91",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoCarbonBlack
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(RapidoBorder)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Number Input Field
                    BasicTextField(
                        value = uiState.phoneInput,
                        onValueChange = onPhoneChanged,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        textStyle = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RapidoCarbonBlack,
                            letterSpacing = 1.sp
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = if (uiState.isPhoneValid) ImeAction.Done else ImeAction.None
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (uiState.isPhoneValid) {
                                    keyboardController?.hide()
                                    onContinueClicked()
                                }
                            }
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(RapidoCarbonBlack),
                        decorationBox = { innerTextField ->
                            if (uiState.phoneInput.isEmpty()) {
                                Text(
                                    text = "Enter 10-digit number",
                                    fontSize = 15.sp,
                                    color = RapidoTextMuted
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (uiState.phoneInput.isNotEmpty()) {
                        IconButton(
                            onClick = { onPhoneChanged("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = RapidoTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Validation helper text
                if (uiState.phoneInput.isNotEmpty() && !uiState.isPhoneValid) {
                    Text(
                        text = "Must be a 10-digit number starting with 6, 7, 8, or 9",
                        fontSize = 11.sp,
                        color = RapidoDangerRed,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Bottom Section: CTA & Legal Disclaimer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RapidoPrimaryButton(
                    text = "Continue",
                    onClick = {
                        keyboardController?.hide()
                        onContinueClicked()
                    },
                    enabled = uiState.isPhoneValid,
                    isLoading = uiState.isLoading
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Legal Disclaimer
                Text(
                    text = "By continuing, you agree to Wash-Go's\nTerms of Service and Privacy Policy",
                    fontSize = 12.sp,
                    color = RapidoTextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
