package com.washgo.agent.ui.auth

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun AgentOnboardingScreen(
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
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Step Header
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        imageVector = Icons.Default.Person,
                        contentDescription = "Onboarding",
                        tint = RapidoCarbonBlack,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Partner Onboarding",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = RapidoCarbonBlack
                    )
                    Text(
                        text = "Step 2 of 3 • Profile & Detailing Setup",
                        fontSize = 13.sp,
                        color = RapidoTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Verified Mobile Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = RapidoSuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Verified Mobile Number",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoSuccessGreen
                        )
                        Text(
                            text = uiState.formattedFullPhone,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoCarbonBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Personal Details
            SectionHeader(title = "1. Personal Information")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = uiState.firstName,
                        onValueChange = { viewModel.onFirstNameChanged(it) },
                        label = { Text("First Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = outlinedColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.lastName,
                        onValueChange = { viewModel.onLastNameChanged(it) },
                        label = { Text("Last Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = outlinedColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label = { Text("Email Address *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = outlinedColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Vehicle Service Specialty
            SectionHeader(title = "2. Service Specialty")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "What vehicles are you equipped to wash & detail?",
                        fontSize = 13.sp,
                        color = RapidoTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SpecialtySelectCard(
                            icon = Icons.Default.TwoWheeler,
                            title = "Bikes Only",
                            selected = uiState.vehicleSpecialty == "BIKE_ONLY",
                            onClick = { viewModel.onSpecialtyChanged("BIKE_ONLY") },
                            modifier = Modifier.weight(1f)
                        )
                        SpecialtySelectCard(
                            icon = Icons.Default.DirectionsCar,
                            title = "Cars Only",
                            selected = uiState.vehicleSpecialty == "CAR_ONLY",
                            onClick = { viewModel.onSpecialtyChanged("CAR_ONLY") },
                            modifier = Modifier.weight(1f)
                        )
                        SpecialtySelectCard(
                            icon = Icons.Default.ElectricBike,
                            title = "All Types",
                            selected = uiState.vehicleSpecialty == "BOTH",
                            onClick = { viewModel.onSpecialtyChanged("BOTH") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Equipment & Capabilities
            SectionHeader(title = "3. Equipment Capabilities")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = RapidoSurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Water Capacity Slider
                    Text(
                        text = "Water Storage Tank Capacity: ${uiState.waterCapacity} Liters",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RapidoCarbonBlack
                    )
                    Slider(
                        value = uiState.waterCapacity.toFloat(),
                        onValueChange = {
                            viewModel.onEquipmentChanged(it.toInt(), uiState.hasPressureWasher, uiState.hasVacuumExtractor)
                        },
                        valueRange = 0f..500f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = RapidoCarbonBlack,
                            activeTrackColor = RapidoYellowPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pressure Washer Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = "High-Pressure Washer",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = RapidoTextPrimary
                            )
                            Text(
                                text = "Equipped with mobile electric/petrol pressure unit",
                                fontSize = 12.sp,
                                color = RapidoTextSecondary
                            )
                        }
                        Switch(
                            checked = uiState.hasPressureWasher,
                            onCheckedChange = {
                                viewModel.onEquipmentChanged(uiState.waterCapacity, it, uiState.hasVacuumExtractor)
                            },
                            colors = switchColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Vacuum Extractor Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = "Vacuum & Extractor",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = RapidoTextPrimary
                            )
                            Text(
                                text = "Interior upholstery vacuum cleaning kit",
                                fontSize = 12.sp,
                                color = RapidoTextSecondary
                            )
                        }
                        Switch(
                            checked = uiState.hasVacuumExtractor,
                            onCheckedChange = {
                                viewModel.onEquipmentChanged(uiState.waterCapacity, uiState.hasPressureWasher, it)
                            },
                            colors = switchColors()
                        )
                    }
                }
            }

            // Error banner
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = uiState.errorMessage!!,
                    fontSize = 13.sp,
                    color = RapidoDangerRed,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Submit Button
            RapidoPrimaryButton(
                text = "Submit Application & KYC",
                onClick = { viewModel.submitRegistration() },
                enabled = uiState.isOnboardingValid && !uiState.isLoading,
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = RapidoCarbonBlack,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SpecialtySelectCard(
    icon: ImageVector,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) RapidoYellowPrimary.copy(alpha = 0.2f) else Color(0xFFF7F7F7))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) RapidoYellowPrimary else Color(0xFFE2E2E2),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) RapidoCarbonBlack else RapidoTextSecondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) RapidoCarbonBlack else RapidoTextSecondary
            )
        }
    }
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RapidoYellowPrimary,
    unfocusedBorderColor = Color(0xFFE0E0E0),
    focusedLabelColor = RapidoCarbonBlack
)

@Composable
private fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = RapidoCarbonBlack,
    checkedTrackColor = RapidoYellowPrimary
)
