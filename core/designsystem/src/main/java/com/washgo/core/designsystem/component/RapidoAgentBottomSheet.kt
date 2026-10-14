package com.washgo.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.HorizontalDivider
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
import com.washgo.core.designsystem.theme.RapidoDivider
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoTextPrimary
import com.washgo.core.designsystem.theme.RapidoTextSecondary
import com.washgo.core.designsystem.theme.RapidoYellowPrimary
import com.washgo.core.model.AgentCandidate

@Composable
fun RapidoAgentBottomSheet(
    agents: List<AgentCandidate>,
    selectedAgent: AgentCandidate?,
    servicePriceText: String,
    onAgentSelected: (AgentCandidate) -> Unit,
    onConfirmBooking: (AgentCandidate) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = RapidoSurfaceWhite,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFDDDDDD))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sheet Title & Count Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Nearby Washers Found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RapidoTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(RapidoYellowPrimary, RoundedCornerShape(10.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${agents.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RapidoCarbonBlack
                            )
                        }
                    }
                    Text(
                        text = "Sorted by fastest arrival time to your vehicle",
                        fontSize = 12.sp,
                        color = RapidoTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = RapidoDivider)
            Spacer(modifier = Modifier.height(12.dp))

            // Agents Candidate List
            if (agents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active washers within search radius.\nTry expanding search or check back shortly.",
                        fontSize = 14.sp,
                        color = RapidoTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    items(agents, key = { it.agentId }) { agent ->
                        val isSelected = selectedAgent?.agentId == agent.agentId
                        RapidoAgentCard(
                            agent = agent,
                            isSelected = isSelected,
                            onSelect = { onAgentSelected(agent) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            val effectiveAgent = selectedAgent ?: agents.firstOrNull()
            val buttonText = if (effectiveAgent != null) {
                "Book ${effectiveAgent.displayName} • $servicePriceText"
            } else {
                "Select a Washer to Book"
            }

            RapidoPrimaryButton(
                text = buttonText,
                onClick = {
                    if (effectiveAgent != null) {
                        onConfirmBooking(effectiveAgent)
                    }
                },
                enabled = effectiveAgent != null,
                isLoading = isLoading,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = "Book Wash",
                        tint = RapidoCarbonBlack
                    )
                }
            )
        }
    }
}
