package com.example.controlcenter

import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenLockRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.IosBuiltinAppId
import com.example.launcher.LauncherUiState
import com.example.theme.IosColors
import com.example.theme.SfProText

@Composable
fun ControlCenterOverlay(
    state: LauncherUiState,
    onDismiss: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onToggleFocusMode: () -> Unit,
    onToggleRotationLock: () -> Unit,
    onToggleLowPower: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onMediaKey: (Int) -> Unit,
    onOpenSystemSettings: (String) -> Unit,
    onOpenCamera: () -> Unit,
    onLaunchBuiltin: (IosBuiltinAppId) -> Unit
) {
    val cardBg = Color(0x6B242630)
    val cardBorder = Color.White.copy(alpha = 0.22f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x880B0D14))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top bar matching video 00:00
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .clickable {
                            onDismiss()
                            onLaunchBuiltin(IosBuiltinAppId.SETTINGS)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(18.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.connectivityStatus.carrierName,
                        fontFamily = SfProText,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "${state.batteryStatus.level}%",
                        fontFamily = SfProText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.70f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .clickable { onDismiss() }
                        .testTag("control_center_close"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Control Center", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // Row 1: Connectivity 2x2 + Now Playing 2x2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Connectivity 2x2 card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(cardBg)
                        .border(0.6.dp, cardBorder, RoundedCornerShape(28.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ConnectivityCircle(
                                icon = Icons.Filled.AirplanemodeActive,
                                label = "Airplane Mode",
                                active = state.connectivityStatus.airplaneModeOn,
                                activeColor = IosColors.SystemOrange,
                                onClick = { onOpenSystemSettings(Settings.ACTION_AIRPLANE_MODE_SETTINGS) }
                            )
                            ConnectivityCircle(
                                icon = Icons.Filled.CellTower,
                                label = "Cellular",
                                active = state.connectivityStatus.cellularConnected,
                                activeColor = IosColors.SystemGreen,
                                onClick = { onOpenSystemSettings(Settings.ACTION_DATA_ROAMING_SETTINGS) }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ConnectivityCircle(
                                icon = Icons.Filled.Wifi,
                                label = "Wi-Fi",
                                active = state.connectivityStatus.wifiConnected,
                                activeColor = IosColors.SystemBlue,
                                onClick = { onOpenSystemSettings(Settings.ACTION_WIFI_SETTINGS) }
                            )
                            ConnectivityCircle(
                                icon = Icons.Filled.Bluetooth,
                                label = "Bluetooth",
                                active = state.connectivityStatus.bluetoothEnabled,
                                activeColor = IosColors.SystemBlue,
                                onClick = { onOpenSystemSettings(Settings.ACTION_BLUETOOTH_SETTINGS) }
                            )
                        }
                    }
                }

                // Now Playing 2x2 card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(cardBg)
                        .border(0.6.dp, cardBorder, RoundedCornerShape(28.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(4.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.mediaInfo.title,
                            fontFamily = SfProText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (state.mediaInfo.artist.isNotBlank()) {
                            Text(
                                text = state.mediaInfo.artist,
                                fontFamily = SfProText,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = Color.White.copy(alpha = 0.7f))
                        }
                        IconButton(
                            onClick = { onMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (state.mediaInfo.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play or Pause",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        IconButton(
                            onClick = { onMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // Row 2: Left (Rotation Lock + Mirroring + Focus Pill), Right (Brightness & Volume Sliders)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp)
                    .clickable(enabled = false) {},
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ControlRoundToggle(
                            icon = Icons.Filled.ScreenLockRotation,
                            label = "Rotation Lock",
                            active = state.settings.rotationLockOn,
                            activeBg = Color.White,
                            activeTint = IosColors.SystemRed,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            onClick = onToggleRotationLock,
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                        )
                        ControlRoundToggle(
                            icon = Icons.Filled.Cast,
                            label = "Screen Mirroring",
                            active = false,
                            activeBg = Color.White,
                            activeTint = IosColors.SystemBlue,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            onClick = { onOpenSystemSettings(Settings.ACTION_CAST_SETTINGS) },
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                        )
                    }

                    // Focus Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clip(RoundedCornerShape(35.dp))
                            .background(if (state.settings.focusModeOn) Color.White else cardBg)
                            .border(0.6.dp, cardBorder, RoundedCornerShape(35.dp))
                            .clickable(onClick = onToggleFocusMode)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (state.settings.focusModeOn) IosColors.SystemIndigo
                                    else Color.White.copy(alpha = 0.16f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DarkMode,
                                contentDescription = "Focus",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (state.settings.focusModeOn) "Do Not Disturb" else "Focus",
                            fontFamily = SfProText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = if (state.settings.focusModeOn) Color.Black else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Vertical Brightness & Volume Sliders
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    VerticalPillSlider(
                        fraction = state.brightnessFraction,
                        icon = Icons.Filled.WbSunny,
                        contentDescription = "Brightness Slider",
                        onFractionChange = onBrightnessChange,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("brightness_slider")
                    )
                    VerticalPillSlider(
                        fraction = state.volumeFraction,
                        icon = Icons.Filled.VolumeUp,
                        contentDescription = "Volume Slider",
                        onFractionChange = onVolumeChange,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("volume_slider")
                    )
                }
            }

            // Bottom Quick Action Circle Grid (4 columns)
            val quickButtons = listOf(
                QuickControlItem(
                    icon = Icons.Filled.FlashlightOn,
                    label = "Flashlight",
                    active = state.flashlightOn,
                    activeTint = Color(0xFF0A84FF),
                    onClick = onToggleFlashlight
                ),
                QuickControlItem(
                    icon = Icons.Filled.Timer,
                    label = "Clock",
                    active = false,
                    activeTint = IosColors.SystemOrange,
                    onClick = {
                        onDismiss()
                        onLaunchBuiltin(IosBuiltinAppId.CLOCK)
                    }
                ),
                QuickControlItem(
                    icon = Icons.Filled.Calculate,
                    label = "Calculator",
                    active = false,
                    activeTint = Color.Black,
                    onClick = {
                        onDismiss()
                        onLaunchBuiltin(IosBuiltinAppId.CALCULATOR)
                    }
                ),
                QuickControlItem(
                    icon = Icons.Filled.CameraAlt,
                    label = "Camera",
                    active = false,
                    activeTint = Color.Black,
                    onClick = {
                        onDismiss()
                        onOpenCamera()
                    }
                ),
                QuickControlItem(
                    icon = Icons.Filled.BatteryChargingFull,
                    label = "Low Power",
                    active = state.settings.lowPowerMode,
                    activeTint = IosColors.SystemYellow,
                    onClick = onToggleLowPower
                ),
                QuickControlItem(
                    icon = Icons.Filled.Brightness6,
                    label = "Dark Mode",
                    active = state.settings.darkMode,
                    activeTint = IosColors.SystemIndigo,
                    onClick = onToggleDarkMode
                ),
                QuickControlItem(
                    icon = Icons.Filled.EditNote,
                    label = "Notes",
                    active = false,
                    activeTint = IosColors.SystemOrange,
                    onClick = {
                        onDismiss()
                        onLaunchBuiltin(IosBuiltinAppId.NOTES)
                    }
                ),
                QuickControlItem(
                    icon = Icons.Filled.Settings,
                    label = "Settings",
                    active = false,
                    activeTint = IosColors.SystemBlue,
                    onClick = {
                        onDismiss()
                        onLaunchBuiltin(IosBuiltinAppId.SETTINGS)
                    }
                )
            )

            quickButtons.chunked(4).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) {},
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rowItems.forEach { item ->
                        ControlRoundToggle(
                            icon = item.icon,
                            label = item.label,
                            active = item.active,
                            activeBg = Color.White,
                            activeTint = item.activeTint,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            onClick = item.onClick,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

private data class QuickControlItem(
    val icon: ImageVector,
    val label: String,
    val active: Boolean,
    val activeTint: Color,
    val onClick: () -> Unit
)

@Composable
private fun ConnectivityCircle(
    icon: ImageVector,
    label: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (active) activeColor else Color.White.copy(alpha = 0.16f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ControlRoundToggle(
    icon: ImageVector,
    label: String,
    active: Boolean,
    activeBg: Color,
    activeTint: Color,
    cardBg: Color,
    cardBorder: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (active) activeBg else cardBg)
            .border(0.6.dp, cardBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) activeTint else Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun VerticalPillSlider(
    fraction: Float,
    icon: ImageVector,
    contentDescription: String,
    onFractionChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val clamped = fraction.coerceIn(0f, 1f)
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x6B242630))
            .border(0.6.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    val h = size.height.toFloat().coerceAtLeast(1f)
                    val newFraction = 1f - (change.position.y / h).coerceIn(0f, 1f)
                    onFractionChange(newFraction)
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(clamped.coerceAtLeast(0.05f))
                .background(Color.White.copy(alpha = 0.90f))
        )
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (clamped > 0.24f) Color(0xFF1C1C1E) else Color.White,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(24.dp)
        )
    }
}
