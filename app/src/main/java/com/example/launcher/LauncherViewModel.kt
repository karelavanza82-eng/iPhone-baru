package com.example.launcher

import android.app.Application
import android.view.View
import android.view.Window
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.icons.IosIconKey
import com.example.services.DeviceSystemController
import com.example.services.NotificationBridge
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class LauncherUiState(
    val isLoading: Boolean = true,
    val allApps: List<LauncherApp> = emptyList(),
    val pages: List<HomePageModel> = emptyList(),
    val dockApps: List<LauncherApp> = emptyList(),
    val recentApps: List<ActiveAppSession> = emptyList(),
    val activeAppSession: ActiveAppSession? = null,
    val closingExternalSession: ActiveAppSession? = null,
    val isClosingActiveApp: Boolean = false,
    val isEditMode: Boolean = false,
    val contextMenuApp: LauncherApp? = null,
    val showWidgetGallery: Boolean = false,
    val showSpotlight: Boolean = false,
    val showControlCenter: Boolean = false,
    val showNotificationCenter: Boolean = false,
    val showAppSwitcher: Boolean = false,
    val currentPageIndex: Int = 0,
    val searchQuery: String = "",
    val settings: PersistedLauncherSettings = PersistedLauncherSettings(),
    val batteryStatus: BatteryStatus = BatteryStatus(),
    val connectivityStatus: ConnectivityStatus = ConnectivityStatus(),
    val mediaInfo: MediaPlaybackInfo = MediaPlaybackInfo(),
    val badgeCounts: Map<String, Int> = emptyMap(),
    val notificationGroups: List<NotificationGroup> = emptyList(),
    val flashlightOn: Boolean = false,
    val volumeFraction: Float = 0.5f,
    val brightnessFraction: Float = 0.65f,
    val isDefaultLauncher: Boolean = false,
    val isNotificationAccessGranted: Boolean = false,
    val storageInfo: DeviceStorageInfo = DeviceStorageInfo(),
    val notes: List<NoteRecord> = defaultNotes(),
    val reminders: List<ReminderRecord> = defaultReminders()
)

private fun defaultNotes() = listOf(
    NoteRecord(
        id = "note_1",
        title = "Weekend Checklist",
        body = "Pick up coffee beans\nCheck camera battery\nSync playlists for road trip",
        updatedAtMillis = System.currentTimeMillis() - 3600_000L
    ),
    NoteRecord(
        id = "note_2",
        title = "Design Ideas",
        body = "Frosted dock material\nSpring-physics icon morphing\nSF Pro geometric hierarchy",
        updatedAtMillis = System.currentTimeMillis() - 86400_000L
    )
)

