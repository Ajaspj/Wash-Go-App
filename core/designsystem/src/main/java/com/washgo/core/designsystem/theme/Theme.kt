package com.washgo.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val RapidoShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

private val RapidoColorScheme = lightColorScheme(
    primary = RapidoYellowPrimary,
    onPrimary = RapidoCarbonBlack,
    primaryContainer = RapidoYellowLight,
    onPrimaryContainer = RapidoCarbonBlack,
    secondary = RapidoCarbonBlack,
    onSecondary = RapidoSurfaceWhite,
    secondaryContainer = RapidoSurfaceLight,
    onSecondaryContainer = RapidoTextPrimary,
    background = RapidoSurfaceLight,
    onBackground = RapidoTextPrimary,
    surface = RapidoSurfaceWhite,
    onSurface = RapidoTextPrimary,
    surfaceVariant = RapidoSurfaceLight,
    onSurfaceVariant = RapidoTextSecondary,
    outline = RapidoBorder,
    error = RapidoDangerRed,
    onError = RapidoSurfaceWhite
)

@Composable
fun RapidoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RapidoColorScheme,
        shapes = RapidoShapes,
        content = content
    )
}
