package com.example.waggoapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waggoapp.ui.theme.*

import androidx.compose.foundation.BorderStroke
import com.example.waggoapp.ui.viewmodel.AuthState

/**
 * State object representing the UI state for the Authentication screen.
 */
typealias AuthUiState = AuthState


/**
 * Clean, production-ready Jetpack Compose Authentication screen for HydroGlide.
 * Features Material 3 components, dark neon automotive styling, and structured state hoisting.
 */
@Composable
fun AuthScreen(
    state: AuthUiState,
    onToggleAuthMode: (Boolean) -> Unit,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    onAppleSignInClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SurfaceContainerLowest,
                        SurfaceDark,
                        SurfaceContainerLow
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header with Glowing Water-Drop Beacon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHighest)
                    .border(1.5.dp, PrimaryContainerCyan.copy(alpha = 0.6f), CircleShape)
                    .shadow(elevation = 20.dp, shape = CircleShape, spotColor = PrimaryContainerCyan),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.WaterDrop,
                    contentDescription = "HydroGlide Logo",
                    tint = PrimaryContainerCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "HydroGlide",
                    color = PrimaryCyan,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainerCyan)
                )
            }

            Text(
                text = "Clean Tech Doorstep Vehicle Care",
                color = OnSurfaceVariantSlate,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Segmented Mode Tabs: Log In vs Sign Up
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainerLow)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Log In Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (!state.isSignUp) SurfaceContainerHigh else Color.Transparent
                        )
                        .then(
                            if (!state.isSignUp) Modifier.border(
                                1.dp,
                                PrimaryContainerCyan.copy(alpha = 0.4f),
                                RoundedCornerShape(20.dp)
                            ) else Modifier
                        )
                        .clickable { onToggleAuthMode(false) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log In",
                        color = if (!state.isSignUp) PrimaryContainerCyan else OnSurfaceVariantSlate,
                        fontWeight = if (!state.isSignUp) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }

                // Sign Up Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (state.isSignUp) SurfaceContainerHigh else Color.Transparent
                        )
                        .then(
                            if (state.isSignUp) Modifier.border(
                                1.dp,
                                PrimaryContainerCyan.copy(alpha = 0.4f),
                                RoundedCornerShape(20.dp)
                            ) else Modifier
                        )
                        .clickable { onToggleAuthMode(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign Up",
                        color = if (state.isSignUp) PrimaryContainerCyan else OnSurfaceVariantSlate,
                        fontWeight = if (state.isSignUp) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceContainer
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            OutlineVariant.copy(alpha = 0.3f),
                            PrimaryContainerCyan.copy(alpha = 0.2f),
                            OutlineVariant.copy(alpha = 0.3f)
                        )
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Full Name (Sign Up only)
                    AnimatedVisibility(
                        visible = state.isSignUp,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        OutlinedTextField(
                            value = state.fullName,
                            onValueChange = onFullNameChange,
                            label = { Text("Full Name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = PrimaryContainerCyan
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryContainerCyan,
                                unfocusedBorderColor = OutlineVariant,
                                focusedLabelColor = PrimaryContainerCyan,
                                unfocusedLabelColor = OnSurfaceVariantSlate,
                                focusedTextColor = OnSurfaceWhite,
                                unfocusedTextColor = OnSurfaceWhite,
                                focusedContainerColor = SurfaceContainerLow,
                                unfocusedContainerColor = SurfaceContainerLow
                            )
                        )
                    }

                    // Email Field
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = PrimaryContainerCyan
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryContainerCyan,
                            unfocusedBorderColor = OutlineVariant,
                            focusedLabelColor = PrimaryContainerCyan,
                            unfocusedLabelColor = OnSurfaceVariantSlate,
                            focusedTextColor = OnSurfaceWhite,
                            unfocusedTextColor = OnSurfaceWhite,
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedContainerColor = SurfaceContainerLow
                        )
                    )

                    // Phone Number (Sign Up only)
                    AnimatedVisibility(
                        visible = state.isSignUp,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = onPhoneChange,
                            label = { Text("Mobile Number (Doorstep Dispatch)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Phone,
                                    contentDescription = null,
                                    tint = PrimaryContainerCyan
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryContainerCyan,
                                unfocusedBorderColor = OutlineVariant,
                                focusedLabelColor = PrimaryContainerCyan,
                                unfocusedLabelColor = OnSurfaceVariantSlate,
                                focusedTextColor = OnSurfaceWhite,
                                unfocusedTextColor = OnSurfaceWhite,
                                focusedContainerColor = SurfaceContainerLow,
                                unfocusedContainerColor = SurfaceContainerLow
                            )
                        )
                    }

                    // Password Field
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = PrimaryContainerCyan
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = onTogglePasswordVisibility) {
                                Icon(
                                    imageVector = if (state.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (state.isPasswordVisible) "Hide password" else "Show password",
                                    tint = OnSurfaceVariantSlate
                                )
                            }
                        },
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryContainerCyan,
                            unfocusedBorderColor = OutlineVariant,
                            focusedLabelColor = PrimaryContainerCyan,
                            unfocusedLabelColor = OnSurfaceVariantSlate,
                            focusedTextColor = OnSurfaceWhite,
                            unfocusedTextColor = OnSurfaceWhite,
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedContainerColor = SurfaceContainerLow
                        )
                    )

                    if (state.isSignUp) {
                        Text(
                            text = "Must be 12-72 chars with uppercase, lowercase, number, & special symbol (e.g. WashGo#2026!Secured)",
                            color = PrimaryContainerCyan.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    // Options Row: Remember Me & Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onRememberMeChange(!state.rememberMe) }
                        ) {
                            Checkbox(
                                checked = state.rememberMe,
                                onCheckedChange = onRememberMeChange,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrimaryContainerCyan,
                                    checkmarkColor = SurfaceContainerLowest,
                                    uncheckedColor = OutlineSlate
                                )
                            )
                            Text(
                                text = "Remember me",
                                color = OnSurfaceVariantSlate,
                                fontSize = 13.sp
                            )
                        }

                        if (!state.isSignUp) {
                            Text(
                                text = "Forgot password?",
                                color = PrimaryContainerCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { onForgotPasswordClick() }
                            )
                        }
                    }

                    // Error Message Banner (if any)
                    if (state.errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ErrorContainerRed.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, ErrorRed)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ErrorOutline,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = state.errorMessage,
                                    color = ErrorRed,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Primary Action CTA Button
                    Button(
                        onClick = onSubmit,
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(26.dp),
                                spotColor = PrimaryContainerCyan
                            ),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryContainerCyan,
                            contentColor = SurfaceContainerLowest
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = SurfaceContainerLowest,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (state.isSignUp) "Create your account" else "Sign in to continue",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Or Continue With Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = OutlineVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "OR FAST ACCESS",
                    color = OnSurfaceVariantSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = OutlineVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Social Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SurfaceContainerHigh
                    ),
                    border = BorderStroke(1.dp, OutlineVariant)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Google",
                        tint = PrimaryCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google",
                        color = OnSurfaceWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = onAppleSignInClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SurfaceContainerHigh
                    ),
                    border = BorderStroke(1.dp, OutlineVariant)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Apple",
                        tint = PrimaryCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apple",
                        color = OnSurfaceWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Backend Connectivity Indicator
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceContainerLowest.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                    Text(
                        text = "Backend Connected: 127.0.0.1:8080",
                        color = OnSurfaceVariantSlate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF051424)
@Composable
fun AuthScreenPreview() {
    HydroGlideTheme {
        AuthScreen(
            state = AuthUiState(
                isSignUp = false,
                email = "alex.rider@example.com"
            ),
            onToggleAuthMode = {},
            onFullNameChange = {},
            onEmailChange = {},
            onPhoneChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onRememberMeChange = {},
            onSubmit = {}
        )
    }
}
