package com.washgo.agent.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoDangerRed
import com.washgo.core.designsystem.theme.RapidoYellowPrimary

@Composable
fun CountdownCircularIndicator(
    remainingSeconds: Int,
    totalSeconds: Int = 15,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val progress = (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "countdownProgress"
    )

    val ringColor by animateColorAsState(
        targetValue = if (remainingSeconds <= 5) RapidoDangerRed else RapidoYellowPrimary,
        label = "ringColor"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Background track circle
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(size),
            color = Color(0xFF2C2C2C),
            strokeWidth = 4.5.dp
        )

        // Active animated countdown ring
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(size),
            color = ringColor,
            strokeWidth = 4.5.dp,
            strokeCap = StrokeCap.Round
        )

        // Center badge with remaining seconds
        Box(
            modifier = Modifier
                .size(size - 14.dp)
                .clip(CircleShape)
                .background(RapidoCarbonBlack),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${remainingSeconds}s",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ringColor
            )
        }
    }
}
