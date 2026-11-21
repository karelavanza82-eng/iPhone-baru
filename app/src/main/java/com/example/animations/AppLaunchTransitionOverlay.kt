package com.example.animations

import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.builtinapps.BuiltinIosAppContent
import com.example.homescreen.IosStatusBar
import com.example.icons.IosAppIconGraphic
import com.example.launcher.ActiveAppSession
import com.example.launcher.IosBuiltinAppId
import com.example.launcher.LauncherApp
import com.example.launcher.LauncherUiState
import com.example.launcher.WallpaperPreset
import com.example.settings.IosSettingsApp
import com.example.theme.SfProDisplay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun AppLaunchTransitionOverlay(
    session: ActiveAppSession,
    isClosing: Boolean,
    isExternalReturn: Boolean,
    state: LauncherUiState,
    onProgressChanged: (Float) -> Unit,
    onRequestClose: () -> Unit,
    onOpenSwitcher: () -> Unit,
    onCloseFinished: () -> Unit,
    onLaunchApp: (LauncherApp, View?) -> Unit,
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
    onOpenCamera: () -> Unit,
    onSaveNote: (String, String, String?) -> Unit,
    onDeleteNote: (String) -> Unit,
    onToggleReminder: (String) -> Unit,
    onAddReminder: (String) -> Unit
) {
    val progressAnim = remember(session.app.id, isExternalReturn) {
        Animatable(if (isExternalReturn) 1f else 0f)
    }
    var dragOffsetY by remember(session.app.id) { mutableFloatStateOf(0f) }
    var dragOffsetX by remember(session.app.id) { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    BackHandler(enabled = !isExternalReturn && !isClosing) {
        onRequestClose()
    }

    LaunchedEffect(session.app.id, isClosing, isExternalReturn) {
        if (isExternalReturn || isClosing) {
            onProgressChanged(progressAnim.value)
            progressAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.86f,
                    stiffness = 380f
                )
            ) {
                onProgressChanged(value)
            }
            onCloseFinished()
        } else {
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.86f,
                    stiffness = 360f
                )
            ) {
                onProgressChanged(value)
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWPx = with(density) { maxWidth.toPx() }
        val screenHPx = with(density) { maxHeight.toPx() }

        val p = progressAnim.value.coerceIn(0f, 1f)
        val dragUpFactor = ((-dragOffsetY) / (screenHPx * 0.45f)).coerceIn(0f, 0.52f)
        val effectiveScale = (1f - dragUpFactor * 0.55f).coerceIn(0.45f, 1f)

        val origin = session.originRect
        val targetLeft = if (dragOffsetY < 0f) {
            (screenWPx * (1f - effectiveScale) * 0.5f) + dragOffsetX * 0.4f
        } else 0f
        val targetTop = if (dragOffsetY < 0f) {
            (screenHPx * (1f - effectiveScale) * 0.5f) + dragOffsetY * 0.45f
        } else 0f
        val targetW = screenWPx * effectiveScale
        val targetH = screenHPx * effectiveScale

        val curLeft = lerp(origin.left, targetLeft, p)
        val curTop = lerp(origin.top, targetTop, p)
        val curWidth = lerp(origin.width, targetW, p).coerceAtLeast(1f)
        val curHeight = lerp(origin.height, targetH, p).coerceAtLeast(1f)

        // Authentic iOS corner morph: icon radius (14dp) -> mid-flight pill (42dp) -> screen corner (0dp / 42dp when dragged)
        val midCurve = (1f - abs(p - 0.4f) * 2.5f).coerceIn(0f, 1f)
        val settledCorner = if (dragOffsetY < -10f) 40f else 0f
        val cornerDp = (lerp(14f, settledCorner, p) + midCurve * 26f).coerceIn(0f, 44f).dp

        val appContentAlpha = ((p - 0.16f) / 0.34f).coerceIn(0f, 1f)
        val iconCoverAlpha = (1f - (p / 0.32f)).coerceIn(0f, 1f)

        val curWidthDp = with(density) { curWidth.toDp() }
        val curHeightDp = with(density) { curHeight.toDp() }

        val isDark = state.settings.darkMode
        val surfaceColor = if (session.app.builtinId == IosBuiltinAppId.SETTINGS && !isDark) {
            Color(0xFFF2F2F7)
        } else {
            Color.Black
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(curLeft.roundToInt(), curTop.roundToInt()) }
                .width(curWidthDp)
                .height(curHeightDp)
                .clip(RoundedCornerShape(cornerDp))
                .background(surfaceColor)
        ) {
            // Live App Content (fades in smoothly as window expands)
            if (appContentAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(appContentAlpha)
                ) {
                    if (session.app.isBuiltin) {
                        val isLightStatus = !(session.app.builtinId == IosBuiltinAppId.SETTINGS && !isDark)
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
                                isLightContent = isLightStatus,
                                showDynamicIsland = true
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                if (session.app.builtinId == IosBuiltinAppId.SETTINGS) {
                                    IosSettingsApp(
                                        state = state,
                                        onSetDarkMode = onSetDarkMode,
                                        onSetBatteryPercent = onSetBatteryPercent,
                                        onSetLowPower = onSetLowPower,
                                        onSetShowBadges = onSetShowBadges,
                                        onSetShowLabels = onSetShowLabels,
                                        onSetWallpaperPreset = onSetWallpaperPreset,
                                        onSetCustomWallpaperUri = onSetCustomWallpaperUri,
                                        onSetProfileName = onSetProfileName,
                                        onBrightnessChange = onBrightnessChange,
                                        onResetLayout = onResetLayout,
                                        onOpenHomeChooser = onOpenHomeChooser,
                                        onOpenNotificationAccess = onOpenNotificationAccess,
                                        onOpenSystemSettings = onOpenSystemSettings,
                                        onOpenCamera = onOpenCamera
                                    )
                                } else {
                                    BuiltinIosAppContent(
                                        builtinId = session.app.builtinId!!,
                                        state = state,
                                        onLaunchApp = onLaunchApp,
                                        onSaveNote = onSaveNote,
                                        onDeleteNote = onDeleteNote,
                                        onToggleReminder = onToggleReminder,
                                        onAddReminder = onAddReminder,
                                        onSetWallpaperPreset = onSetWallpaperPreset
                                    )
                                }
                            }

                            // Interactive iOS Home Indicator Bar at bottom!
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .pointerInput(session.app.id) {
                                        detectVerticalDragGestures(
                                            onDragStart = {
                                                dragOffsetY = 0f
                                                dragOffsetX = 0f
                                            },
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY = (dragOffsetY + dragAmount).coerceAtMost(0f)
                                                val progressDuringDrag = (1f - ((-dragOffsetY) / (screenHPx * 0.7f))).coerceIn(0.45f, 1f)
                                                onProgressChanged(progressDuringDrag)
                                            },
                                            onDragEnd = {
                                                if (dragOffsetY < -110f) {
                                                    if (dragOffsetY < -screenHPx * 0.42f) {
                                                        dragOffsetY = 0f
                                                        onOpenSwitcher()
                                                    } else {
                                                        onRequestClose()
                                                    }
                                                } else {
                                                    scope.launch {
                                                        dragOffsetY = 0f
                                                        onProgressChanged(1f)
                                                    }
                                                }
                                            },
                                            onDragCancel = {
                                                dragOffsetY = 0f
                                                onProgressChanged(1f)
                                            }
                                        )
                                    }
                                    .testTag("ios_home_indicator"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(136.dp)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (isLightStatus) Color.White.copy(alpha = 0.85f)
                                            else Color.Black.copy(alpha = 0.75f)
                                        )
                                )
                            }
                        }
                    } else {
                        // External Android App Launch / Return Splash Card
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF121318))
                                .statusBarsPadding(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(96.dp)) {
                                    IosAppIconGraphic(
                                        iconKey = session.app.iconKey,
                                        fallbackBitmap = session.app.fallbackBitmap,
                                        fallbackTintColor = session.app.fallbackTintColor,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Text(
                                    text = session.app.label,
                                    fontFamily = SfProDisplay,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Icon Morph Cover (visible at start of open & end of close so it merges seamlessly with home icon!)
            if (iconCoverAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(iconCoverAlpha)
                ) {
                    IosAppIconGraphic(
                        iconKey = session.app.iconKey,
                        fallbackBitmap = session.app.fallbackBitmap,
                        fallbackTintColor = session.app.fallbackTintColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
