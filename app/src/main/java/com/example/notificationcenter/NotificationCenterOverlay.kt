package com.example.notificationcenter

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homescreen.IosStatusBar
import com.example.icons.IosAppIconGraphic
import com.example.launcher.LauncherUiState
import com.example.launcher.NotificationGroup
import com.example.services.NotificationBridge
import com.example.theme.IosColors
import com.example.theme.IosTypography
import com.example.theme.SfProText
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationCenterOverlay(
    state: LauncherUiState,
    onDismiss: () -> Unit,
    onGrantNotificationAccess: () -> Unit,
    onLaunchPackage: (String) -> Unit,
    onToggleFlashlight: () -> Unit,
    onOpenCamera: () -> Unit
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(15_000L)
        }
    }

    val dateStr = remember(nowMillis) {
        SimpleDateFormat("EEE d MMM", Locale.ENGLISH).format(Date(nowMillis))
    }
    val timeStr = remember(nowMillis) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(nowMillis))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66060810))
            .pointerInput(Unit) {
                var drag = 0f
                detectVerticalDragGestures(
                    onDragStart = { drag = 0f },
                    onVerticalDrag = { _, dy -> drag += dy },
                    onDragEnd = { if (drag < -90f) onDismiss() }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            IosStatusBar(
                batteryStatus = state.batteryStatus,
                connectivityStatus = state.connectivityStatus,
                mediaInfo = state.mediaInfo,
                lowPowerMode = state.settings.lowPowerMode,
                showBatteryPercentage = state.settings.showBatteryPercentage,
                isLightContent = true,
                showDynamicIsland = false
            )

            Spacer(Modifier.height(14.dp))

            // Lock Screen style Date & Clock matching video 00:01
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dateStr,
                    style = IosTypography.LockScreenDate.copy(
                        color = Color.White.copy(alpha = 0.92f),
                        shadow = Shadow(Color.Black.copy(alpha = 0.35f), Offset(0f, 2f), 8f)
                    )
                )
                Text(
                    text = timeStr,
                    style = IosTypography.LockScreenClock.copy(
                        color = Color.White.copy(alpha = 0.95f),
                        shadow = Shadow(Color.Black.copy(alpha = 0.35f), Offset(0f, 3f), 12f)
                    )
                )
            }

            Spacer(Modifier.height(18.dp))

            // Real Notification Groups List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (!state.isNotificationAccessGranted) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0x9920222C))
                                .border(0.6.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(22.dp))
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsNone,
                                    contentDescription = null,
                                    tint = IosColors.SystemBlue
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Notification Access",
                                    fontFamily = SfProText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Grant Notification Access so PureOS can display live app notification badges and grouped notifications here.",
                                fontFamily = SfProText,
                                fontSize = 13.5.sp,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                            Button(
                                onClick = onGrantNotificationAccess,
                                colors = ButtonDefaults.buttonColors(containerColor = IosColors.SystemBlue),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Enable Notification Access", fontFamily = SfProText, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else if (state.notificationGroups.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No Notifications",
                                fontFamily = SfProText,
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.70f)
                            )
                        }
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Notification Center",
                                fontFamily = SfProText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.20f))
                                    .clickable { NotificationBridge.clearAll() }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Clear",
                                    fontFamily = SfProText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    items(state.notificationGroups, key = { it.packageName }) { group ->
                        NotificationGroupCard(
                            group = group,
                            nowMillis = nowMillis,
                            onOpenApp = {
                                onDismiss()
                                onLaunchPackage(group.packageName)
                            },
                            onDismissItem = { key -> NotificationBridge.cancelNotification(key) }
                        )
                    }
                }
            }

            // Bottom Flashlight & Camera buttons matching video 00:01
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 34.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (state.flashlightOn) Color.White else Color(0x661C1E26))
                        .border(0.6.dp, Color.White.copy(alpha = 0.24f), CircleShape)
                        .clickable(onClick = onToggleFlashlight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlashlightOn,
                        contentDescription = "Flashlight",
                        tint = if (state.flashlightOn) Color.Black else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(134.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .clickable { onDismiss() }
                        .testTag("notification_center_home_bar")
                )

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0x661C1E26))
                        .border(0.6.dp, Color.White.copy(alpha = 0.24f), CircleShape)
                        .clickable {
                            onDismiss()
                            onOpenCamera()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Camera",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationGroupCard(
    group: NotificationGroup,
    nowMillis: Long,
    onOpenApp: () -> Unit,
    onDismissItem: (String) -> Unit
) {
    val latest = group.items.firstOrNull() ?: return
    val relTime = remember(latest.postTimeMillis, nowMillis) {
        DateUtils.getRelativeTimeSpanString(
            latest.postTimeMillis,
            nowMillis,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xAD22242E))
            .border(0.6.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(22.dp))
            .clickable(onClick = onOpenApp)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(38.dp)) {
                IosAppIconGraphic(
                    iconKey = group.iconKey,
                    fallbackBitmap = null,
                    fallbackTintColor = IosColors.SystemBlue,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = latest.title,
                        fontFamily = SfProText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = relTime,
                        fontFamily = SfProText,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.55f)
                    )
                }
                if (latest.text.isNotBlank()) {
                    Text(
                        text = latest.text,
                        fontFamily = SfProText,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(
                onClick = { onDismissItem(latest.key) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss notification",
                    tint = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        if (group.items.size > 1) {
            Text(
                text = "${group.items.size - 1} more notification${if (group.items.size - 1 > 1) "s" else ""} from ${group.appLabel}",
                fontFamily = SfProText,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 50.dp)
            )
        }
    }
}
