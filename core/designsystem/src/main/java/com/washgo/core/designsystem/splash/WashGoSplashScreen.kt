package com.washgo.core.designsystem.splash

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalCarWash
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoCarbonDark
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite
import com.washgo.core.designsystem.theme.RapidoYellowDark
import com.washgo.core.designsystem.theme.RapidoYellowLight
import com.washgo.core.designsystem.theme.RapidoYellowPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Styling variant for the Wash-Go application splash screen.
 */
enum class SplashVariant {
    CUSTOMER,
    AGENT
}

/**
 * Premium animated splash screen for the Wash-Go platform.
 *
 * Designed with smooth mobility-inspired motion, subtle washing/water-droplet ripples,
 * and specular shine sweeps while strictly honoring brand colors and typography.
 *
 * @param variant Visual aesthetic ([SplashVariant.CUSTOMER] or [SplashVariant.AGENT])
 * @param onSplashComplete Callback invoked when the ~1.2s branded intro completes
 */
@Composable
fun WashGoSplashScreen(
    variant: SplashVariant,
    modifier: Modifier = Modifier,
    onSplashComplete: () -> Unit
) {
    val context = LocalContext.current

    // Check if user has reduced-motion enabled in system accessibility settings
    val isReducedMotion = remember {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }

    // Colors customized per variant
    val backgroundColor = when (variant) {
        SplashVariant.CUSTOMER -> RapidoYellowPrimary
        SplashVariant.AGENT -> RapidoCarbonBlack
    }

    val emblemBgColor = when (variant) {
        SplashVariant.CUSTOMER -> RapidoCarbonBlack
        SplashVariant.AGENT -> RapidoYellowPrimary
    }

    val emblemIconColor = when (variant) {
        SplashVariant.CUSTOMER -> RapidoYellowPrimary
        SplashVariant.AGENT -> RapidoCarbonBlack
    }

    val primaryTextColor = when (variant) {
        SplashVariant.CUSTOMER -> RapidoCarbonBlack
        SplashVariant.AGENT -> RapidoSurfaceWhite
    }

    val secondaryTextColor = when (variant) {
        SplashVariant.CUSTOMER -> RapidoCarbonDark
        SplashVariant.AGENT -> RapidoYellowPrimary
    }

    val rippleColor = when (variant) {
        SplashVariant.CUSTOMER -> RapidoCarbonBlack.copy(alpha = 0.12f)
        SplashVariant.AGENT -> RapidoYellowPrimary.copy(alpha = 0.22f)
    }

    // Animation States
    val emblemScale = remember { Animatable(if (isReducedMotion) 1f else 0.65f) }
    val emblemAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val rippleScale1 = remember { Animatable(if (isReducedMotion) 1f else 0.8f) }
    val rippleAlpha1 = remember { Animatable(if (isReducedMotion) 0f else 0.6f) }
    val rippleScale2 = remember { Animatable(if (isReducedMotion) 1f else 0.8f) }
    val rippleAlpha2 = remember { Animatable(if (isReducedMotion) 0f else 0.4f) }
    val textOffsetY = remember { Animatable(if (isReducedMotion) 0f else 24f) }
    val textAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val shineOffset = remember { Animatable(if (isReducedMotion) 1f else -0.3f) }

    // Specular shine infinite sweep
    val infiniteTransition = rememberInfiniteTransition(label = "ShineTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    LaunchedEffect(Unit) {
        if (isReducedMotion) {
            delay(400)
            onSplashComplete()
            return@LaunchedEffect
        }

        // 1. Emblem pop-in with spring physics
        launch {
            emblemScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            emblemAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }

        // 2. Concentric water wave ripple pulses
        delay(150)
        launch {
            rippleScale1.animateTo(
                targetValue = 1.6f,
                animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
            )
        }
        launch {
            rippleAlpha1.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 750, easing = LinearEasing)
            )
        }

        delay(120)
        launch {
            rippleScale2.animateTo(
                targetValue = 1.9f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
        }
        launch {
            rippleAlpha2.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800, easing = LinearEasing)
            )
        }

        // 3. Specular shine sweep
        launch {
            delay(300)
            shineOffset.animateTo(
                targetValue = 1.3f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
        }

        // 4. Staggered brand typography reveal
        delay(100)
        launch {
            textOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }

        // 5. Total branding display hold (~1.2s), then complete smoothly
        delay(850)
        onSplashComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient radial background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height / 2.3f)
            val glowColor = if (variant == SplashVariant.CUSTOMER) {
                RapidoYellowLight.copy(alpha = 0.5f)
            } else {
                RapidoCarbonDark.copy(alpha = 0.7f)
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = centerOffset,
                    radius = size.minDimension * 0.75f
                ),
                radius = size.minDimension * 0.75f,
                center = centerOffset
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Emblem with concentric water ripples
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(170.dp)
            ) {
                // Outer Water Ripple 2
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(rippleScale2.value)
                        .alpha(rippleAlpha2.value)
                        .clip(CircleShape)
                        .background(rippleColor)
                )

                // Outer Water Ripple 1
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(rippleScale1.value)
                        .alpha(rippleAlpha1.value)
                        .clip(CircleShape)
                        .background(rippleColor)
                )

                // Central Elevated Brand Shield
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(emblemScale.value)
                        .alpha(emblemAlpha.value)
                        .clip(CircleShape)
                        .background(emblemBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    // Central Brand Icon
                    if (variant == SplashVariant.CUSTOMER) {
                        Icon(
                            imageVector = Icons.Default.LocalCarWash,
                            contentDescription = "Wash-Go Logo",
                            tint = emblemIconColor,
                            modifier = Modifier.size(54.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Wash-Go Partner Logo",
                            tint = emblemIconColor,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    // Specular light sweep overlay
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val progress = shineOffset.value
                        val startX = size.width * progress
                        val brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.28f),
                                Color.Transparent
                            ),
                            start = Offset(startX - 40f, 0f),
                            end = Offset(startX + 40f, size.height)
                        )
                        drawCircle(brush = brush)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Staggered Typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset { IntOffset(x = 0, y = textOffsetY.value.toInt()) }
                    .alpha(textAlpha.value)
            ) {
                Text(
                    text = if (variant == SplashVariant.CUSTOMER) "WASH-GO" else "WASH-GO PARTNER",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (variant == SplashVariant.CUSTOMER) 2.5.sp else 1.5.sp,
                    color = primaryTextColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (variant == SplashVariant.CUSTOMER) {
                        "DOORSTEP DETAILING"
                    } else {
                        "PROFESSIONAL DETAILING DISPATCH"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.8.sp,
                    color = secondaryTextColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (variant == SplashVariant.CUSTOMER) {
                        "Vehicle care delivered in under 15 minutes"
                    } else {
                        "High-demand on-demand partner network"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (variant == SplashVariant.CUSTOMER) RapidoCarbonDark.copy(alpha = 0.75f) else RapidoSurfaceWhite.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom Brand Accent: Subtle animated progress pill
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp)
                .alpha(pulseAlpha),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dotColor = if (variant == SplashVariant.CUSTOMER) {
                RapidoCarbonBlack.copy(alpha = 0.45f)
            } else {
                RapidoYellowPrimary.copy(alpha = 0.7f)
            }

            Box(
                modifier = Modifier
                    .size(width = 24.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(dotColor)
            )
            Box(
                modifier = Modifier
                    .size(width = 8.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(dotColor.copy(alpha = 0.5f))
            )
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(dotColor.copy(alpha = 0.3f))
            )
        }
    }
}
