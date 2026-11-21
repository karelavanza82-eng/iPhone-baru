package com.example.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.BatteryStatus
import com.example.launcher.HomeWidgetItem
import com.example.launcher.MediaPlaybackInfo
import com.example.launcher.ReminderRecord
import com.example.launcher.WidgetSize
import com.example.launcher.WidgetType
import com.example.theme.IosColors
import com.example.theme.SfProDisplay
import com.example.theme.SfProText

@Composable
fun WidgetGalleryOverlay(
    batteryStatus: BatteryStatus,
    mediaInfo: MediaPlaybackInfo,
    reminders: List<ReminderRecord>,
    onAddWidget: (WidgetType, WidgetSize) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSize by remember { mutableStateOf(WidgetSize.SMALL) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xF51C1C1E))
                .clickable(enabled = false) {}
                .navigationBarsPadding()
                .padding(top = 12.dp, bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(38.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Widget",
                    fontFamily = SfProDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = IosColors.SystemBlue, fontFamily = SfProText, fontSize = 16.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C2C2E))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                WidgetSize.entries.forEach { size ->
                    val active = selectedSize == size
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) Color(0xFF48484A) else Color.Transparent)
                            .clickable { selectedSize = size }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${size.label} (${size.columns}×${size.rows})",
                            fontFamily = SfProText,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(WidgetType.entries) { type ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF2C2C2E))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = type.displayName,
                                fontFamily = SfProText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Button(
                                onClick = { onAddWidget(type, selectedSize) },
                                colors = ButtonDefaults.buttonColors(containerColor = IosColors.SystemBlue),
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("+ Add", fontFamily = SfProText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(if (selectedSize == WidgetSize.SMALL) 156.dp else 300.dp)
                                .height(if (selectedSize == WidgetSize.LARGE) 210.dp else 148.dp)
                        ) {
                            IosWidgetCard(
                                widget = HomeWidgetItem("preview_${type.name}", type, selectedSize),
                                batteryStatus = batteryStatus,
                                mediaInfo = mediaInfo,
                                reminders = reminders,
                                onWidgetClick = { onAddWidget(type, selectedSize) },
                                onMediaPlayPause = {},
                                onMediaNext = {},
                                onCycleSmartStack = {},
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
