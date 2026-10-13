package com.washgo.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.washgo.core.designsystem.theme.RapidoBorder
import com.washgo.core.designsystem.theme.RapidoCarbonBlack
import com.washgo.core.designsystem.theme.RapidoSelectedBorder
import com.washgo.core.designsystem.theme.RapidoSurfaceWhite

@Composable
fun RapidoOtpInput(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    otpLength: Int = 4
) {
    BasicTextField(
        value = otpValue,
        onValueChange = { newValue ->
            if (newValue.length <= otpLength && newValue.all { it.isDigit() }) {
                onOtpChange(newValue)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                for (i in 0 until otpLength) {
                    val char = otpValue.getOrNull(i)?.toString() ?: ""
                    val isFocused = otpValue.length == i

                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(64.dp)
                            .background(RapidoSurfaceWhite, RoundedCornerShape(12.dp))
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) RapidoSelectedBorder else RapidoBorder,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RapidoCarbonBlack,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}
