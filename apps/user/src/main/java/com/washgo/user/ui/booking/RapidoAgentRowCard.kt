package com.washgo.user.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoSelectedBorder
import com.washgo.core.designsystem.theme.RapidoSuccessGreen
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoYellowDark
import com.washgo.core.designsystem.theme.RapidoYellowPrimary
import com.washgo.user.data.network.NearbyAgentDto

@Composable
fun RapidoAgentRowCard(
    agent: NearbyAgentDto,
    priceText: String,
    isSelected: Boolean,
    isBookingInProgress: Boolean,
    onSelect: () -> Unit,
    onConfirmBooking: (NearbyAgentDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) RapidoSelectedBorder else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFFDE7) else RapidoSurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Section: Avatar, Details, and ETA Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar Placeholder with Verified Badge
                Box(
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) RapidoYellowPrimary else Color(0xFFEEEEEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = agent.name.take(1).uppercase(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = RapidoCarbonBlack
                        )
                    }

                    // Mini Verified Shield
                    Surface(
                        shape = CircleShape,
                        color = RapidoSurfaceWhite,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Verified Agent",
                            tint = RapidoSuccessGreen,
                            modifier = Modifier.padding(1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Agent Details Column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = agent.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = RapidoTextPrimary
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Star Rating Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFF9C4),
                            modifier = Modifier.border(
                                width = 0.5.dp,
                                color = RapidoYellowDark,
                                shape = RoundedCornerShape(6.dp)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Rating",
                                    tint = RapidoYellowDark,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = String.format("%.1f", agent.rating),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RapidoCarbonBlack
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${String.format("%.1f", agent.distanceKm)} km away",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = RapidoTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // ETA Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RapidoYellowPrimary,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ElectricBolt,
                            contentDescription = "ETA",
                            tint = RapidoCarbonBlack,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = agent.formattedEta,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RapidoCarbonBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent "Confirm Wash Booking" Action Button at the bottom of the card
            Button(
                onClick = { onConfirmBooking(agent) },
                enabled = !isBookingInProgress,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RapidoCarbonBlack,
                    contentColor = RapidoYellowPrimary,
                    disabledContainerColor = Color(0xFF666666)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isBookingInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = RapidoYellowPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dispatching Request...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RapidoYellowPrimary
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WaterDrop,
                            contentDescription = "Confirm Wash",
                            tint = RapidoYellowPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirm Wash Booking • $priceText",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RapidoYellowPrimary
                        )
                    }
                }
            }
        }
    }
}
