package com.example.homescreen

import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.appdrawer.AppLibraryPage
import com.example.icons.IosAppIconGraphic
import com.example.icons.IosNotificationBadge
import com.example.launcher.HomeGridEntry
import com.example.launcher.HomePageModel
import com.example.launcher.IosBuiltinAppId
import com.example.launcher.LauncherApp
import com.example.launcher.LauncherUiState
import com.example.theme.IosColors
import com.example.theme.SfProText
import com.example.widgets.IosWidgetCard
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class PlacedGridSlot(
    val entryIndex: Int,
    val entry: HomeGridEntry,
    val col: Int,
    val row: Int,
    val colSpan: Int,
    val rowSpan: Int
)

private fun computeGridSlots(entries: List<HomeGridEntry>): List<PlacedGridSlot> {
    val cols = 4
    val maxRows = 6
    val occupied = Array(maxRows) { BooleanArray(cols) }
    val placed = mutableListOf<PlacedGridSlot>()

    entries.forEachIndexed { idx, entry ->
        val spanC = entry.colSpan.coerceIn(1, cols)
        val spanR = entry.rowSpan.coerceIn(1, maxRows)
        var found = false
        for (r in 0..(maxRows - spanR)) {
            for (c in 0..(cols - spanC)) {
                var free = true
                for (dr in 0 until spanR) {
                    for (dc in 0 until spanC) {
                        if (occupied[r + dr][c + dc]) {
                            free = false
                            break
                        }
                    }
                    if (!free) break
                }
                if (free) {
                    for (dr in 0 until spanR) {
                        for (dc in 0 until spanC) {
                            occupied[r + dr][c + dc] = true
                        }
                    }
                    placed.add(PlacedGridSlot(idx, entry, c, r, spanC, spanR))
                    found = true
                    break
                }
            }
            if (found) break
        }
    }
    return placed
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenView(
    state: LauncherUiState,
    homeScale: Float,
    homeAlpha: Float,
    onPageChanged: (Int) -> Unit,
    onRegisterBounds: (String, Rect) -> Unit,
    onLaunchApp: (LauncherApp, View?) -> Unit,
    onLaunchBuiltin: (IosBuiltinAppId) -> Unit,
    onMediaPlayPause: () -> Unit,
    onMediaNext: () -> Unit,
    onCycleSmartStack: (Int, String) -> Unit,
    onEnterEditMode: () -> Unit,
    onExitEditMode: () -> Unit,
    onOpenWidgetGallery: () -> Unit,
    onRemoveEntry: (Int, String) -> Unit,
    onRemoveFromDock: (String) -> Unit,
    onMoveEntry: (Int, Int, Int) -> Unit,
    onMoveToDock: (LauncherApp, Int) -> Unit,
    onShowContextMenu: (LauncherApp?) -> Unit,
    onOpenSpotlight: () -> Unit,
    onOpenControlCenter: () -> Unit,
    onOpenNotificationCenter: () -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPages = state.pages.size + 1 // +1 for App Library page on far right
    val pagerState = rememberPagerState(initialPage = state.currentPageIndex) { totalPages }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    val isAppLibraryPage = pagerState.currentPage == state.pages.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = homeScale
                scaleY = homeScale
                alpha = homeAlpha
            }
            .pointerInput(state.isEditMode) {
                var totalDragY = 0f
                var startX = 0f
                var startY = 0f
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        totalDragY = 0f
                        startX = offset.x
                        startY = offset.y
                    },
                    onVerticalDrag = { _, dragAmount ->
                        totalDragY += dragAmount
                    },
                    onDragEnd = {
                        if (!state.isEditMode && totalDragY > 90f) {
                            val topZone = size.height * 0.22f
                            if (startY <= topZone) {
                                if (startX >= size.width * 0.5f) {
                                    onOpenControlCenter()
                                } else {
                                    onOpenNotificationCenter()
                                }
                            } else {
                                onOpenSpotlight()
                            }
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Top Status Bar or Edit Mode Bar
            Box(modifier = Modifier.fillMaxWidth()) {
                IosStatusBar(
                    batteryStatus = state.batteryStatus,
                    connectivityStatus = state.connectivityStatus,
                    mediaInfo = state.mediaInfo,
                    lowPowerMode = state.settings.lowPowerMode,
                    showBatteryPercentage = state.settings.showBatteryPercentage,
                    isLightContent = true,
                    showDynamicIsland = !state.isEditMode,
                    onTapLeft = onOpenNotificationCenter,
                    onTapRight = onOpenControlCenter
                )

                if (state.isEditMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 44.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.28f))
                                .border(0.6.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                                .clickable { onOpenWidgetGallery() }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("add_widget_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Widget", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Widget", color = Color.White, fontFamily = SfProText, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.28f))
                                .border(0.6.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                                .clickable { onExitEditMode() }
                                .padding(horizontal = 18.dp, vertical = 7.dp)
                                .testTag("done_edit_button")
                        ) {
                            Text("Done", color = Color.White, fontFamily = SfProText, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                        }
                    }
                }
            }

            // Main Pager (Home Pages + App Library)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                if (pageIndex < state.pages.size) {
                    val page = state.pages[pageIndex]
                    HomeGridPage(
                        page = page,
                        state = state,
                        onRegisterBounds = onRegisterBounds,
                        onLaunchApp = { app -> onLaunchApp(app, view) },
                        onLaunchBuiltin = onLaunchBuiltin,
                        onMediaPlayPause = onMediaPlayPause,
                        onMediaNext = onMediaNext,
                        onCycleSmartStack = { widgetId -> onCycleSmartStack(pageIndex, widgetId) },
                        onEnterEditMode = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEnterEditMode()
                        },
                        onRemoveEntry = { entryId -> onRemoveEntry(pageIndex, entryId) },
                        onMoveEntry = { fromIdx, toIdx -> onMoveEntry(pageIndex, fromIdx, toIdx) },
                        onShowContextMenu = { app ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onShowContextMenu(app)
                        }
                    )
                } else {
                    AppLibraryPage(
                        allApps = state.allApps,
                        badgeCounts = if (state.settings.showBadges) state.badgeCounts else emptyMap(),
                        onLaunchApp = { app -> onLaunchApp(app, view) },
                        onRegisterBounds = onRegisterBounds
                    )
                }
            }

            // Search Pill + Page Dots + Dock (hidden on App Library page just like real iPhone!)
            if (!isAppLibraryPage) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Page Indicator Dots + Search Pill matching the reference video
                    Row(
                        modifier = Modifier
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (state.pages.size > 1) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(state.pages.size) { idx ->
                                    val selected = pagerState.currentPage == idx
                                    Box(
                                        modifier = Modifier
                                            .size(if (selected) 7.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = if (selected) 1f else 0.42f))
                                            .clickable {
                                                scope.launch { pagerState.animateScrollToPage(idx) }
                                            }
                                    )
                                }
                            }
                        }

                        // Authentic iOS "Search" pill above dock
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x471E212B))
                                .border(0.5.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                                .clickable { onOpenSpotlight() }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                                .testTag("home_search_pill"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = Color.White.copy(alpha = 0.92f),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Search",
                                fontFamily = SfProText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                    }

                    // Frosted iOS Dock
                    IosFrostedDock(
                        dockApps = state.dockApps,
                        badgeCounts = if (state.settings.showBadges) state.badgeCounts else emptyMap(),
                        isEditMode = state.isEditMode,
                        activeHiddenAppId = state.activeAppSession?.app?.id,
                        onRegisterBounds = onRegisterBounds,
                        onLaunchApp = { app -> onLaunchApp(app, view) },
                        onRemoveFromDock = onRemoveFromDock,
                        onShowContextMenu = { app ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onShowContextMenu(app)
                        }
                    )
                }
            }
        }

        // Long-Press App Context Menu Overlay
        AnimatedVisibility(
            visible = state.contextMenuApp != null,
            enter = fadeIn(tween(150)) + scaleIn(tween(180), initialScale = 0.92f),
            exit = fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.92f)
        ) {
            state.contextMenuApp?.let { app ->
                AppContextMenuOverlay(
                    app = app,
                    currentPageIndex = pagerState.currentPage,
                    isInDock = state.dockApps.any { it.id == app.id },
                    canMoveToDock = state.dockApps.size < 4,
                    onDismiss = { onShowContextMenu(null) },
                    onEditHomeScreen = {
                        onShowContextMenu(null)
                        onEnterEditMode()
                    },
                    onMoveToDock = {
                        onMoveToDock(app, pagerState.currentPage)
                    },
                    onRemoveFromHome = {
                        val isInDock = state.dockApps.any { it.id == app.id }
                        if (isInDock) onRemoveFromDock(app.id)
                        else onRemoveEntry(pagerState.currentPage, "app:${app.id}")
                    },
                    onAppInfo = {
                        onShowContextMenu(null)
                        onOpenAppInfo(app.packageName)
                    },
                    onUninstall = {
                        onShowContextMenu(null)
                        onUninstallApp(app.packageName)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeGridPage(
    page: HomePageModel,
    state: LauncherUiState,
    onRegisterBounds: (String, Rect) -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onLaunchBuiltin: (IosBuiltinAppId) -> Unit,
    onMediaPlayPause: () -> Unit,
    onMediaNext: () -> Unit,
    onCycleSmartStack: (String) -> Unit,
    onEnterEditMode: () -> Unit,
    onRemoveEntry: (String) -> Unit,
    onMoveEntry: (Int, Int) -> Unit,
    onShowContextMenu: (LauncherApp) -> Unit
) {
    val density = LocalDensity.current
    val placedSlots = remember(page.entries) { computeGridSlots(page.entries) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (state.isEditMode) Unit },
                onLongClick = onEnterEditMode
            )
            .padding(horizontal = 18.dp, vertical = 6.dp)
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight
        val cellW = totalWidth / 4f
        val cellH = (totalHeight / 6f).coerceAtMost(108.dp)
        val iconSize = (cellW * 0.72f).coerceIn(54.dp, 64.dp)

        var draggedEntryIdx by remember { mutableIntStateOf(-1) }
        var dragOffset by remember { mutableStateOf(Offset.Zero) }

        placedSlots.forEach { slot ->
            val xDp = cellW * slot.col
            val yDp = cellH * slot.row
            val slotWDp = cellW * slot.colSpan
            val slotHDp = cellH * slot.rowSpan

            val isBeingDragged = draggedEntryIdx == slot.entryIndex
            val dragShiftX = if (isBeingDragged) dragOffset.x else 0f
            val dragShiftY = if (isBeingDragged) dragOffset.y else 0f

            val jiggleRotation = rememberJiggleRotation(
                enabled = state.isEditMode,
                seed = slot.entryIndex
            )

            Box(
                modifier = Modifier
                    .offset(x = xDp, y = yDp)
                    .width(slotWDp)
                    .height(slotHDp)
                    .zIndex(if (isBeingDragged) 20f else 1f)
                    .graphicsLayer {
                        translationX = dragShiftX
                        translationY = dragShiftY
                        rotationZ = if (isBeingDragged) 0f else jiggleRotation
                        scaleX = if (isBeingDragged) 1.08f else 1f
                        scaleY = if (isBeingDragged) 1.08f else 1f
                    }
                    .then(
                        if (state.isEditMode) {
                            Modifier.pointerInput(slot.entryIndex, page.entries.size) {
                                detectDragGestures(
                                    onDragStart = {
                                        draggedEntryIdx = slot.entryIndex
                                        dragOffset = Offset.Zero
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount
                                    },
                                    onDragEnd = {
                                        val cellWPx = with(density) { cellW.toPx() }
                                        val cellHPx = with(density) { cellH.toPx() }
                                        val targetCol = ((slot.col * cellWPx + dragOffset.x + cellWPx * 0.5f) / cellWPx)
                                            .toInt().coerceIn(0, 3)
                                        val targetRow = ((slot.row * cellHPx + dragOffset.y + cellHPx * 0.5f) / cellHPx)
                                            .toInt().coerceIn(0, 5)
                                        val closest = placedSlots.minByOrNull { s ->
                                            val dc = s.col - targetCol
                                            val dr = s.row - targetRow
                                            dc * dc + dr * dr
                                        }
                                        if (closest != null && closest.entryIndex != slot.entryIndex) {
                                            onMoveEntry(slot.entryIndex, closest.entryIndex)
                                        }
                                        draggedEntryIdx = -1
                                        dragOffset = Offset.Zero
                                    },
                                    onDragCancel = {
                                        draggedEntryIdx = -1
                                        dragOffset = Offset.Zero
                                    }
                                )
                            }
                        } else Modifier
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                when (val entry = slot.entry) {
                    is HomeGridEntry.AppEntry -> {
                        val app = entry.app
                        val badgeCount = if (state.settings.showBadges && !app.isBuiltin) {
                            state.badgeCounts[app.packageName] ?: 0
                        } else 0
                        val isHiddenForLaunch = state.activeAppSession?.app?.id == app.id && !state.isClosingActiveApp

                        IosHomeAppTile(
                            app = app,
                            iconSize = iconSize,
                            badgeCount = badgeCount,
                            showLabel = state.settings.showIconLabels,
                            isEditMode = state.isEditMode,
                            isHidden = isHiddenForLaunch,
                            onRegisterBounds = { rect -> onRegisterBounds(app.id, rect) },
                            onClick = { if (!state.isEditMode) onLaunchApp(app) },
                            onLongClick = { onShowContextMenu(app) },
                            onRemoveClick = { onRemoveEntry(entry.entryId) }
                        )
                    }

                    is HomeGridEntry.WidgetEntry -> {
                        val widget = entry.widget
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                IosWidgetCard(
                                    widget = widget,
                                    batteryStatus = state.batteryStatus,
                                    mediaInfo = state.mediaInfo,
                                    reminders = state.reminders,
                                    onWidgetClick = {
                                        if (!state.isEditMode) {
                                            when (widget.type) {
                                                com.example.launcher.WidgetType.BATTERIES -> onLaunchBuiltin(IosBuiltinAppId.SETTINGS)
                                                com.example.launcher.WidgetType.CALENDAR -> onLaunchBuiltin(IosBuiltinAppId.CALENDAR)
                                                com.example.launcher.WidgetType.WEATHER -> onLaunchBuiltin(IosBuiltinAppId.WEATHER)
                                                com.example.launcher.WidgetType.CLOCK -> onLaunchBuiltin(IosBuiltinAppId.CLOCK)
                                                com.example.launcher.WidgetType.PHOTOS -> onLaunchBuiltin(IosBuiltinAppId.PHOTOS)
                                                else -> {}
                                            }
                                        }
                                    },
                                    onMediaPlayPause = onMediaPlayPause,
                                    onMediaNext = onMediaNext,
                                    onCycleSmartStack = { onCycleSmartStack(widget.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                )
                                if (state.settings.showIconLabels) {
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = widget.type.displayName,
                                        style = TextStyle(
                                            fontFamily = SfProText,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.5.sp,
                                            color = Color.White,
                                            shadow = Shadow(
                                                color = Color.Black.copy(alpha = 0.55f),
                                                offset = Offset(0f, 1f),
                                                blurRadius = 4f
                                            )
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (state.isEditMode) {
                                EditRemoveBadge(
                                    onClick = { onRemoveEntry(entry.entryId) },
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .offset(x = (-4).dp, y = (-4).dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IosHomeAppTile(
    app: LauncherApp,
    iconSize: Dp,
    badgeCount: Int,
    showLabel: Boolean,
    isEditMode: Boolean,
    isHidden: Boolean,
    onRegisterBounds: (Rect) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .alpha(if (isHidden) 0f else 1f)
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .onGloballyPositioned { coords ->
                        onRegisterBounds(coords.boundsInRoot())
                    }
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(14.dp), clip = false)
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                    .testTag("app_icon_${app.id}")
            ) {
                IosAppIconGraphic(
                    iconKey = app.iconKey,
                    fallbackBitmap = app.fallbackBitmap,
                    fallbackTintColor = app.fallbackTintColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (badgeCount > 0 && !isEditMode) {
                IosNotificationBadge(
                    count = badgeCount,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-5).dp)
                )
            }

            if (isEditMode) {
                EditRemoveBadge(
                    onClick = onRemoveClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-6).dp, y = (-6).dp)
                )
            }
        }

        if (showLabel) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = app.label,
                style = TextStyle(
                    fontFamily = SfProText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.5.sp,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.65f),
                        offset = Offset(0f, 1f),
                        blurRadius = 4f
                    )
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 3.dp)
            )
        }
    }
}

@Composable
private fun IosFrostedDock(
    dockApps: List<LauncherApp>,
    badgeCounts: Map<String, Int>,
    isEditMode: Boolean,
    activeHiddenAppId: String?,
    onRegisterBounds: (String, Rect) -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onRemoveFromDock: (String) -> Unit,
    onShowContextMenu: (LauncherApp) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(bottom = 12.dp)
            .height(92.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(IosColors.DockGlass)
            .border(0.6.dp, IosColors.DockBorder, RoundedCornerShape(32.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dockApps.forEach { app ->
                val badgeCount = if (!app.isBuiltin) badgeCounts[app.packageName] ?: 0 else 0
                val rot = rememberJiggleRotation(enabled = isEditMode, seed = app.id.hashCode())
                Box(
                    modifier = Modifier.graphicsLayer { rotationZ = rot }
                ) {
                    IosHomeAppTile(
                        app = app,
                        iconSize = 60.dp,
                        badgeCount = badgeCount,
                        showLabel = false,
                        isEditMode = isEditMode,
                        isHidden = activeHiddenAppId == app.id,
                        onRegisterBounds = { rect -> onRegisterBounds(app.id, rect) },
                        onClick = { if (!isEditMode) onLaunchApp(app) },
                        onLongClick = { onShowContextMenu(app) },
                        onRemoveClick = { onRemoveFromDock(app.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditRemoveBadge(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(Color(0xD8D1D1D6))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Remove,
            contentDescription = "Remove from Home Screen",
            tint = Color(0xFF1C1C1E),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun rememberJiggleRotation(enabled: Boolean, seed: Int): Float {
    if (!enabled) return 0f
    val infiniteTransition = rememberInfiniteTransition(label = "jiggle")
    val phase = if (seed % 2 == 0) 1f else -1f
    val rot by infiniteTransition.animateFloat(
        initialValue = -1.6f * phase,
        targetValue = 1.6f * phase,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 135 + (seed.and(3) * 15), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jiggle_rot"
    )
    return rot
}

@Composable
private fun AppContextMenuOverlay(
    app: LauncherApp,
    currentPageIndex: Int,
    isInDock: Boolean,
    canMoveToDock: Boolean,
    onDismiss: () -> Unit,
    onEditHomeScreen: () -> Unit,
    onMoveToDock: () -> Unit,
    onRemoveFromHome: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(264.dp)
                .clickable(enabled = false) {},
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(72.dp)) {
                IosAppIconGraphic(
                    iconKey = app.iconKey,
                    fallbackBitmap = app.fallbackBitmap,
                    fallbackTintColor = app.fallbackTintColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = app.label,
                fontFamily = SfProText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.White
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xEB252529))
                    .border(0.5.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
            ) {
                ContextMenuRow(
                    title = "Edit Home Screen",
                    icon = Icons.Filled.Edit,
                    tint = Color.White,
                    onClick = onEditHomeScreen
                )
                if (!isInDock && canMoveToDock) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
                    ContextMenuRow(
                        title = "Move to Dock",
                        icon = Icons.Filled.Add,
                        tint = Color.White,
                        onClick = onMoveToDock
                    )
                }
                if (!app.isBuiltin) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
                    ContextMenuRow(
                        title = "App Info",
                        icon = Icons.Filled.Info,
                        tint = Color.White,
                        onClick = onAppInfo
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
                ContextMenuRow(
                    title = "Remove from Home Screen",
                    icon = Icons.Outlined.RemoveCircleOutline,
                    tint = IosColors.SystemRed,
                    onClick = onRemoveFromHome
                )
                if (!app.isBuiltin) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
                    ContextMenuRow(
                        title = "Uninstall App",
                        icon = Icons.Filled.Delete,
                        tint = IosColors.SystemRed,
                        onClick = onUninstall
                    )
                }
            }
        }
    }
}

@Composable
private fun ContextMenuRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = SfProText,
            fontSize = 15.sp,
            color = tint
        )
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}
