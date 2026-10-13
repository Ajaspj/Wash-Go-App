package com.washgo.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.washgo.core.designsystem.theme.RapidoBorder
import com.washgo.core.designsystem.theme.RapidoStarGold

@Composable
fun RapidoStarRatingBar(
    rating: Int,
    onRatingChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    starSize: Dp = 24.dp,
    activeColor: Color = RapidoStarGold,
    inactiveColor: Color = RapidoBorder
) {
    Row(modifier = modifier) {
        for (i in 1..maxStars) {
            val isFilled = i <= rating
            val icon = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarOutline
            val tint = if (isFilled) activeColor else inactiveColor

            val iconModifier = Modifier
                .size(starSize)
                .then(
                    if (onRatingChanged != null) {
                        Modifier.clickable { onRatingChanged(i) }
                    } else {
                        Modifier
                    }
                )

            Icon(
                imageVector = icon,
                contentDescription = "Star $i",
                tint = tint,
                modifier = iconModifier
            )
        }
    }
}
