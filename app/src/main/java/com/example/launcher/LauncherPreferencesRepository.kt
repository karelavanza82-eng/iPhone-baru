package com.example.launcher

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(name = "pureos_launcher_prefs")

data class SavedHomeEntry(
    val isWidget: Boolean,
    val appId: String? = null,
    val widgetId: String? = null,
    val widgetType: WidgetType? = null,
    val widgetSize: WidgetSize? = null
)

data class PersistedLauncherSettings(
    val wallpaperPreset: WallpaperPreset = WallpaperPreset.IOS_BLUE,
    val customWallpaperUri: String? = null,
    val darkMode: Boolean = true,
    val showBadges: Boolean = true,
    val showIconLabels: Boolean = true,
    val showBatteryPercentage: Boolean = true,
    val lowPowerMode: Boolean = false,
    val focusModeOn: Boolean = false,
    val rotationLockOn: Boolean = false,
    val profileName: String = "PureOS User",
    val pagesJson: String? = null,
    val dockIdsJson: String? = null,
    val notesJson: String? = null,
    val remindersJson: String? = null
)

class LauncherPreferencesRepository(private val context: Context) {

    private object Keys {
        val WALLPAPER_PRESET = stringPreferencesKey("wallpaper_preset")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val SHOW_BADGES = booleanPreferencesKey("show_badges")
        val SHOW_ICON_LABELS = booleanPreferencesKey("show_icon_labels")
        val SHOW_BATTERY_PERCENT = booleanPreferencesKey("show_battery_percent")
        val LOW_POWER_MODE = booleanPreferencesKey("low_power_mode")
        val FOCUS_MODE = booleanPreferencesKey("focus_mode")
        val ROTATION_LOCK = booleanPreferencesKey("rotation_lock")
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PAGES_JSON = stringPreferencesKey("pages_json")
        val DOCK_IDS_JSON = stringPreferencesKey("dock_ids_json")
        val NOTES_JSON = stringPreferencesKey("notes_json")
        val REMINDERS_JSON = stringPreferencesKey("reminders_json")
    }

