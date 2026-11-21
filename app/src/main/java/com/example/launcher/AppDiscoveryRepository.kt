package com.example.launcher

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import com.example.icons.IosIconKey
import com.example.icons.IosIconMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

class AppDiscoveryRepository(private val context: Context) {

    private val bitmapCache = ConcurrentHashMap<String, ImageBitmap>()
    private val tintCache = ConcurrentHashMap<String, Color>()

    suspend fun loadAllApps(): List<LauncherApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val installedApps = resolveInfos
            .asSequence()
            .filter { it.activityInfo != null && it.activityInfo.packageName != context.packageName }
            .distinctBy { "${it.activityInfo.packageName}/${it.activityInfo.name}" }
            .map { info ->
                val pkg = info.activityInfo.packageName
                val act = info.activityInfo.name
                val label = info.loadLabel(pm)?.toString()?.trim().takeUnless { it.isNullOrEmpty() }
                    ?: pkg.substringAfterLast('.')
                val iconKey = IosIconMapper.resolveIconKey(pkg, label)
                val cachedPair = if (iconKey == null) loadAndCacheDrawable(pm, info, pkg) else null
                val installTime = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0L)).firstInstallTime
                    } else {
                        @Suppress("DEPRECATION")
                        pm.getPackageInfo(pkg, 0).firstInstallTime
                    }
                } catch (_: Throwable) {
                    0L
                }
                val category = resolveCategory(info.activityInfo.applicationInfo, pkg, label)
                LauncherApp(
                    id = "pkg:$pkg/$act",
                    label = label,
                    packageName = pkg,
                    activityName = act,
                    iconKey = iconKey,
                    fallbackBitmap = cachedPair?.first,
                    fallbackTintColor = cachedPair?.second ?: Color(0xFF2C2C2E),
                    category = category,
                    builtinId = null,
                    installTimeMillis = installTime
                )
            }
            .sortedBy { it.label.lowercase(Locale.ROOT) }
            .toList()

        val builtinApps = createBuiltinIosApps()
        builtinApps + installedApps
    }

    fun createBuiltinIosApps(): List<LauncherApp> {
        return listOf(
            builtinApp(IosBuiltinAppId.SETTINGS, AppCategory.UTILITIES),
            builtinApp(IosBuiltinAppId.CLOCK, AppCategory.UTILITIES),
            builtinApp(IosBuiltinAppId.CALENDAR, AppCategory.PRODUCTIVITY),
            builtinApp(IosBuiltinAppId.PHOTOS, AppCategory.CREATIVITY),
            builtinApp(IosBuiltinAppId.WEATHER, AppCategory.INFORMATION),
            builtinApp(IosBuiltinAppId.NOTES, AppCategory.PRODUCTIVITY),
            builtinApp(IosBuiltinAppId.REMINDERS, AppCategory.PRODUCTIVITY),
            builtinApp(IosBuiltinAppId.FILES, AppCategory.UTILITIES),
            builtinApp(IosBuiltinAppId.APP_STORE, AppCategory.ENTERTAINMENT),
            builtinApp(IosBuiltinAppId.CALCULATOR, AppCategory.UTILITIES)
        )
    }

    private fun builtinApp(id: IosBuiltinAppId, category: AppCategory): LauncherApp {
        return LauncherApp(
            id = id.routeId,
            label = id.defaultTitle,
            packageName = context.packageName,
            activityName = null,
            iconKey = id.iconKey,
            fallbackBitmap = null,
            fallbackTintColor = Color(0xFF1C1C1E),
            category = category,
            builtinId = id,
            installTimeMillis = 0L
        )
    }

    fun launchExternalApp(
        app: LauncherApp,
        originRect: Rect,
        anchorView: View?
    ): Boolean {
        return try {
            val intent = if (!app.activityName.isNullOrBlank()) {
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    component = ComponentName(app.packageName, app.activityName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                }
            } else {
                context.packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                }
            } ?: return false

            val bundle = if (anchorView != null && originRect.width > 0f && originRect.height > 0f) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityOptions.makeClipRevealAnimation(
                        anchorView,
                        originRect.left.roundToInt(),
                        originRect.top.roundToInt(),
                        originRect.width.roundToInt(),
                        originRect.height.roundToInt()
                    ).toBundle()
                } else {
                    ActivityOptions.makeScaleUpAnimation(
                        anchorView,
                        originRect.left.roundToInt(),
                        originRect.top.roundToInt(),
                        originRect.width.roundToInt(),
                        originRect.height.roundToInt()
                    ).toBundle()
                }
            } else {
                null
            }
            context.startActivity(intent, bundle)
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun loadAndCacheDrawable(
        pm: PackageManager,
        info: ResolveInfo,
        pkg: String
    ): Pair<ImageBitmap, Color>? {
        val existingBmp = bitmapCache[pkg]
        val existingTint = tintCache[pkg]
        if (existingBmp != null && existingTint != null) {
            return existingBmp to existingTint
        }
        return try {
            val drawable = info.loadIcon(pm) ?: return null
            val bitmap = drawableToBitmap(drawable, 144)
            val tint = extractDominantTint(bitmap)
            val imageBmp = bitmap.asImageBitmap()
            bitmapCache[pkg] = imageBmp
            tintCache[pkg] = tint
            imageBmp to tint
        } catch (_: Throwable) {
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable, targetSizePx: Int): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return Bitmap.createScaledBitmap(drawable.bitmap, targetSizePx, targetSizePx, true)
        }
        val bitmap = Bitmap.createBitmap(targetSizePx, targetSizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun extractDominantTint(bitmap: Bitmap): Color {
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        var count = 0
        val step = 12
        for (x in 0 until bitmap.width step step) {
            for (y in 0 until bitmap.height step step) {
                val px = bitmap.getPixel(x, y)
                val alpha = (px ushr 24) and 0xFF
                if (alpha > 180) {
                    val r = (px ushr 16) and 0xFF
                    val g = (px ushr 8) and 0xFF
                    val b = px and 0xFF
                    if (!(r > 242 && g > 242 && b > 242)) {
                        rSum += r
                        gSum += g
                        bSum += b
                        count++
                    }
                }
            }
        }
        if (count == 0) return Color(0xFF2C2C2E)
        val avgR = (rSum / count).toInt().coerceIn(30, 225)
        val avgG = (gSum / count).toInt().coerceIn(30, 225)
        val avgB = (bSum / count).toInt().coerceIn(30, 225)
        return Color(android.graphics.Color.rgb(avgR, avgG, avgB))
    }

    private fun resolveCategory(appInfo: ApplicationInfo?, pkg: String, label: String): AppCategory {
        val p = pkg.lowercase(Locale.ROOT)
        val l = label.lowercase(Locale.ROOT)
        if (p.contains("whatsapp") || p.contains("instagram") || p.contains("tiktok") ||
            p.contains("musically") || p.contains("facebook") || p.contains("telegram") ||
            p.contains("discord") || p.contains("messaging") || p.contains("dialer") ||
            l.contains("chat") || l.contains("social")
        ) {
            return AppCategory.SOCIAL
        }
        if (p.contains("youtube") || p.contains("spotify") || p.contains("netflix") ||
            p.contains("music") || p.contains("video") || p.contains("game")
        ) {
            return AppCategory.ENTERTAINMENT
        }
        if (p.contains("camera") || p.contains("photos") || p.contains("gallery") || p.contains("pinterest")) {
            return AppCategory.CREATIVITY
        }
        if (p.contains("gm") || p.contains("docs") || p.contains("calendar") || p.contains("keep") ||
            p.contains("notes") || p.contains("wallet") || p.contains("drive")
        ) {
            return AppCategory.PRODUCTIVITY
        }
        if (p.contains("chrome") || p.contains("browser") || p.contains("maps") || p.contains("weather")) {
            return AppCategory.INFORMATION
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo != null) {
            return when (appInfo.category) {
                ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_GAME -> AppCategory.ENTERTAINMENT
                ApplicationInfo.CATEGORY_IMAGE -> AppCategory.CREATIVITY
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
                ApplicationInfo.CATEGORY_NEWS,
                ApplicationInfo.CATEGORY_MAPS -> AppCategory.INFORMATION
                else -> AppCategory.UTILITIES
            }
        }
        return AppCategory.UTILITIES
    }
}
