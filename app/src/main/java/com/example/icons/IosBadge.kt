package com.example.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.theme.IosColors
import com.example.theme.SfProText
import java.text.NumberFormat
import java.util.Locale

fun formatIosBadgeCount(count: Int): String {
    return when {
        count <= 0 -> ""
        count > 9999 -> "999+"
        count >= 1000 -> NumberFormat.getNumberInstance(Locale.US).format(count)
        else -> count.toString()
    }
}

@Composable
fun IosNotificationBadge(
    count: Int,
    modifier: Modifier = Modifier,
    badgeHeight: Dp = 20.dp
) {
    if (count <= 0) return
    val text = formatIosBadgeCount(count)
    val horizontalPadding = if (text.length == 1) 0.dp else 6.dp
    Box(
        modifier = modifier
            .height(badgeHeight)
            .defaultMinSize(minWidth = badgeHeight)
            .shadow(elevation = 3.dp, shape = CircleShape, clip = false)
            .background(color = IosColors.SystemRed, shape = CircleShape)
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = SfProText,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (text.length >= 4) 10.sp else 12.sp,
            lineHeight = 13.sp,
            maxLines = 1
        )
    }
}