    val settingsFlow: Flow<PersistedLauncherSettings> = context.launcherDataStore.data.map { prefs ->
        val presetId = prefs[Keys.WALLPAPER_PRESET] ?: WallpaperPreset.IOS_BLUE.id
        val preset = WallpaperPreset.entries.firstOrNull { it.id == presetId } ?: WallpaperPreset.IOS_BLUE
        PersistedLauncherSettings(
            wallpaperPreset = preset,
            customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI],
            darkMode = prefs[Keys.DARK_MODE] ?: true,
            showBadges = prefs[Keys.SHOW_BADGES] ?: true,
            showIconLabels = prefs[Keys.SHOW_ICON_LABELS] ?: true,
            showBatteryPercentage = prefs[Keys.SHOW_BATTERY_PERCENT] ?: true,
            lowPowerMode = prefs[Keys.LOW_POWER_MODE] ?: false,
            focusModeOn = prefs[Keys.FOCUS_MODE] ?: false,
            rotationLockOn = prefs[Keys.ROTATION_LOCK] ?: false,
            profileName = prefs[Keys.PROFILE_NAME] ?: "PureOS User",
            pagesJson = prefs[Keys.PAGES_JSON],
            dockIdsJson = prefs[Keys.DOCK_IDS_JSON],
            notesJson = prefs[Keys.NOTES_JSON],
            remindersJson = prefs[Keys.REMINDERS_JSON]
        )
    }

    suspend fun setWallpaperPreset(preset: WallpaperPreset) {
        context.launcherDataStore.edit {
            it[Keys.WALLPAPER_PRESET] = preset.id
            it.remove(Keys.CUSTOM_WALLPAPER_URI)
        }
    }

    suspend fun setCustomWallpaperUri(uri: String) {
        context.launcherDataStore.edit {
            it[Keys.CUSTOM_WALLPAPER_URI] = uri
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setShowBadges(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_BADGES] = enabled }
    }

    suspend fun setShowIconLabels(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_ICON_LABELS] = enabled }
    }

    suspend fun setShowBatteryPercentage(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_BATTERY_PERCENT] = enabled }
    }

    suspend fun setLowPowerMode(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.LOW_POWER_MODE] = enabled }
    }

    suspend fun setFocusMode(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.FOCUS_MODE] = enabled }
    }

    suspend fun setRotationLock(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.ROTATION_LOCK] = enabled }
    }

    suspend fun setProfileName(name: String) {
        context.launcherDataStore.edit { it[Keys.PROFILE_NAME] = name }
    }

    suspend fun saveLayout(pages: List<HomePageModel>, dockApps: List<LauncherApp>) {
        val pagesArr = JSONArray()
        for (page in pages) {
            val entriesArr = JSONArray()
            for (entry in page.entries) {
                val obj = JSONObject()
                when (entry) {
                    is HomeGridEntry.AppEntry -> {
                        obj.put("type", "app")
                        obj.put("appId", entry.app.id)
                    }
                    is HomeGridEntry.WidgetEntry -> {
                        obj.put("type", "widget")
                        obj.put("widgetId", entry.widget.id)
                        obj.put("widgetType", entry.widget.type.name)
                        obj.put("widgetSize", entry.widget.size.name)
                    }
                }
                entriesArr.put(obj)
            }
            pagesArr.put(entriesArr)
        }
        val dockArr = JSONArray()
        dockApps.forEach { dockArr.put(it.id) }

        context.launcherDataStore.edit {
            it[Keys.PAGES_JSON] = pagesArr.toString()
            it[Keys.DOCK_IDS_JSON] = dockArr.toString()
        }
    }

    suspend fun saveNotes(notes: List<NoteRecord>) {
        val arr = JSONArray()
        notes.forEach { note ->
            arr.put(JSONObject().apply {
                put("id", note.id)
                put("title", note.title)
                put("body", note.body)
                put("updatedAt", note.updatedAtMillis)
            })
        }
        context.launcherDataStore.edit { it[Keys.NOTES_JSON] = arr.toString() }
    }

    suspend fun saveReminders(reminders: List<ReminderRecord>) {
        val arr = JSONArray()
        reminders.forEach { r ->
            arr.put(JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("done", r.isCompleted)
            })
        }
        context.launcherDataStore.edit { it[Keys.REMINDERS_JSON] = arr.toString() }
    }

    fun parsePages(json: String?): List<List<SavedHomeEntry>>? {
        if (json.isNullOrBlank()) return null
        return try {
            val arr = JSONArray(json)
            val pages = mutableListOf<List<SavedHomeEntry>>()
            for (i in 0 until arr.length()) {
                val pArr = arr.getJSONArray(i)
                val entries = mutableListOf<SavedHomeEntry>()
                for (j in 0 until pArr.length()) {
                    val obj = pArr.getJSONObject(j)
                    if (obj.optString("type") == "widget") {
                        val wt = runCatching { WidgetType.valueOf(obj.getString("widgetType")) }.getOrNull()
                        val ws = runCatching { WidgetSize.valueOf(obj.getString("widgetSize")) }.getOrNull()
                        if (wt != null && ws != null) {
                            entries.add(
                                SavedHomeEntry(
                                    isWidget = true,
                                    widgetId = obj.optString("widgetId", "w_${i}_${j}"),
                                    widgetType = wt,
                                    widgetSize = ws
                                )
                            )
                        }
                    } else {
                        val appId = obj.optString("appId")
                        if (appId.isNotBlank()) {
                            entries.add(SavedHomeEntry(isWidget = false, appId = appId))
                        }
                    }
                }
                pages.add(entries)
            }
            pages
        } catch (_: Throwable) {
            null
        }
    }

    fun parseDockIds(json: String?): List<String>? {
        if (json.isNullOrBlank()) return null
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Throwable) {
            null
        }
    }

    fun parseNotes(json: String?): List<NoteRecord>? {
        if (json.isNullOrBlank()) return null
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                NoteRecord(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    body = obj.getString("body"),
                    updatedAtMillis = obj.getLong("updatedAt")
                )
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun parseReminders(json: String?): List<ReminderRecord>? {
        if (json.isNullOrBlank()) return null
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                ReminderRecord(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    isCompleted = obj.getBoolean("done")
                )
            }
        } catch (_: Throwable) {
            null
        }
    }
}
