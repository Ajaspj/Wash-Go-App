package com.washgo.agent.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val ElectricGreen = Color(0xFF00E676)
private val DarkGreenTrack = Color(0xFF0B2012)
private val NeonBorder = Color(0xFF00FF88)

@Composable
fun SwipeToAcceptButton(
    modifier: Modifier = Modifier,
    text: String = "SWIPE TO ACCEPT WASH",
    isAccepted: Boolean = false,
    onAccept: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(CircleShape)
            .background(DarkGreenTrack)
            .border(2.dp, NeonBorder.copy(alpha = 0.8f), CircleShape),
        contentAlignment = Alignment.CenterStart
    ) {
        val density = LocalDensity.current
        val thumbSizePx = with(density) { 54.dp.toPx() }
        val maxOffsetPx = with(density) { (maxWidth - 58.dp).toPx() }
        val thresholdPx = maxOffsetPx * 0.72f

        // Progress fill gradient behind the track
        val currentProgress = if (maxOffsetPx > 0) (dragOffset.value / maxOffsetPx).coerceIn(0f, 1f) else 0f
        val progressWidth = maxWidth * currentProgress

        Box(
            modifier = Modifier
                .width(progressWidth)
                .height(64.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(ElectricGreen.copy(alpha = 0.25f), ElectricGreen.copy(alpha = 0.55f))
                    )
                )
        )

        // Center track text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = text,
                color = ElectricGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = ">>>",
                color = ElectricGreen.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // Draggable thumb knob
        Box(
            modifier = Modifier
                .offset { IntOffset(dragOffset.value.roundToInt() + 5, 0) }
                .size(54.dp)
                .clip(CircleShape)
                .background(ElectricGreen)
                .pointerInput(isAccepted) {
                    if (isAccepted) return@pointerInput

                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDragEnd = {
                            isDragging = false
                            if (dragOffset.value >= thresholdPx) {
                                coroutineScope.launch {
                                    dragOffset.animateTo(
                                        targetValue = maxOffsetPx,
                                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                    )
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onAccept()
                                }
                            } else {
                                coroutineScope.launch {
                                    dragOffset.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            coroutineScope.launch {
                                val newTarget = (dragOffset.value + dragAmount).coerceIn(0f, maxOffsetPx)
                                dragOffset.snapTo(newTarget)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAccepted) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Slide Arrow",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
