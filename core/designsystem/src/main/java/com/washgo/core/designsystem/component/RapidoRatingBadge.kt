package com.washgo.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoStarGold
import com.washgo.core.designsystem.theme.RapidoTextSecondary

@Composable
fun RapidoRatingBadge(
    rating: Double,
    reviewsCount: Int? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFFFF9E6),
    borderColor: Color = Color(0xFFFFE082)
) {
    Row(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Rating",
            tint = RapidoStarGold,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "%.1f".format(rating),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = RapidoCarbonBlack
        )
        if (reviewsCount != null && reviewsCount > 0) {
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "($reviewsCount)",
                fontSize = 11.sp,
                color = RapidoTextSecondary
            )
        }
    }
}