private fun defaultReminders() = listOf(
    ReminderRecord("rem_1", "Grant Notification Access for live badges", false),
    ReminderRecord("rem_2", "Set PureOS as Default Home App", false),
    ReminderRecord("rem_3", "Customize Home Screen Widgets", true)
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepo = AppDiscoveryRepository(application)
    private val prefsRepo = LauncherPreferencesRepository(application)
    val systemController = DeviceSystemController(application)

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private val iconBoundsRegistry = mutableMapOf<String, Rect>()
    private var pendingExternalSession: ActiveAppSession? = null
    private var windowRef: Window? = null

    init {
        systemController.startListening()
        observeSystemFlows()
        initializeLauncher()
    }

    fun attachWindow(window: Window?) {
        windowRef = window
    }

    override fun onCleared() {
        super.onCleared()
        systemController.stopListening()
    }

    private fun observeSystemFlows() {
        viewModelScope.launch {
            prefsRepo.settingsFlow.collect { persisted ->
                _uiState.update { state ->
                    val parsedNotes = prefsRepo.parseNotes(persisted.notesJson) ?: state.notes
                    val parsedReminders = prefsRepo.parseReminders(persisted.remindersJson) ?: state.reminders
                    state.copy(
                        settings = persisted,
                        notes = parsedNotes,
                        reminders = parsedReminders
                    )
                }
            }
        }
        viewModelScope.launch {
            systemController.batteryStatus.collect { b ->
                _uiState.update { it.copy(batteryStatus = b) }
            }
        }
        viewModelScope.launch {
            systemController.connectivityStatus.collect { c ->
                _uiState.update { it.copy(connectivityStatus = c) }
            }
        }
        viewModelScope.launch {
            systemController.flashlightOn.collect { f ->
                _uiState.update { it.copy(flashlightOn = f) }
            }
        }
        viewModelScope.launch {
            systemController.volumeFraction.collect { v ->
                _uiState.update { it.copy(volumeFraction = v) }
            }
        }
        viewModelScope.launch {
            systemController.brightnessFraction.collect { br ->
                _uiState.update { it.copy(brightnessFraction = br) }
            }
        }
        viewModelScope.launch {
            NotificationBridge.badgeCounts.collect { counts ->
                _uiState.update { it.copy(badgeCounts = counts) }
            }
        }
        viewModelScope.launch {
            NotificationBridge.notificationGroups.collect { groups ->
                _uiState.update { it.copy(notificationGroups = groups) }
            }
        }
        viewModelScope.launch {
            NotificationBridge.mediaInfo.collect { media ->
                _uiState.update { it.copy(mediaInfo = media) }
            }
        }
    }

    private fun initializeLauncher() {
        viewModelScope.launch {
            val apps = appRepo.loadAllApps()
            val persisted = prefsRepo.settingsFlow.first()
            val (pages, dock) = buildInitialLayout(apps, persisted)
            val storage = systemController.readDeviceStorageInfo()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    allApps = apps,
                    pages = pages,
                    dockApps = dock,
                    storageInfo = storage,
                    isDefaultLauncher = systemController.isDefaultHomeLauncher(),
                    isNotificationAccessGranted = NotificationBridge.isNotificationAccessGranted(getApplication())
                )
            }
        }
    }

    fun refreshInstalledAppsAndPermissions() {
        viewModelScope.launch {
            systemController.refreshTelemetry()
            val apps = appRepo.loadAllApps()
            val appById = apps.associateBy { it.id }
            _uiState.update { state ->
                val updatedPages = state.pages.map { page ->
                    val filtered = page.entries.mapNotNull { entry ->
                        when (entry) {
                            is HomeGridEntry.AppEntry -> appById[entry.app.id]?.let { HomeGridEntry.AppEntry(it) }
                            is HomeGridEntry.WidgetEntry -> entry
                        }
                    }
                    page.copy(entries = filtered)
                }
                val updatedDock = state.dockApps.mapNotNull { appById[it.id] }
                state.copy(
                    allApps = apps,
                    pages = updatedPages,
                    dockApps = updatedDock,
                    isDefaultLauncher = systemController.isDefaultHomeLauncher(),
                    isNotificationAccessGranted = NotificationBridge.isNotificationAccessGranted(getApplication()),
                    storageInfo = systemController.readDeviceStorageInfo()
                )
            }
        }
    }

    fun onHostResumed() {
        refreshInstalledAppsAndPermissions()
        val returning = pendingExternalSession
        if (returning != null) {
            pendingExternalSession = null
            val latestRect = iconBoundsRegistry[returning.app.id] ?: returning.originRect
            _uiState.update {
                it.copy(
                    activeAppSession = null,
                    closingExternalSession = returning.copy(originRect = latestRect),
                    isClosingActiveApp = false
                )
            }
        }
    }

    fun onExternalCloseAnimationFinished() {
        _uiState.update { it.copy(closingExternalSession = null) }
    }

    fun registerIconBounds(entryKey: String, rect: Rect) {
        if (rect.width > 0f && rect.height > 0f) {
            iconBoundsRegistry[entryKey] = rect
        }
    }

    fun getIconBounds(appId: String): Rect {
        return iconBoundsRegistry[appId] ?: Rect(400f, 900f, 560f, 1060f)
    }

    fun launchApp(app: LauncherApp, anchorView: View?) {
        if (_uiState.value.isEditMode) return
        val origin = iconBoundsRegistry[app.id] ?: Rect(420f, 1000f, 580f, 1160f)
        val session = ActiveAppSession(
            app = app,
            originRect = origin,
            isExternalApp = !app.isBuiltin
        )

        _uiState.update { state ->
            val updatedRecents = (listOf(session) + state.recentApps.filter { it.app.id != app.id }).take(10)
            state.copy(
                activeAppSession = session,
                isClosingActiveApp = false,
                closingExternalSession = null,
                showSpotlight = false,
                showControlCenter = false,
                showNotificationCenter = false,
                showAppSwitcher = false,
                contextMenuApp = null,
                recentApps = updatedRecents
            )
        }

        if (!app.isBuiltin) {
            pendingExternalSession = session
            viewModelScope.launch {
                delay(240L)
                val launched = appRepo.launchExternalApp(app, origin, anchorView)
                if (!launched) {
                    pendingExternalSession = null
                    requestCloseActiveApp()
                }
            }
        }
    }

    fun launchByPackage(packageName: String, anchorView: View?) {
        val match = _uiState.value.allApps.firstOrNull { !it.isBuiltin && it.packageName == packageName }
        if (match != null) {
            launchApp(match, anchorView)
        }
    }

    fun launchBuiltin(builtinId: IosBuiltinAppId) {
        val match = _uiState.value.allApps.firstOrNull { it.builtinId == builtinId } ?: return
        launchApp(match, null)
    }

    fun requestCloseActiveApp() {
        val current = _uiState.value.activeAppSession ?: return
        val updatedOrigin = iconBoundsRegistry[current.app.id] ?: current.originRect
        _uiState.update {
            it.copy(
                activeAppSession = current.copy(originRect = updatedOrigin),
                isClosingActiveApp = true
            )
        }
    }

    fun onActiveAppClosed() {
        _uiState.update {
            it.copy(
                activeAppSession = null,
                isClosingActiveApp = false
            )
        }
    }

    fun openAppSwitcher() {
        _uiState.update {
            it.copy(
                showAppSwitcher = true,
                activeAppSession = null,
                isClosingActiveApp = false,
                showControlCenter = false,
                showNotificationCenter = false,
                showSpotlight = false
            )
        }
    }

    fun closeAppSwitcher() {
        _uiState.update { it.copy(showAppSwitcher = false) }
    }

    fun removeRecentApp(appId: String) {
        _uiState.update { state ->
            state.copy(recentApps = state.recentApps.filterNot { it.app.id == appId })
        }
    }

    fun setCurrentPage(index: Int) {
        _uiState.update { it.copy(currentPageIndex = index) }
    }

    fun setEditMode(enabled: Boolean) {
        _uiState.update { it.copy(isEditMode = enabled, contextMenuApp = null) }
    }

    fun showContextMenu(app: LauncherApp?) {
        _uiState.update { it.copy(contextMenuApp = app) }
    }

    fun setShowWidgetGallery(show: Boolean) {
        _uiState.update { it.copy(showWidgetGallery = show) }
    }

    fun setShowSpotlight(show: Boolean) {
        _uiState.update {
            it.copy(
                showSpotlight = show,
                searchQuery = if (show) "" else it.searchQuery,
                showControlCenter = false,
                showNotificationCenter = false
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setShowControlCenter(show: Boolean) {
        _uiState.update {
            it.copy(
                showControlCenter = show,
                showNotificationCenter = if (show) false else it.showNotificationCenter,
                showSpotlight = if (show) false else it.showSpotlight
            )
        }
    }

    fun setShowNotificationCenter(show: Boolean) {
        _uiState.update {
            it.copy(
                showNotificationCenter = show,
                showControlCenter = if (show) false else it.showControlCenter,
                showSpotlight = if (show) false else it.showSpotlight
            )
        }
    }

    fun removeEntryFromHome(pageIndex: Int, entryId: String) {
        _uiState.update { state ->
            val newPages = state.pages.map { page ->
                if (page.pageIndex == pageIndex) {
                    page.copy(entries = page.entries.filterNot { it.entryId == entryId })
                } else {
                    page
                }
            }.filter { it.entries.isNotEmpty() || it.pageIndex == 0 }
                .mapIndexed { idx, page -> page.copy(pageIndex = idx) }
            persistLayoutAsync(newPages, state.dockApps)
            state.copy(pages = newPages, contextMenuApp = null)
        }
    }

    fun removeAppFromDock(appId: String) {
        _uiState.update { state ->
            val newDock = state.dockApps.filterNot { it.id == appId }
            persistLayoutAsync(state.pages, newDock)
            state.copy(dockApps = newDock, contextMenuApp = null)
        }
    }

    fun addAppToHome(app: LauncherApp, targetPageIndex: Int? = null) {
        _uiState.update { state ->
            val pageIdx = (targetPageIndex ?: state.currentPageIndex.coerceAtMost((state.pages.size - 1).coerceAtLeast(0)))
            val existsOnHome = state.pages.any { p ->
                p.entries.any { it is HomeGridEntry.AppEntry && it.app.id == app.id }
            } || state.dockApps.any { it.id == app.id }
            if (existsOnHome) return@update state

            val updatedPages = state.pages.toMutableList()
            if (updatedPages.isEmpty()) {
                updatedPages.add(HomePageModel(0, listOf(HomeGridEntry.AppEntry(app))))
            } else {
                val idx = pageIdx.coerceIn(0, updatedPages.lastIndex)
                val target = updatedPages[idx]
                updatedPages[idx] = target.copy(entries = target.entries + HomeGridEntry.AppEntry(app))
            }
            persistLayoutAsync(updatedPages, state.dockApps)
            state.copy(pages = updatedPages)
        }
    }

    fun moveEntryOnPage(pageIndex: Int, fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val updatedPages = state.pages.map { page ->
                if (page.pageIndex == pageIndex &&
                    fromIndex in page.entries.indices &&
                    toIndex in page.entries.indices &&
                    fromIndex != toIndex
                ) {
                    val mutable = page.entries.toMutableList()
                    val item = mutable.removeAt(fromIndex)
                    mutable.add(toIndex, item)
                    page.copy(entries = mutable)
                } else {
                    page
                }
            }
            persistLayoutAsync(updatedPages, state.dockApps)
            state.copy(pages = updatedPages)
        }
    }

    fun moveAppToDock(app: LauncherApp, fromPageIndex: Int) {
        _uiState.update { state ->
            if (state.dockApps.size >= 4 || state.dockApps.any { it.id == app.id }) return@update state
            val newDock = state.dockApps + app
            val newPages = state.pages.map { p ->
                if (p.pageIndex == fromPageIndex) {
                    p.copy(entries = p.entries.filterNot { it is HomeGridEntry.AppEntry && it.app.id == app.id })
                } else p
            }
            persistLayoutAsync(newPages, newDock)
            state.copy(pages = newPages, dockApps = newDock, contextMenuApp = null)
        }
    }

    fun addWidget(type: WidgetType, size: WidgetSize) {
        _uiState.update { state ->
            val widget = HomeWidgetItem(
                id = "w_${UUID.randomUUID().toString().take(8)}",
                type = type,
                size = size
            )
            val newEntry = HomeGridEntry.WidgetEntry(widget)
            val targetIdx = state.currentPageIndex.coerceIn(0, (state.pages.size - 1).coerceAtLeast(0))
            val updatedPages = state.pages.toMutableList()
            if (updatedPages.isEmpty()) {
                updatedPages.add(HomePageModel(0, listOf(newEntry)))
            } else {
                val p = updatedPages[targetIdx]
                updatedPages[targetIdx] = p.copy(entries = listOf(newEntry) + p.entries)
            }
            persistLayoutAsync(updatedPages, state.dockApps)
            state.copy(pages = updatedPages, showWidgetGallery = false)
        }
    }

    fun cycleSmartStack(pageIndex: Int, widgetId: String) {
        _uiState.update { state ->
            val updatedPages = state.pages.map { page ->
                if (page.pageIndex == pageIndex) {
                    page.copy(entries = page.entries.map { entry ->
                        if (entry is HomeGridEntry.WidgetEntry && entry.widget.id == widgetId) {
                            entry.copy(widget = entry.widget.copy(stackIndex = (entry.widget.stackIndex + 1) % 4))
                        } else entry
                    })
                } else page
            }
            state.copy(pages = updatedPages)
        }
    }

    fun toggleFlashlight() = systemController.toggleFlashlight()
    fun setVolume(fraction: Float) = systemController.setVolumeFraction(fraction)
    fun setBrightness(fraction: Float) = systemController.setBrightnessFraction(fraction, windowRef)
    fun toggleFocusMode() = viewModelScope.launch {
        prefsRepo.setFocusMode(!_uiState.value.settings.focusModeOn)
    }
    fun toggleRotationLock() = viewModelScope.launch {
        prefsRepo.setRotationLock(!_uiState.value.settings.rotationLockOn)
    }
    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { prefsRepo.setDarkMode(enabled) }
    fun setShowBadges(enabled: Boolean) = viewModelScope.launch { prefsRepo.setShowBadges(enabled) }
    fun setShowIconLabels(enabled: Boolean) = viewModelScope.launch { prefsRepo.setShowIconLabels(enabled) }
    fun setShowBatteryPercentage(enabled: Boolean) = viewModelScope.launch { prefsRepo.setShowBatteryPercentage(enabled) }
    fun setLowPowerMode(enabled: Boolean) = viewModelScope.launch { prefsRepo.setLowPowerMode(enabled) }
    fun setWallpaperPreset(preset: WallpaperPreset) = viewModelScope.launch { prefsRepo.setWallpaperPreset(preset) }
    fun setCustomWallpaperUri(uri: String) = viewModelScope.launch { prefsRepo.setCustomWallpaperUri(uri) }
    fun setProfileName(name: String) = viewModelScope.launch { prefsRepo.setProfileName(name) }

    fun saveNote(title: String, body: String, existingId: String? = null) {
        val current = _uiState.value.notes.toMutableList()
        val now = System.currentTimeMillis()
        if (existingId != null) {
            val idx = current.indexOfFirst { it.id == existingId }
            if (idx >= 0) {
                current[idx] = current[idx].copy(title = title, body = body, updatedAtMillis = now)
            }
        } else {
            current.add(0, NoteRecord("note_${UUID.randomUUID().toString().take(6)}", title, body, now))
        }
        _uiState.update { it.copy(notes = current) }
        viewModelScope.launch { prefsRepo.saveNotes(current) }
    }

    fun deleteNote(id: String) {
        val current = _uiState.value.notes.filterNot { it.id == id }
        _uiState.update { it.copy(notes = current) }
        viewModelScope.launch { prefsRepo.saveNotes(current) }
    }

    fun toggleReminder(id: String) {
        val current = _uiState.value.reminders.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        _uiState.update { it.copy(reminders = current) }
        viewModelScope.launch { prefsRepo.saveReminders(current) }
    }

    fun addReminder(title: String) {
        if (title.isBlank()) return
        val current = listOf(ReminderRecord("rem_${UUID.randomUUID().toString().take(6)}", title.trim(), false)) +
            _uiState.value.reminders
        _uiState.update { it.copy(reminders = current) }
        viewModelScope.launch { prefsRepo.saveReminders(current) }
    }

    fun resetHomeLayout() {
        viewModelScope.launch {
            val apps = _uiState.value.allApps
            val (pages, dock) = buildDefaultLayout(apps)
            prefsRepo.saveLayout(pages, dock)
            _uiState.update { it.copy(pages = pages, dockApps = dock) }
        }
    }

    private fun persistLayoutAsync(pages: List<HomePageModel>, dock: List<LauncherApp>) {
        viewModelScope.launch {
            prefsRepo.saveLayout(pages, dock)
        }
    }

    private fun buildInitialLayout(
        apps: List<LauncherApp>,
        persisted: PersistedLauncherSettings
    ): Pair<List<HomePageModel>, List<LauncherApp>> {
        val appById = apps.associateBy { it.id }
        val savedPages = prefsRepo.parsePages(persisted.pagesJson)
        val savedDockIds = prefsRepo.parseDockIds(persisted.dockIdsJson)

        if (savedPages != null && savedPages.isNotEmpty()) {
            val pages = savedPages.mapIndexed { pageIdx, savedList ->
                val entries = savedList.mapNotNull { item ->
                    if (item.isWidget && item.widgetType != null && item.widgetSize != null) {
                        HomeGridEntry.WidgetEntry(
                            HomeWidgetItem(
                                id = item.widgetId ?: "w_${pageIdx}",
                                type = item.widgetType,
                                size = item.widgetSize
                            )
                        )
                    } else if (item.appId != null) {
                        appById[item.appId]?.let { HomeGridEntry.AppEntry(it) }
                    } else null
                }
                HomePageModel(pageIndex = pageIdx, entries = entries)
            }.filter { it.entries.isNotEmpty() || it.pageIndex == 0 }

            val dock = savedDockIds?.mapNotNull { appById[it] }?.take(4).orEmpty()
            if (pages.isNotEmpty()) {
                return pages to dock.ifEmpty { pickDefaultDockApps(apps) }
            }
        }
        return buildDefaultLayout(apps)
    }

    private fun buildDefaultLayout(apps: List<LauncherApp>): Pair<List<HomePageModel>, List<LauncherApp>> {
        val dock = pickDefaultDockApps(apps)
        val dockIds = dock.map { it.id }.toSet()
        val available = apps.filterNot { it.id in dockIds }.toMutableList()

        fun takeByIcon(vararg keys: IosIconKey): LauncherApp? {
            for (k in keys) {
                val idx = available.indexOfFirst { it.iconKey == k && !it.isBuiltin }
                    .takeIf { it >= 0 }
                    ?: available.indexOfFirst { it.iconKey == k }
                if (idx >= 0) return available.removeAt(idx)
            }
            return null
        }

        // Page 1: Mirrors the reference video's iPhone Home Screen!
        // Top 4x2: Batteries (2x2) + Calendar (2x2)
        // Remaining 16 slots: iOS core icons + installed counterparts
        val page1Entries = mutableListOf<HomeGridEntry>(
            HomeGridEntry.WidgetEntry(HomeWidgetItem("w_bat", WidgetType.BATTERIES, WidgetSize.SMALL)),
            HomeGridEntry.WidgetEntry(HomeWidgetItem("w_cal", WidgetType.CALENDAR, WidgetSize.SMALL))
        )
        val page1Preferred = listOf(
            IosIconKey.FACETIME,
            IosIconKey.CALENDAR,
            IosIconKey.PHOTOS,
            IosIconKey.MUSIC,
            IosIconKey.SAFARI,
            IosIconKey.NOTES,
            IosIconKey.REMINDERS,
            IosIconKey.CLOCK,
            IosIconKey.WEATHER,
            IosIconKey.APP_STORE,
            IosIconKey.MAPS,
            IosIconKey.SETTINGS,
            IosIconKey.FILES,
            IosIconKey.WALLET,
            IosIconKey.CALCULATOR,
            IosIconKey.CHROME
        )
        for (key in page1Preferred) {
            if (page1Entries.size >= 18) break // 2 widgets + 16 icons
            takeByIcon(key)?.let { page1Entries.add(HomeGridEntry.AppEntry(it)) }
        }
        while (page1Entries.size < 18 && available.isNotEmpty()) {
            page1Entries.add(HomeGridEntry.AppEntry(available.removeAt(0)))
        }

        // Page 2: Mirrors Page 2 of the reference video!
        // Top 4x2: Weather (2x2) + Music (2x2)
        // Row 3-4 left: Clock (2x2) + 4 icons
        // Row 5-6: 8 icons
        val page2Entries = mutableListOf<HomeGridEntry>(
            HomeGridEntry.WidgetEntry(HomeWidgetItem("w_weather", WidgetType.WEATHER, WidgetSize.SMALL)),
            HomeGridEntry.WidgetEntry(HomeWidgetItem("w_music", WidgetType.MUSIC, WidgetSize.SMALL)),
            HomeGridEntry.WidgetEntry(HomeWidgetItem("w_clock", WidgetType.CLOCK, WidgetSize.SMALL))
        )
        val page2Preferred = listOf(
            IosIconKey.PINTEREST,
            IosIconKey.KEEP,
            IosIconKey.DRIVE,
            IosIconKey.INSTAGRAM,
            IosIconKey.TIKTOK,
            IosIconKey.YOUTUBE,
            IosIconKey.YOUTUBE_MUSIC,
            IosIconKey.CAMERA,
            IosIconKey.GEMINI,
            IosIconKey.FACEBOOK,
            IosIconKey.DISCORD,
            IosIconKey.SPOTIFY
        )
        for (key in page2Preferred) {
            if (page2Entries.size >= 15) break // 3 widgets (12 cells) + 12 icons = 24 cells
            takeByIcon(key)?.let { page2Entries.add(HomeGridEntry.AppEntry(it)) }
        }
        while (page2Entries.size < 15 && available.isNotEmpty()) {
            page2Entries.add(HomeGridEntry.AppEntry(available.removeAt(0)))
        }

        val pages = mutableListOf(
            HomePageModel(0, page1Entries),
            HomePageModel(1, page2Entries)
        )

        // Page 3+: Remaining installed Android apps (up to 24 per page)
        var pageIdx = 2
        while (available.isNotEmpty()) {
            val chunk = available.take(24)
            available.subList(0, chunk.size).clear()
            pages.add(HomePageModel(pageIdx++, chunk.map { HomeGridEntry.AppEntry(it) }))
        }

        return pages to dock
    }

    private fun pickDefaultDockApps(apps: List<LauncherApp>): List<LauncherApp> {
        val result = mutableListOf<LauncherApp>()
        fun findMatch(vararg keys: IosIconKey): LauncherApp? {
            for (k in keys) {
                val found = apps.firstOrNull { it.iconKey == k && !it.isBuiltin && it !in result }
                    ?: apps.firstOrNull { it.iconKey == k && it !in result }
                if (found != null) return found
            }
            return null
        }
        findMatch(IosIconKey.PHONE, IosIconKey.FACETIME)?.let { result.add(it) }
        findMatch(IosIconKey.SAFARI, IosIconKey.CHROME, IosIconKey.GMAIL, IosIconKey.MAIL)?.let { result.add(it) }
        findMatch(IosIconKey.WHATSAPP, IosIconKey.MESSAGES, IosIconKey.TELEGRAM)?.let { result.add(it) }
        findMatch(IosIconKey.CAMERA, IosIconKey.SETTINGS, IosIconKey.APP_STORE)?.let { result.add(it) }
        for (app in apps) {
            if (result.size >= 4) break
            if (app !in result) result.add(app)
        }
        return result.take(4)
    }
}
