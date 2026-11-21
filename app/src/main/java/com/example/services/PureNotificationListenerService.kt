package com.example.services

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import com.example.icons.IosIconMapper
import com.example.launcher.MediaPlaybackInfo
import com.example.launcher.NotificationEntry
import com.example.launcher.NotificationGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

object NotificationBridge {
    private val _badgeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val badgeCounts: StateFlow<Map<String, Int>> = _badgeCounts.asStateFlow()

    private val _notificationGroups = MutableStateFlow<List<NotificationGroup>>(emptyList())
    val notificationGroups: StateFlow<List<NotificationGroup>> = _notificationGroups.asStateFlow()

    private val _mediaInfo = MutableStateFlow(MediaPlaybackInfo())
    val mediaInfo: StateFlow<MediaPlaybackInfo> = _mediaInfo.asStateFlow()

    private val _listenerConnected = MutableStateFlow(false)
    val listenerConnected: StateFlow<Boolean> = _listenerConnected.asStateFlow()

    private var serviceRef: WeakReference<PureNotificationListenerService>? = null

    internal fun attachService(service: PureNotificationListenerService) {
        serviceRef = WeakReference(service)
        _listenerConnected.value = true
    }

    internal fun detachService() {
        serviceRef = null
        _listenerConnected.value = false
    }

    internal fun updateNotifications(
        counts: Map<String, Int>,
        groups: List<NotificationGroup>
    ) {
        _badgeCounts.value = counts
        _notificationGroups.value = groups
    }

    internal fun updateMedia(info: MediaPlaybackInfo) {
        _mediaInfo.value = info
    }

    fun cancelNotification(key: String) {
        serviceRef?.get()?.cancelSingleNotification(key)
    }

    fun clearAll() {
        serviceRef?.get()?.clearAllActiveNotifications()
    }

    fun isNotificationAccessGranted(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        val expectedComponent = ComponentName(context, PureNotificationListenerService::class.java)
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledListeners)
        while (splitter.hasNext()) {
            val componentName = ComponentName.unflattenFromString(splitter.next())
            if (componentName == expectedComponent) return true
        }
        return false
    }
}

class PureNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationBridge.attachService(this)
        refreshNotifications()
        refreshMediaSession()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NotificationBridge.detachService()
        NotificationBridge.updateNotifications(emptyMap(), emptyList())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        refreshNotifications()
        refreshMediaSession()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        refreshNotifications()
        refreshMediaSession()
    }

    fun cancelSingleNotification(key: String) {
        try {
            cancelNotification(key)
            refreshNotifications()
        } catch (_: Throwable) {
        }
    }

    fun clearAllActiveNotifications() {
        try {
            cancelAllNotifications()
            refreshNotifications()
        } catch (_: Throwable) {
        }
    }

    private fun refreshNotifications() {
        val active = try {
            activeNotifications ?: emptyArray()
        } catch (_: Throwable) {
            emptyArray()
        }
        val pm = packageManager
        val counts = mutableMapOf<String, Int>()
        val entriesByPkg = mutableMapOf<String, MutableList<NotificationEntry>>()

        for (sbn in active) {
            if (sbn.packageName == packageName) continue
            val notif = sbn.notification ?: continue
            if ((notif.flags and Notification.FLAG_GROUP_SUMMARY) != 0) continue

            val extras = notif.extras
            val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
            val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
            if (title.isEmpty() && text.isEmpty()) continue

            val appLabel = resolveAppLabel(pm, sbn.packageName)
            val iconKey = IosIconMapper.resolveIconKey(sbn.packageName, appLabel)

            counts[sbn.packageName] = (counts[sbn.packageName] ?: 0) + 1
            val list = entriesByPkg.getOrPut(sbn.packageName) { mutableListOf() }
            list.add(
                NotificationEntry(
                    key = sbn.key,
                    packageName = sbn.packageName,
                    appLabel = appLabel,
                    title = title.ifEmpty { appLabel },
                    text = text,
                    postTimeMillis = sbn.postTime,
                    iconKey = iconKey
                )
            )
        }

        val groups = entriesByPkg.map { (pkg, items) ->
            val sorted = items.sortedByDescending { it.postTimeMillis }
            val first = sorted.first()
            NotificationGroup(
                packageName = pkg,
                appLabel = first.appLabel,
                iconKey = first.iconKey,
                items = sorted
            )
        }.sortedByDescending { it.items.firstOrNull()?.postTimeMillis ?: 0L }

        NotificationBridge.updateNotifications(counts, groups)
    }

    private fun refreshMediaSession() {
        try {
            val msm = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
            val component = ComponentName(this, PureNotificationListenerService::class.java)
            val controllers = msm.getActiveSessions(component)
            val active = controllers.firstOrNull {
                val state = it.playbackState?.state
                state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_PAUSED
            } ?: controllers.firstOrNull()

            if (active == null) {
                NotificationBridge.updateMedia(MediaPlaybackInfo())
                return
            }
            val meta = active.metadata
            val title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }
                ?: resolveAppLabel(packageManager, active.packageName)
            val artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty()
            val isPlaying = active.playbackState?.state == PlaybackState.STATE_PLAYING
            NotificationBridge.updateMedia(
                MediaPlaybackInfo(
                    title = title,
                    artist = artist,
                    isPlaying = isPlaying,
                    packageName = active.packageName
                )
            )
        } catch (_: SecurityException) {
        } catch (_: Throwable) {
        }
    }

    private fun resolveAppLabel(pm: PackageManager, pkg: String): String {
        return try {
            val info = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: Throwable) {
            pkg.substringAfterLast('.')
        }
    }
}
