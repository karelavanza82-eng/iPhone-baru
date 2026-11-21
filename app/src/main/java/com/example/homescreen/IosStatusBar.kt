package com.example.homescreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.BatteryStatus
import com.example.launcher.ConnectivityStatus
import com.example.launcher.MediaPlaybackInfo
import com.example.theme.IosColors
import com.example.theme.SfProText
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IosStatusBar(
    batteryStatus: BatteryStatus,
    connectivityStatus: ConnectivityStatus,
    mediaInfo: MediaPlaybackInfo,
    lowPowerMode: Boolean,
    showBatteryPercentage: Boolean,
    isLightContent: Boolean = true,
    showDynamicIsland: Boolean = true,
    onTapLeft: () -> Unit = {},
    onTapRight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var timeText by remember { mutableStateOf(formatStatusTime()) }
    LaunchedEffect(Unit) {
        while (true) {
            timeText = formatStatusTime()
            delay(15_000L)
        }
    }

    val fgColor = if (isLightContent) Color.White else Color.Black

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Time
        Box(
            modifier = Modifier
                .width(72.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTapLeft
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = timeText,
                fontFamily = SfProText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = fgColor,
                letterSpacing = (-0.2).sp
            )
        }

        // Center: Dynamic Island
        if (showDynamicIsland) {
            Box(
                modifier = Modifier
                    .width(if (mediaInfo.isPlaying) 136.dp else 104.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (mediaInfo.isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(IosColors.SystemPink)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            listOf(8.dp, 13.dp, 10.dp, 6.dp).forEach { barH ->
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(barH)
                                        .clip(CircleShape)
                                        .background(IosColors.SystemGreen)
                                )
                            }
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF141824))
                        )
                    }
                }
            }
        }

        // Right: Signal + Wi-Fi + Battery Pill
        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTapRight
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (connectivityStatus.airplaneModeOn) {
                Icon(
                    imageVector = Icons.Filled.AirplanemodeActive,
                    contentDescription = "Airplane Mode",
                    tint = fgColor,
                    modifier = Modifier.size(15.dp)
                )
            } else {
                SignalBarsIcon(
                    activeBars = if (connectivityStatus.cellularConnected) 4 else 2,
                    color = fgColor
                )
                if (connectivityStatus.wifiConnected) {
                    Icon(
                        imageVector = Icons.Filled.Wifi,
                        contentDescription = "Wi-Fi Connected",
                        tint = fgColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            IosBatteryPill(
                level = batteryStatus.level,
                isCharging = batteryStatus.isCharging,
                lowPowerMode = lowPowerMode,
                showPercentage = showBatteryPercentage,
                fgColor = fgColor
            )
        }
    }
}

@Composable
private fun SignalBarsIcon(activeBars: Int, color: Color) {
    Canvas(modifier = Modifier.size(width = 16.dp, height = 12.dp)) {
        val barWidth = size.width / 5.5f
        val gap = barWidth * 0.45f
        val heights = listOf(0.35f, 0.55f, 0.78f, 1.0f)
        heights.forEachIndexed { idx, frac ->
            val h = size.height * frac
            drawRoundRect(
                color = if (idx < activeBars) color else color.copy(alpha = 0.35f),
                topLeft = Offset(idx * (barWidth + gap), size.height - h),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
            )
        }
    }
}

@Composable
private fun IosBatteryPill(
    level: Int,
    isCharging: Boolean,
    lowPowerMode: Boolean,
    showPercentage: Boolean,
    fgColor: Color
) {
    val fillColor = when {
        isCharging -> IosColors.SystemGreen
        lowPowerMode -> IosColors.SystemYellow
        level <= 20 -> IosColors.SystemRed
        else -> fgColor
    }
    val textColor = when {
        isCharging || lowPowerMode || level <= 20 -> Color.Black
        fgColor == Color.White -> Color.Black
        else -> Color.White
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(27.dp)
                .height(13.5.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(fgColor.copy(alpha = 0.32f)),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((level / 100f).coerceIn(0.08f, 1f))
                    .height(13.5.dp)
                    .background(fillColor)
            )
            if (showPercentage) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$level",
                        fontFamily = SfProText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        lineHeight = 10.sp,
                        color = textColor
                    )
                    if (isCharging) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "Charging",
                            tint = textColor,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.width(1.5.dp))
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                .background(fgColor.copy(alpha = 0.55f))
        )
    }
}

private fun formatStatusTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
