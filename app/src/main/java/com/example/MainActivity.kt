package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.animations.AppLaunchTransitionOverlay
import com.example.animations.AppSwitcherOverlay
import com.example.appdrawer.SpotlightSearchOverlay
import com.example.controlcenter.ControlCenterOverlay
import com.example.homescreen.HomeScreenView
import com.example.launcher.LauncherViewModel
import com.example.notificationcenter.NotificationCenterOverlay
import com.example.theme.IosColors
import com.example.ui.theme.PureOsTheme
import com.example.wallpaper.WallpaperLayer
import com.example.widgets.WidgetGalleryOverlay

class MainActivity : ComponentActivity() {

    private val launcherViewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        launcherViewModel.attachWindow(window)

        setContent {
            val state by launcherViewModel.uiState.collectAsStateWithLifecycle()
            PureOsTheme(darkTheme = state.settings.darkMode) {
                PureOsLauncherRoot(viewModel = launcherViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        launcherViewModel.onHostResumed()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Pressed Home while already on launcher or returning from an external app
        if (intent.hasCategory(Intent.CATEGORY_HOME)) {
            launcherViewModel.setShowControlCenter(false)
            launcherViewModel.setShowNotificationCenter(false)
            launcherViewModel.setShowSpotlight(false)
            launcherViewModel.closeAppSwitcher()
            launcherViewModel.setEditMode(false)
            launcherViewModel.requestCloseActiveApp()
        }
    }
}

@Composable
private fun PureOsLauncherRoot(viewModel: LauncherViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var activeTransitionProgress by remember { mutableFloatStateOf(0f) }

    // Handle Android back press cleanly across overlays and edit mode
    BackHandler(
        enabled = state.showControlCenter ||
            state.showNotificationCenter ||
            state.showSpotlight ||
            state.showWidgetGallery ||
            state.showAppSwitcher ||
            state.isEditMode ||
            state.contextMenuApp != null
    ) {
        when {
            state.contextMenuApp != null -> viewModel.showContextMenu(null)
            state.showWidgetGallery -> viewModel.setShowWidgetGallery(false)
            state.showControlCenter -> viewModel.setShowControlCenter(false)
            state.showNotificationCenter -> viewModel.setShowNotificationCenter(false)
            state.showSpotlight -> viewModel.setShowSpotlight(false)
            state.showAppSwitcher -> viewModel.closeAppSwitcher()
            state.isEditMode -> viewModel.setEditMode(false)
        }
    }

    val overlayActive = state.showControlCenter ||
        state.showNotificationCenter ||
        state.showSpotlight ||
        state.showAppSwitcher ||
        state.showWidgetGallery ||
        state.contextMenuApp != null

    val hasAppTransition = state.activeAppSession != null || state.closingExternalSession != null
    val effectiveAppProgress = if (hasAppTransition) activeTransitionProgress else 0f

    val targetBlur = when {
        hasAppTransition -> (24f * effectiveAppProgress).dp
        state.showControlCenter || state.showSpotlight || state.showAppSwitcher -> 24.dp
        state.showNotificationCenter -> 8.dp
        state.contextMenuApp != null || state.showWidgetGallery -> 16.dp
        else -> 0.dp
    }
    val blurRadius by animateDpAsState(
        targetValue = targetBlur,
        animationSpec = if (hasAppTransition) tween(16) else spring(stiffness = 380f),
        label = "wallpaper_blur"
    )

    val targetDim = when {
        hasAppTransition -> 0.42f * effectiveAppProgress
        state.showControlCenter -> 0.30f
        state.showSpotlight -> 0.36f
        state.showAppSwitcher -> 0.35f
        else -> 0f
    }

    val homeScale by animateFloatAsState(
        targetValue = when {
            hasAppTransition -> 1f - 0.08f * effectiveAppProgress
            overlayActive -> 0.93f
            else -> 1f
        },
        animationSpec = if (hasAppTransition) tween(16) else spring(stiffness = 380f),
        label = "home_scale"
    )

    val homeAlpha by animateFloatAsState(
        targetValue = when {
            hasAppTransition -> 1f - 0.72f * effectiveAppProgress
            state.showControlCenter || state.showNotificationCenter || state.showSpotlight || state.showAppSwitcher -> 0.10f
            else -> 1f
        },
        animationSpec = if (hasAppTransition) tween(16) else spring(stiffness = 380f),
        label = "home_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Dynamic Wallpaper + Blur Layer
        WallpaperLayer(
            preset = state.settings.wallpaperPreset,
            customUri = state.settings.customWallpaperUri,
            blurRadius = blurRadius,
            dimAlpha = targetDim,
            scale = if (hasAppTransition) 1f + 0.05f * effectiveAppProgress else 1f
        )

        if (state.isLoading) {
            CircularProgressIndicator(
                color = IosColors.SystemBlue,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // 2. Authentic iPhone Home Screen + App Library Pager
            HomeScreenView(
                state = state,
                homeScale = homeScale,
                homeAlpha = homeAlpha,
                onPageChanged = viewModel::setCurrentPage,
                onRegisterBounds = viewModel::registerIconBounds,
                onLaunchApp = viewModel::launchApp,
                onLaunchBuiltin = viewModel::launchBuiltin,
                onMediaPlayPause = {
                    viewModel.systemController.dispatchMediaKey(android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                },
                onMediaNext = {
                    viewModel.systemController.dispatchMediaKey(android.view.KeyEvent.KEYCODE_MEDIA_NEXT)
                },
                onCycleSmartStack = viewModel::cycleSmartStack,
                onEnterEditMode = { viewModel.setEditMode(true) },
                onExitEditMode = { viewModel.setEditMode(false) },
                onOpenWidgetGallery = { viewModel.setShowWidgetGallery(true) },
                onRemoveEntry = viewModel::removeEntryFromHome,
                onRemoveFromDock = viewModel::removeAppFromDock,
                onMoveEntry = viewModel::moveEntryOnPage,
                onMoveToDock = viewModel::moveAppToDock,
                onShowContextMenu = viewModel::showContextMenu,
                onOpenSpotlight = { viewModel.setShowSpotlight(true) },
                onOpenControlCenter = { viewModel.setShowControlCenter(true) },
                onOpenNotificationCenter = { viewModel.setShowNotificationCenter(true) },
                onOpenAppInfo = viewModel.systemController::openAppInfo,
                onUninstallApp = viewModel.systemController::requestUninstall
            )
        }

        // 3. Spotlight Search Overlay
        AnimatedVisibility(
            visible = state.showSpotlight,
            enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { -it / 10 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(180)) { -it / 10 }
        ) {
            SpotlightSearchOverlay(
                query = state.searchQuery,
                allApps = state.allApps,
                recentApps = state.recentApps,
                onQueryChange = viewModel::updateSearchQuery,
                onLaunchApp = { app -> viewModel.launchApp(app, null) },
                onDismiss = { viewModel.setShowSpotlight(false) }
            )
        }

        // 4. Notification Center / Lock Screen Overlay
        AnimatedVisibility(
            visible = state.showNotificationCenter,
            enter = slideInVertically(spring(dampingRatio = 0.88f, stiffness = 360f)) { -it } + fadeIn(tween(160)),
            exit = slideOutVertically(spring(dampingRatio = 0.90f, stiffness = 400f)) { -it } + fadeOut(tween(140))
        ) {
            NotificationCenterOverlay(
                state = state,
                onDismiss = { viewModel.setShowNotificationCenter(false) },
                onGrantNotificationAccess = viewModel.systemController::openNotificationListenerSettings,
                onLaunchPackage = { pkg -> viewModel.launchByPackage(pkg, null) },
                onToggleFlashlight = viewModel::toggleFlashlight,
                onOpenCamera = viewModel.systemController::openCameraApp
            )
        }

        // 5. Control Center Overlay
        AnimatedVisibility(
            visible = state.showControlCenter,
            enter = fadeIn(tween(180)) + slideInVertically(spring(dampingRatio = 0.86f, stiffness = 380f)) { -it / 5 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(180)) { -it / 5 }
        ) {
            ControlCenterOverlay(
                state = state,
                onDismiss = { viewModel.setShowControlCenter(false) },
                onToggleFlashlight = viewModel::toggleFlashlight,
                onToggleFocusMode = viewModel::toggleFocusMode,
                onToggleRotationLock = viewModel::toggleRotationLock,
                onToggleLowPower = { viewModel.setLowPowerMode(!state.settings.lowPowerMode) },
                onToggleDarkMode = { viewModel.setDarkMode(!state.settings.darkMode) },
                onBrightnessChange = viewModel::setBrightness,
                onVolumeChange = viewModel::setVolume,
                onMediaKey = viewModel.systemController::dispatchMediaKey,
                onOpenSystemSettings = viewModel.systemController::openSystemSettings,
                onOpenCamera = viewModel.systemController::openCameraApp,
                onLaunchBuiltin = viewModel::launchBuiltin
            )
        }

        // 6. Widget Gallery Bottom Sheet
        AnimatedVisibility(
            visible = state.showWidgetGallery,
            enter = fadeIn(tween(160)) + slideInVertically(spring(dampingRatio = 0.86f, stiffness = 360f)) { it },
            exit = fadeOut(tween(140)) + slideOutVertically(tween(180)) { it }
        ) {
            WidgetGalleryOverlay(
                batteryStatus = state.batteryStatus,
                mediaInfo = state.mediaInfo,
                reminders = state.reminders,
                onAddWidget = viewModel::addWidget,
                onDismiss = { viewModel.setShowWidgetGallery(false) }
            )
        }

        // 7. iOS App Switcher Carousel
        AnimatedVisibility(
            visible = state.showAppSwitcher,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(140))
        ) {
            AppSwitcherOverlay(
                recentApps = state.recentApps,
                onSelectApp = { app, anchor ->
                    viewModel.closeAppSwitcher()
                    viewModel.launchApp(app, anchor)
                },
                onRemoveRecent = viewModel::removeRecentApp,
                onDismiss = viewModel::closeAppSwitcher
            )
        }

        // 8. Hero Spring App Opening & Closing Transition Overlay (In-App + External Return)
        val activeSession = state.activeAppSession ?: state.closingExternalSession
        val isReturningFromExternal = state.activeAppSession == null && state.closingExternalSession != null
        if (activeSession != null) {
            AppLaunchTransitionOverlay(
                session = activeSession,
                isClosing = state.isClosingActiveApp,
                isExternalReturn = isReturningFromExternal,
                state = state,
                onProgressChanged = { p -> activeTransitionProgress = p },
                onRequestClose = viewModel::requestCloseActiveApp,
                onOpenSwitcher = viewModel::openAppSwitcher,
                onCloseFinished = {
                    activeTransitionProgress = 0f
                    if (isReturningFromExternal) viewModel.onExternalCloseAnimationFinished()
                    else viewModel.onActiveAppClosed()
                },
                onLaunchApp = viewModel::launchApp,
                onSetDarkMode = viewModel::setDarkMode,
                onSetBatteryPercent = viewModel::setShowBatteryPercentage,
                onSetLowPower = viewModel::setLowPowerMode,
                onSetShowBadges = viewModel::setShowBadges,
                onSetShowLabels = viewModel::setShowIconLabels,
                onSetWallpaperPreset = viewModel::setWallpaperPreset,
                onSetCustomWallpaperUri = viewModel::setCustomWallpaperUri,
                onSetProfileName = viewModel::setProfileName,
                onBrightnessChange = viewModel::setBrightness,
                onResetLayout = viewModel::resetHomeLayout,
                onOpenHomeChooser = viewModel.systemController::openHomeLauncherPicker,
                onOpenNotificationAccess = viewModel.systemController::openNotificationListenerSettings,
                onOpenSystemSettings = viewModel.systemController::openSystemSettings,
                onOpenCamera = viewModel.systemController::openCameraApp,
                onSaveNote = viewModel::saveNote,
                onDeleteNote = viewModel::deleteNote,
                onToggleReminder = viewModel::toggleReminder,
                onAddReminder = viewModel::addReminder
            )
        }
    }
}
