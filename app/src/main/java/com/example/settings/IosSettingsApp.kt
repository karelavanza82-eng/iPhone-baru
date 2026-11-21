package com.example.settings

import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.icons.IosNotificationBadge
import com.example.launcher.LauncherUiState
import com.example.launcher.WallpaperPreset
import com.example.theme.IosColors
import com.example.theme.SfProDisplay
import com.example.theme.SfProText

private enum class SettingsDestination(val title: String) {
    ROOT("Settings"),
    GENERAL("General"),
    ABOUT("About"),
    BATTERY("Battery"),
    DISPLAY("Display & Brightness"),
    WALLPAPER("Wallpaper"),
    HOME_SCREEN("Home Screen")
}

@Composable
fun IosSettingsApp(
    state: LauncherUiState,
    onSetDarkMode: (Boolean) -> Unit,
    onSetBatteryPercent: (Boolean) -> Unit,
    onSetLowPower: (Boolean) -> Unit,
    onSetShowBadges: (Boolean) -> Unit,
    onSetShowLabels: (Boolean) -> Unit,
    onSetWallpaperPreset: (WallpaperPreset) -> Unit,
    onSetCustomWallpaperUri: (String) -> Unit,
    onSetProfileName: (String) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onResetLayout: () -> Unit,
    onOpenHomeChooser: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onOpenSystemSettings: (String) -> Unit,
    onOpenCamera: () -> Unit
) {
    var navStack by remember { mutableStateOf(listOf(SettingsDestination.ROOT)) }
    val current = navStack.last()
    var searchQuery by remember { mutableStateOf("") }
    var showNameDialog by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onSetCustomWallpaperUri(uri.toString())
        }
    }

    BackHandler(enabled = navStack.size > 1) {
        navStack = navStack.dropLast(1)
    }

    val isDark = state.settings.darkMode
    val bgColor = if (isDark) IosColors.DarkBackground else IosColors.LightBackground
    val cardColor = if (isDark) IosColors.DarkSecondaryBackground else IosColors.LightSecondaryBackground
    val dividerColor = if (isDark) IosColors.DarkSeparator else IosColors.LightSeparator
    val textPrimary = if (isDark) Color.White else Color.Black
    val textSecondary = if (isDark) IosColors.LabelSecondaryDark else IosColors.LabelSecondaryLight

    fun push(dest: SettingsDestination) {
        navStack = navStack + dest
    }

    fun pop() {
        if (navStack.size > 1) navStack = navStack.dropLast(1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // iOS Top Navigation Header
        if (current != SettingsDestination.ROOT) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { pop() }
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = IosColors.SystemBlue,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = navStack[navStack.lastIndex - 1].title,
                        fontFamily = SfProText,
                        fontSize = 17.sp,
                        color = IosColors.SystemBlue
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = current.title,
                    fontFamily = SfProText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = textPrimary
                )
                Spacer(Modifier.weight(1.3f))
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            when (current) {
                SettingsDestination.ROOT -> {
                    item {
                        Text(
                            text = "Settings",
                            fontFamily = SfProDisplay,
                            fontWeight = FontWeight.Bold,
                            fontSize = 34.sp,
                            color = textPrimary,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                        )
                    }

                    // Search Bar
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) IosColors.DarkSecondaryBackground else IosColors.LightTertiaryBackground)
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search Settings",
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text("Search", fontFamily = SfProText, fontSize = 16.sp, color = textSecondary)
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    cursorBrush = SolidColor(IosColors.SystemBlue),
                                    textStyle = TextStyle(fontFamily = SfProText, fontSize = 16.sp, color = textPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Profile Card matching video 00:03
                    if (searchQuery.isBlank()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cardColor)
                                    .clickable { showNameDialog = true }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(IosColors.SystemBlue.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = "Profile",
                                        tint = IosColors.SystemBlue,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = state.settings.profileName,
                                        fontFamily = SfProDisplay,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 19.sp,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Launcher Profile, Wallpaper & Device",
                                        fontFamily = SfProText,
                                        fontSize = 13.sp,
                                        color = textSecondary
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = textSecondary
                                )
                            }
                        }

                        // Setup status card (Default Launcher & Notification Access)
                        if (!state.isDefaultLauncher || !state.isNotificationAccessGranted) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(cardColor)
                                ) {
                                    if (!state.isDefaultLauncher) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenHomeChooser() }
                                                .padding(horizontal = 16.dp, vertical = 13.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Set PureOS as Default Launcher",
                                                fontFamily = SfProText,
                                                fontSize = 16.sp,
                                                color = textPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IosNotificationBadge(count = 1)
                                            Spacer(Modifier.width(6.dp))
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = textSecondary)
                                        }
                                    }
                                    if (!state.isDefaultLauncher && !state.isNotificationAccessGranted) {
                                        HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                                    }
                                    if (!state.isNotificationAccessGranted) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenNotificationAccess() }
                                                .padding(horizontal = 16.dp, vertical = 13.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Enable Notification Badges",
                                                fontFamily = SfProText,
                                                fontSize = 16.sp,
                                                color = textPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IosNotificationBadge(count = 1)
                                            Spacer(Modifier.width(6.dp))
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = textSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Connectivity & Battery Group
                    item {
                        val rows = listOf(
                            SettingsRowData(
                                title = "Airplane Mode",
                                icon = Icons.Filled.AirplanemodeActive,
                                iconBg = IosColors.SystemOrange,
                                trailingText = if (state.connectivityStatus.airplaneModeOn) "On" else "Off",
                                onClick = { onOpenSystemSettings(Settings.ACTION_AIRPLANE_MODE_SETTINGS) }
                            ),
                            SettingsRowData(
                                title = "Wi-Fi",
                                icon = Icons.Filled.Wifi,
                                iconBg = IosColors.SystemBlue,
                                trailingText = state.connectivityStatus.wifiSsid,
                                onClick = { onOpenSystemSettings(Settings.ACTION_WIFI_SETTINGS) }
                            ),
                            SettingsRowData(
                                title = "Bluetooth",
                                icon = Icons.Filled.Bluetooth,
                                iconBg = IosColors.SystemBlue,
                                trailingText = if (state.connectivityStatus.bluetoothEnabled) "On" else "Off",
                                onClick = { onOpenSystemSettings(Settings.ACTION_BLUETOOTH_SETTINGS) }
                            ),
                            SettingsRowData(
                                title = "Cellular",
                                icon = Icons.Filled.CellTower,
                                iconBg = IosColors.SystemGreen,
                                trailingText = state.connectivityStatus.carrierName,
                                onClick = { onOpenSystemSettings(Settings.ACTION_DATA_ROAMING_SETTINGS) }
                            ),
                            SettingsRowData(
                                title = "Personal Hotspot",
                                icon = Icons.Filled.WifiTethering,
                                iconBg = IosColors.SystemGreen,
                                trailingText = "Off",
                                onClick = { onOpenSystemSettings(Settings.ACTION_WIRELESS_SETTINGS) }
                            ),
                            SettingsRowData(
                                title = "Battery",
                                icon = Icons.Filled.BatteryChargingFull,
                                iconBg = IosColors.SystemGreen,
                                trailingText = "${state.batteryStatus.level}%",
                                onClick = { push(SettingsDestination.BATTERY) }
                            )
                        ).filter { searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) }

                        if (rows.isNotEmpty()) {
                            SettingsGroupCard(rows, cardColor, dividerColor, textPrimary, textSecondary)
                        }
                    }

                    // General, Display, Wallpaper, Home Screen, Notifications, Camera Group
                    item {
                        val rows = listOf(
                            SettingsRowData(
                                title = "General",
                                icon = Icons.Filled.Settings,
                                iconBg = Color(0xFF8E8E93),
                                onClick = { push(SettingsDestination.GENERAL) }
                            ),
                            SettingsRowData(
                                title = "Accessibility",
                                icon = Icons.Filled.AccessibilityNew,
                                iconBg = IosColors.SystemBlue,
                                onClick = { onOpenSystemSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS) }
                            ),
                            SettingsRowData(
                                title = "Display & Brightness",
                                icon = Icons.Filled.WbSunny,
                                iconBg = IosColors.SystemBlue,
                                trailingText = if (isDark) "Dark" else "Light",
                                onClick = { push(SettingsDestination.DISPLAY) }
                            ),
                            SettingsRowData(
                                title = "Wallpaper",
                                icon = Icons.Filled.Wallpaper,
                                iconBg = IosColors.SystemTeal,
                                trailingText = state.settings.wallpaperPreset.title,
                                onClick = { push(SettingsDestination.WALLPAPER) }
                            ),
                            SettingsRowData(
                                title = "Home Screen & App Library",
                                icon = Icons.Filled.Home,
                                iconBg = IosColors.SystemIndigo,
                                onClick = { push(SettingsDestination.HOME_SCREEN) }
                            ),
                            SettingsRowData(
                                title = "Notifications",
                                icon = Icons.Filled.Notifications,
                                iconBg = IosColors.SystemRed,
                                trailingText = if (state.isNotificationAccessGranted) "Enabled" else "Off",
                                onClick = { onOpenNotificationAccess() }
                            ),
                            SettingsRowData(
                                title = "Camera",
                                icon = Icons.Filled.CameraAlt,
                                iconBg = Color(0xFF8E8E93),
                                onClick = onOpenCamera
                            ),
                            SettingsRowData(
                                title = "System Android Settings",
                                icon = Icons.Filled.Apps,
                                iconBg = Color(0xFF636366),
                                onClick = { onOpenSystemSettings(Settings.ACTION_SETTINGS) }
                            )
                        ).filter { searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) }

                        if (rows.isNotEmpty()) {
                            SettingsGroupCard(rows, cardColor, dividerColor, textPrimary, textSecondary)
                        }
                    }
                }

                SettingsDestination.GENERAL -> {
                    item {
                        val rows = listOf(
                            SettingsRowData("About", Icons.Filled.Settings, Color(0xFF8E8E93)) {
                                push(SettingsDestination.ABOUT)
                            },
                            SettingsRowData(
                                title = "Default Home Launcher",
                                icon = Icons.Filled.Home,
                                iconBg = IosColors.SystemBlue,
                                trailingText = if (state.isDefaultLauncher) "PureOS" else "Choose"
                            ) { onOpenHomeChooser() },
                            SettingsRowData("System Date & Time", Icons.Filled.Settings, IosColors.SystemOrange) {
                                onOpenSystemSettings(Settings.ACTION_DATE_SETTINGS)
                            },
                            SettingsRowData("System Storage", Icons.Filled.Apps, Color(0xFF8E8E93)) {
                                onOpenSystemSettings(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                            }
                        )
                        SettingsGroupCard(rows, cardColor, dividerColor, textPrimary, textSecondary)
                    }
                }

                SettingsDestination.ABOUT -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(cardColor)
                        ) {
                            AboutKeyValueRow("Name", state.settings.profileName, textPrimary, textSecondary)
                            HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                            AboutKeyValueRow("PureOS Version", "26.1 (Android ${Build.VERSION.RELEASE})", textPrimary, textSecondary)
                            HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                            AboutKeyValueRow("Model Name", "${Build.MANUFACTURER} ${Build.MODEL}", textPrimary, textSecondary)
                            HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                            AboutKeyValueRow("Build Number", Build.DISPLAY, textPrimary, textSecondary)
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(cardColor)
                        ) {
                            AboutKeyValueRow("Applications", "${state.allApps.size}", textPrimary, textSecondary)
                            HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                            AboutKeyValueRow("Capacity", state.storageInfo.totalGb, textPrimary, textSecondary)
                            HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                            AboutKeyValueRow("Available", state.storageInfo.availableGb, textPrimary, textSecondary)
                        }
                    }
                }

                SettingsDestination.BATTERY -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardColor)
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${state.batteryStatus.level}%",
                                fontFamily = SfProDisplay,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                color = textPrimary
                            )
                            Text(
                                text = if (state.batteryStatus.isCharging) "Charging • ${state.batteryStatus.temperatureC}°C"
                                else "Discharging • Health: ${state.batteryStatus.healthLabel} • ${state.batteryStatus.temperatureC}°C",
                                fontFamily = SfProText,
                                fontSize = 13.5.sp,
                                color = textSecondary
                            )
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(cardColor)
                        ) {
                            SettingsToggleRow(
                                title = "Battery Percentage",
                                checked = state.settings.showBatteryPercentage,
                                onCheckedChange = onSetBatteryPercent,
                                textPrimary = textPrimary
                            )
                            HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                            SettingsToggleRow(
                                title = "Low Power Mode",
                                checked = state.settings.lowPowerMode,
                                onCheckedChange = onSetLowPower,
                                textPrimary = textPrimary
                            )
                        }
                    }
                    item {
                        Button(
                            onClick = { onOpenSystemSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS) },
                            colors = ButtonDefaults.buttonColors(containerColor = IosColors.SystemBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Android Battery Usage", fontFamily = SfProText, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                SettingsDestination.DISPLAY -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(cardColor)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                AppearanceCard(
                                    title = "Light",
                                    selected = !isDark,
                                    previewBg = Color(0xFFF2F2F7),
                                    onClick = { onSetDarkMode(false) }
                                )
                                AppearanceCard(
                                    title = "Dark",
                                    selected = isDark,
                                    previewBg = Color(0xFF1C1C1E),
                                    onClick = { onSetDarkMode(true) }
                                )
                            }
                            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                            Text("Brightness", fontFamily = SfProText, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = textPrimary)
                            Slider(
                                value = state.brightnessFraction,
                                onValueChange = onBrightnessChange,
                                valueRange = 0.05f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = IosColors.SystemBlue
                                )
                            )
                        }
                    }
                    item {
                        Button(
                            onClick = { onOpenSystemSettings(Settings.ACTION_DISPLAY_SETTINGS) },
                            colors = ButtonDefaults.buttonColors(containerColor = IosColors.SystemBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open System Display Settings", fontFamily = SfProText, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                SettingsDestination.WALLPAPER -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardColor)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "iOS Collections",
                                fontFamily = SfProText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = textPrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                WallpaperPreset.entries.forEach { preset ->
                                    val resId = when (preset) {
                                        WallpaperPreset.IOS_BLUE -> R.drawable.img_wallpaper_ios_blue
                                        WallpaperPreset.IOS_AURORA -> R.drawable.img_wallpaper_ios_aurora
                                        WallpaperPreset.IOS_EMERALD -> R.drawable.img_wallpaper_ios_emerald
                                    }
                                    val selected = state.settings.customWallpaperUri == null && state.settings.wallpaperPreset == preset
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSetWallpaperPreset(preset) },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(0.58f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .border(
                                                    width = if (selected) 2.5.dp else 0.5.dp,
                                                    color = if (selected) IosColors.SystemBlue else dividerColor,
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                        ) {
                                            Image(
                                                painter = painterResource(id = resId),
                                                contentDescription = preset.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            if (selected) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(6.dp)
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(IosColors.SystemBlue),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = preset.title,
                                            fontFamily = SfProText,
                                            fontSize = 11.5.sp,
                                            color = textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IosColors.SystemBlue),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Choose Photo from Library", fontFamily = SfProText, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                SettingsDestination.HOME_SCREEN -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(cardColor)
                        ) {
                            SettingsToggleRow(
                                title = "Notification Badges",
                                checked = state.settings.showBadges,
                                onCheckedChange = onSetShowBadges,
                                textPrimary = textPrimary
                            )
                            HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                            SettingsToggleRow(
                                title = "Show App Labels",
                                checked = state.settings.showIconLabels,
                                onCheckedChange = onSetShowLabels,
                                textPrimary = textPrimary
                            )
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onOpenHomeChooser,
                                colors = ButtonDefaults.buttonColors(containerColor = IosColors.SystemBlue),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Set as Default Home App", fontFamily = SfProText, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = onResetLayout,
                                colors = ButtonDefaults.buttonColors(containerColor = cardColor, contentColor = IosColors.SystemRed),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reset Home Screen Layout", fontFamily = SfProText, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNameDialog) {
        var editedName by remember { mutableStateOf(state.settings.profileName) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Edit Profile Name") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    singleLine = true,
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editedName.isNotBlank()) onSetProfileName(editedName.trim())
                    showNameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private data class SettingsRowData(
    val title: String,
    val icon: ImageVector,
    val iconBg: Color,
    val trailingText: String? = null,
    val onClick: () -> Unit
)

@Composable
private fun SettingsGroupCard(
    rows: List<SettingsRowData>,
    cardColor: Color,
    dividerColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardColor)
    ) {
        rows.forEachIndexed { idx, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = row.onClick)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(row.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = row.icon,
                        contentDescription = row.title,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = row.title,
                    fontFamily = SfProText,
                    fontSize = 16.sp,
                    color = textPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!row.trailingText.isNullOrBlank()) {
                    Text(
                        text = row.trailingText,
                        fontFamily = SfProText,
                        fontSize = 15.sp,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (idx < rows.lastIndex) {
                HorizontalDivider(
                    color = dividerColor,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 56.dp)
                )
            }
        }
    }
}

@Composable
private fun AboutKeyValueRow(
    key: String,
    value: String,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = key, fontFamily = SfProText, fontSize = 16.sp, color = textPrimary)
        Text(
            text = value,
            fontFamily = SfProText,
            fontSize = 15.sp,
            color = textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textPrimary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontFamily = SfProText, fontSize = 16.sp, color = textPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = IosColors.SystemGreen
            )
        )
    }
}

@Composable
private fun AppearanceCard(
    title: String,
    selected: Boolean,
    previewBg: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(110.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(previewBg)
                .border(
                    width = if (selected) 2.5.dp else 1.dp,
                    color = if (selected) IosColors.SystemBlue else Color.Gray.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.DarkMode,
                contentDescription = title,
                tint = if (previewBg == Color(0xFF1C1C1E)) Color.White else Color.Black
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = title,
            fontFamily = SfProText,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp,
            color = if (selected) IosColors.SystemBlue else Color.Gray
        )
    }
}
