package com.example.launcher

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.example.icons.IosIconKey

enum class IosBuiltinAppId(val routeId: String, val defaultTitle: String, val iconKey: IosIconKey) {
    SETTINGS("builtin_settings", "Settings", IosIconKey.SETTINGS),
    CLOCK("builtin_clock", "Clock", IosIconKey.CLOCK),
    CALCULATOR("builtin_calculator", "Calculator", IosIconKey.CALCULATOR),
    WEATHER("builtin_weather", "Weather", IosIconKey.WEATHER),
    NOTES("builtin_notes", "Notes", IosIconKey.NOTES),
    CALENDAR("builtin_calendar", "Calendar", IosIconKey.CALENDAR),
    PHOTOS("builtin_photos", "Photos", IosIconKey.PHOTOS),
    APP_STORE("builtin_app_store", "App Store", IosIconKey.APP_STORE),
    FILES("builtin_files", "Files", IosIconKey.FILES),
    REMINDERS("builtin_reminders", "Reminders", IosIconKey.REMINDERS)
}

enum class AppCategory(val title: String) {
    SUGGESTIONS("Suggestions"),
    RECENTLY_ADDED("Recently Added"),
    SOCIAL("Social"),
    UTILITIES("Utilities"),
    PRODUCTIVITY("Productivity & Finance"),
    ENTERTAINMENT("Entertainment"),
    CREATIVITY("Creativity"),
    INFORMATION("Information & Reading"),
    OTHER("Other")
}

data class LauncherApp(
    val id: String,
    val label: String,
    val packageName: String,
    val activityName: String?,
    val iconKey: IosIconKey?,
    val fallbackBitmap: ImageBitmap?,
    val fallbackTintColor: Color,
    val category: AppCategory,
    val builtinId: IosBuiltinAppId? = null,
    val installTimeMillis: Long = 0L
) {
    val isBuiltin: Boolean get() = builtinId != null
}

enum class WidgetType(val displayName: String) {
    BATTERIES("Batteries"),
    CALENDAR("Calendar"),
    WEATHER("Weather"),
    CLOCK("Clock"),
    MUSIC("Music"),
    PHOTOS("Photos"),
    SMART_STACK("Smart Stack")
}

enum class WidgetSize(val columns: Int, val rows: Int, val label: String) {
    SMALL(2, 2, "Small"),
    MEDIUM(4, 2, "Medium"),
    LARGE(4, 4, "Large")
}

data class HomeWidgetItem(
    val id: String,
    val type: WidgetType,
    val size: WidgetSize,
    val stackIndex: Int = 0
)

sealed interface HomeGridEntry {
    val entryId: String
    val colSpan: Int
    val rowSpan: Int

    data class AppEntry(
        val app: LauncherApp
    ) : HomeGridEntry {
        override val entryId: String get() = "app:${app.id}"
        override val colSpan: Int get() = 1
        override val rowSpan: Int get() = 1
    }

    data class WidgetEntry(
        val widget: HomeWidgetItem
    ) : HomeGridEntry {
        override val entryId: String get() = "widget:${widget.id}"
        override val colSpan: Int get() = widget.size.columns
        override val rowSpan: Int get() = widget.size.rows
    }
}

data class HomePageModel(
    val pageIndex: Int,
    val entries: List<HomeGridEntry>
)

enum class WallpaperPreset(val id: String, val title: String) {
    IOS_BLUE("ios_blue", "iOS 18 Sapphire"),
    IOS_AURORA("ios_aurora", "Twilight Aurora"),
    IOS_EMERALD("ios_emerald", "Emerald Depth")
}

data class WallpaperConfig(
    val preset: WallpaperPreset = WallpaperPreset.IOS_BLUE,
    val customUri: String? = null
)

data class NotificationEntry(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val postTimeMillis: Long,
    val iconKey: IosIconKey?
)

data class NotificationGroup(
    val packageName: String,
    val appLabel: String,
    val iconKey: IosIconKey?,
    val items: List<NotificationEntry>
)

data class BatteryStatus(
    val level: Int = 100,
    val isCharging: Boolean = false,
    val healthLabel: String = "Good",
    val temperatureC: Float = 31.5f
)

data class ConnectivityStatus(
    val wifiConnected: Boolean = false,
    val wifiSsid: String = "Not Connected",
    val cellularConnected: Boolean = false,
    val carrierName: String = "No Service",
    val bluetoothEnabled: Boolean = false,
    val airplaneModeOn: Boolean = false
)

data class MediaPlaybackInfo(
    val title: String = "Not Playing",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val packageName: String? = null
)

data class DeviceStorageInfo(
    val totalGb: String = "64 GB",
    val availableGb: String = "32.0 GB"
)

data class ActiveAppSession(
    val app: LauncherApp,
    val originRect: Rect,
    val isExternalApp: Boolean,
    val openedAtMillis: Long = System.currentTimeMillis()
)

data class NoteRecord(
    val id: String,
    val title: String,
    val body: String,
    val updatedAtMillis: Long
)

data class ReminderRecord(
    val id: String,
    val title: String,
    val isCompleted: Boolean
)
