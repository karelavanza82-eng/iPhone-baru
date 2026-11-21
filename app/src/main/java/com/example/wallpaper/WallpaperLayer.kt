package com.example.wallpaper

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.R
import com.example.launcher.WallpaperPreset

@Composable
fun WallpaperLayer(
    preset: WallpaperPreset,
    customUri: String?,
    blurRadius: Dp,
    dimAlpha: Float,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resId = when (preset) {
        WallpaperPreset.IOS_BLUE -> R.drawable.img_wallpaper_ios_blue
        WallpaperPreset.IOS_AURORA -> R.drawable.img_wallpaper_ios_aurora
        WallpaperPreset.IOS_EMERALD -> R.drawable.img_wallpaper_ios_emerald
    }

    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.5.dp) {
        Modifier.blur(radius = blurRadius)
    } else {
        Modifier
    }

    Box(modifier = modifier.fillMaxSize()) {
        val painter = if (!customUri.isNullOrBlank()) {
            rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(customUri))
                    .crossfade(true)
                    .build()
            )
        } else {
            painterResource(id = resId)
        }

        Image(
            painter = painter,
            contentDescription = "Home screen wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .then(blurModifier)
        )

        val fallbackBlurScrim = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && blurRadius > 0.5.dp) {
            (blurRadius.value / 32f).coerceIn(0f, 0.45f)
        } else {
            0f
        }

        val totalDim = (dimAlpha + fallbackBlurScrim).coerceIn(0f, 0.82f)
        if (totalDim > 0.005f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = totalDim))
            )
        }
    }
}
