package com.washgo.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.core.designsystem.theme.RapidoBorder
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoSelectedBorder
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoYellowDark
import com.washgo.core.designsystem.theme.RapidoYellowPrimary
import com.washgo.core.model.AgentCandidate

@Composable
fun RapidoAgentCard(
    agent: AgentCandidate,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) RapidoSelectedBorder else RapidoBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFFDE7) else RapidoSurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Agent Avatar / Initial Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) RapidoYellowPrimary else Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = agent.displayName.take(1).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RapidoCarbonBlack
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Agent Details Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = agent.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RapidoTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RapidoRatingBadge(
                        rating = agent.averageRating,
                        reviewsCount = agent.totalCompletedWashes
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = agent.vehicleSpecialty.label,
                        fontSize = 12.sp,
                        color = RapidoTextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${agent.formattedDistance} away",
                        fontSize = 12.sp,
                        color = RapidoTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ETA Chip & Selection indicator
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // ETA Chip
                Row(
                    modifier = Modifier
                        .background(RapidoYellowPrimary, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlashOn,
                        contentDescription = "Fast ETA",
                        tint = RapidoCarbonBlack,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = agent.formattedEta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RapidoCarbonBlack
                    )
                }

                if (isSelected) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = RapidoYellowDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
