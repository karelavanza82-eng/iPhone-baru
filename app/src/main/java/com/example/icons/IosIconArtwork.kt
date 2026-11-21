package com.example.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.theme.SfProDisplay
import com.example.theme.SfProText
import com.example.theme.iosSquircleShape
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IosAppIconGraphic(
    iconKey: IosIconKey?,
    fallbackBitmap: ImageBitmap?,
    fallbackTintColor: Color,
    modifier: Modifier = Modifier,
    animateClock: Boolean = true
) {
    val squircle = remember { iosSquircleShape() }
    Box(
        modifier = modifier
            .clip(squircle)
    ) {
        if (iconKey != null) {
            when (iconKey) {
                IosIconKey.CLOCK -> LiveClockIconCanvas(animate = animateClock)
                IosIconKey.CALENDAR -> DynamicCalendarIconCanvas()
                else -> StaticIosIconCanvas(iconKey = iconKey)
            }
        } else {
            FallbackAndroidAppIcon(
                bitmap = fallbackBitmap,
                tintColor = fallbackTintColor
            )
        }
    }
}

@Composable
private fun FallbackAndroidAppIcon(
    bitmap: ImageBitmap?,
    tintColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        tintColor.copy(alpha = 0.96f),
                        Color(
                            red = (tintColor.red * 0.72f).coerceIn(0f, 1f),
                            green = (tintColor.green * 0.72f).coerceIn(0f, 1f),
                            blue = (tintColor.blue * 0.72f).coerceIn(0f, 1f),
                            alpha = 1f
                        )
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun LiveClockIconCanvas(animate: Boolean) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(animate) {
        while (animate) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)
        drawRect(color = Color(0xFF1C1C1E))
        val dialRadius = w * 0.43f
        drawCircle(color = Color.White, radius = dialRadius, center = c)

        val numRadius = dialRadius * 0.75f
        val numSizeSp = (w * 0.115f / density).sp
        for (i in 1..12) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val nx = c.x + (numRadius * cos(angle)).toFloat()
            val ny = c.y + (numRadius * sin(angle)).toFloat()
            val measured = textMeasurer.measure(
                text = i.toString(),
                style = TextStyle(
                    fontFamily = SfProDisplay,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = numSizeSp,
                    color = Color(0xFF141414)
                )
            )
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(nx - measured.size.width / 2f, ny - measured.size.height / 2f)
            )
        }

        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val sec = cal.get(Calendar.SECOND)
        val min = cal.get(Calendar.MINUTE) + sec / 60f
        val hr = (cal.get(Calendar.HOUR) % 12) + min / 60f

        val hrAngle = hr * 30f
        val minAngle = min * 6f
        val secAngle = sec * 6f

        rotate(hrAngle, pivot = c) {
            drawLine(
                color = Color(0xFF141414),
                start = Offset(c.x, c.y + dialRadius * 0.08f),
                end = Offset(c.x, c.y - dialRadius * 0.50f),
                strokeWidth = w * 0.042f,
                cap = StrokeCap.Round
            )
        }
        rotate(minAngle, pivot = c) {
            drawLine(
                color = Color(0xFF141414),
                start = Offset(c.x, c.y + dialRadius * 0.10f),
                end = Offset(c.x, c.y - dialRadius * 0.76f),
                strokeWidth = w * 0.030f,
                cap = StrokeCap.Round
            )
        }
        rotate(secAngle, pivot = c) {
            drawLine(
                color = Color(0xFFFF9500),
                start = Offset(c.x, c.y + dialRadius * 0.18f),
                end = Offset(c.x, c.y - dialRadius * 0.84f),
                strokeWidth = w * 0.015f,
                cap = StrokeCap.Round
            )
        }
        drawCircle(color = Color(0xFFFF9500), radius = w * 0.032f, center = c)
        drawCircle(color = Color.White, radius = w * 0.012f, center = c)
    }
}

@Composable
private fun DynamicCalendarIconCanvas() {
    val cal = remember { Calendar.getInstance() }
    val dayOfWeek = remember {
        cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)
            ?.uppercase(Locale.ENGLISH) ?: "FRI"
    }
    val dayNum = remember { cal.get(Calendar.DAY_OF_MONTH).toString() }
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(color = Color(0xFFFAFAFC))

        val dayLabel = textMeasurer.measure(
            text = dayOfWeek,
            style = TextStyle(
                fontFamily = SfProText,
                fontWeight = FontWeight.Bold,
                fontSize = (w * 0.17f / density).sp,
                color = Color(0xFFFF3B30),
                letterSpacing = 0.8.sp
            )
        )
        drawText(
            textLayoutResult = dayLabel,
            topLeft = Offset((w - dayLabel.size.width) / 2f, h * 0.12f)
        )

        val dateNum = textMeasurer.measure(
            text = dayNum,
            style = TextStyle(
                fontFamily = SfProDisplay,
                fontWeight = FontWeight.Normal,
                fontSize = (w * 0.50f / density).sp,
                color = Color(0xFF1C1C1E)
            )
        )
        drawText(
            textLayoutResult = dateNum,
            topLeft = Offset((w - dateNum.size.width) / 2f, h * 0.31f)
        )
    }
}

@Composable
private fun StaticIosIconCanvas(iconKey: IosIconKey) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)

        when (iconKey) {
            IosIconKey.PHONE -> drawPhoneIcon(w, h, c)
            IosIconKey.MESSAGES -> drawMessagesIcon(w, h, c)
            IosIconKey.FACETIME -> drawFaceTimeIcon(w, h, c)
            IosIconKey.SAFARI -> drawSafariIcon(w, h, c)
            IosIconKey.SETTINGS -> drawSettingsIcon(w, h, c)
            IosIconKey.PHOTOS -> drawPhotosIcon(w, h, c)
            IosIconKey.CAMERA -> drawCameraIcon(w, h, c)
            IosIconKey.APP_STORE -> drawAppStoreIcon(w, h, c)
            IosIconKey.MUSIC -> drawMusicIcon(w, h, c)
            IosIconKey.WEATHER -> drawWeatherIcon(w, h, c)
            IosIconKey.MAPS -> drawMapsIcon(w, h, c, textMeasurer)
            IosIconKey.NOTES -> drawNotesIcon(w, h)
            IosIconKey.REMINDERS -> drawRemindersIcon(w, h)
            IosIconKey.FILES -> drawFilesIcon(w, h, c)
            IosIconKey.HEALTH -> drawHealthIcon(w, h)
            IosIconKey.WALLET -> drawWalletIcon(w, h)
            IosIconKey.MAIL -> drawMailIcon(w, h, c)
            IosIconKey.YOUTUBE -> drawYouTubeIcon(w, h, c)
            IosIconKey.YOUTUBE_MUSIC -> drawYouTubeMusicIcon(w, h, c)
            IosIconKey.TIKTOK -> drawTikTokIcon(w, h, c)
            IosIconKey.INSTAGRAM -> drawInstagramIcon(w, h, c)
            IosIconKey.WHATSAPP -> drawWhatsAppIcon(w, h, c)
            IosIconKey.FACEBOOK -> drawFacebookIcon(w, h, c, textMeasurer)
            IosIconKey.GMAIL -> drawGmailIcon(w, h, c)
            IosIconKey.SPOTIFY -> drawSpotifyIcon(w, h, c)
            IosIconKey.TELEGRAM -> drawTelegramIcon(w, h, c)
            IosIconKey.DISCORD -> drawDiscordIcon(w, h, c)
            IosIconKey.APPLE_TV -> drawAppleTvIcon(w, h, c, textMeasurer)
            IosIconKey.GAMES -> drawGamesIcon(w, h, c)
            IosIconKey.TIPS -> drawTipsIcon(w, h, c)
            IosIconKey.CALCULATOR -> drawCalculatorIcon(w, h)
            IosIconKey.KEEP -> drawKeepIcon(w, h, c)
            IosIconKey.DRIVE -> drawDriveIcon(w, h, c)
            IosIconKey.PINTEREST -> drawPinterestIcon(w, h, c, textMeasurer)
            IosIconKey.CHROME -> drawChromeIcon(w, h, c)
            IosIconKey.GEMINI -> drawGeminiIcon(w, h, c)
            IosIconKey.CLOCK, IosIconKey.CALENDAR -> Unit
        }
    }
}

private fun DrawScope.drawGreenGradientBg() {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF67FD7B), Color(0xFF1EC842))
        )
    )
}

private fun DrawScope.drawPhoneReceiverPath(cx: Float, cy: Float, scale: Float, color: Color) {
    val p = Path().apply {
        moveTo(cx - 0.24f * scale, cy - 0.26f * scale)
        cubicTo(
            cx - 0.30f * scale, cy - 0.18f * scale,
            cx - 0.28f * scale, cy - 0.02f * scale,
            cx - 0.14f * scale, cy + 0.14f * scale
        )
        cubicTo(
            cx + 0.02f * scale, cy + 0.28f * scale,
            cx + 0.18f * scale, cy + 0.30f * scale,
            cx + 0.26f * scale, cy + 0.24f * scale
        )
        lineTo(cx + 0.31f * scale, cy + 0.15f * scale)
        cubicTo(
            cx + 0.33f * scale, cy + 0.11f * scale,
            cx + 0.28f * scale, cy + 0.07f * scale,
            cx + 0.21f * scale, cy + 0.04f * scale
        )
        lineTo(cx + 0.14f * scale, cy + 0.08f * scale)
        cubicTo(
            cx + 0.08f * scale, cy + 0.11f * scale,
            cx + 0.01f * scale, cy + 0.05f * scale,
            cx - 0.05f * scale, cy - 0.01f * scale
        )
        cubicTo(
            cx - 0.11f * scale, cy - 0.07f * scale,
            cx - 0.11f * scale, cy - 0.12f * scale,
            cx - 0.08f * scale, cy - 0.16f * scale
        )
        lineTo(cx - 0.04f * scale, cy - 0.22f * scale)
        cubicTo(
            cx - 0.06f * scale, cy - 0.28f * scale,
            cx - 0.11f * scale, cy - 0.32f * scale,
            cx - 0.16f * scale, cy - 0.30f * scale
        )
        close()
    }
    drawPath(p, color = color)
}

private fun DrawScope.drawPhoneIcon(w: Float, h: Float, c: Offset) {
    drawGreenGradientBg()
    drawPhoneReceiverPath(c.x, c.y, w * 0.92f, Color.White)
}

private fun DrawScope.drawMessagesIcon(w: Float, h: Float, c: Offset) {
    drawGreenGradientBg()
    drawOval(
        color = Color.White,
        topLeft = Offset(w * 0.16f, h * 0.19f),
        size = Size(w * 0.68f, h * 0.56f)
    )
    val tail = Path().apply {
        moveTo(w * 0.28f, h * 0.67f)
        quadraticTo(w * 0.23f, h * 0.78f, w * 0.16f, h * 0.81f)
        quadraticTo(w * 0.31f, h * 0.79f, w * 0.40f, h * 0.72f)
        close()
    }
    drawPath(tail, color = Color.White)
}

private fun DrawScope.drawFaceTimeIcon(w: Float, h: Float, c: Offset) {
    drawGreenGradientBg()
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(w * 0.16f, h * 0.31f),
        size = Size(w * 0.46f, h * 0.38f),
        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
    )
    val lens = Path().apply {
        moveTo(w * 0.64f, h * 0.43f)
        lineTo(w * 0.83f, h * 0.32f)
        quadraticTo(w * 0.86f, h * 0.31f, w * 0.86f, h * 0.35f)
        lineTo(w * 0.86f, h * 0.65f)
        quadraticTo(w * 0.86f, h * 0.69f, w * 0.83f, h * 0.68f)
        lineTo(w * 0.64f, h * 0.57f)
        close()
    }
    drawPath(lens, color = Color.White)
}

private fun DrawScope.drawSafariIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color(0xFFFAFAFC))
    val dialR = w * 0.41f
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF2CB5FF), Color(0xFF0064E6))
        ),
        radius = dialR,
        center = c
    )
    for (i in 0 until 36) {
        val deg = i * 10.0
        val rad = Math.toRadians(deg)
        val inner = if (i % 3 == 0) dialR * 0.80f else dialR * 0.86f
        val outer = dialR * 0.94f
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(c.x + (inner * cos(rad)).toFloat(), c.y + (inner * sin(rad)).toFloat()),
            end = Offset(c.x + (outer * cos(rad)).toFloat(), c.y + (outer * sin(rad)).toFloat()),
            strokeWidth = if (i % 3 == 0) w * 0.016f else w * 0.010f,
            cap = StrokeCap.Round
        )
    }
    rotate(45f, pivot = c) {
        val redNeedle = Path().apply {
            moveTo(c.x, c.y - dialR * 0.78f)
            lineTo(c.x + dialR * 0.14f, c.y)
            lineTo(c.x - dialR * 0.14f, c.y)
            close()
        }
        drawPath(redNeedle, color = Color(0xFFFF3B30))
        val whiteNeedle = Path().apply {
            moveTo(c.x, c.y + dialR * 0.78f)
            lineTo(c.x + dialR * 0.14f, c.y)
            lineTo(c.x - dialR * 0.14f, c.y)
            close()
        }
        drawPath(whiteNeedle, color = Color.White)
    }
}

private fun DrawScope.drawSettingsIcon(w: Float, h: Float, c: Offset) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFE3E6EB), Color(0xFF9EA5AE))
        )
    )
    drawCircle(
        color = Color(0xFF2C2F33),
        radius = w * 0.40f,
        center = c,
        style = Stroke(width = w * 0.025f)
    )
    // Outer gear teeth
    for (i in 0 until 24) {
        rotate(i * 15f, pivot = c) {
            drawRoundRect(
                color = Color(0xFF565B63),
                topLeft = Offset(c.x - w * 0.028f, c.y - w * 0.41f),
                size = Size(w * 0.056f, w * 0.12f),
                cornerRadius = CornerRadius(w * 0.015f, w * 0.015f)
            )
        }
    }
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF8C929B), Color(0xFF494D54))
        ),
        radius = w * 0.34f,
        center = c
    )
    drawCircle(color = Color(0xFF24272B), radius = w * 0.25f, center = c)
    for (i in 0 until 3) {
        rotate(i * 120f, pivot = c) {
            drawLine(
                color = Color(0xFF6B7078),
                start = c,
                end = Offset(c.x, c.y - w * 0.25f),
                strokeWidth = w * 0.06f
            )
        }
    }
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFD5D9DF), Color(0xFF8A9099))
        ),
        radius = w * 0.11f,
        center = c
    )
    drawCircle(color = Color(0xFF2B2E33), radius = w * 0.05f, center = c)
}

private fun DrawScope.drawPhotosIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color.White)
    val petalColors = listOf(
        Color(0xFFFF9500), // top orange
        Color(0xFFFFCC00), // top-right yellow
        Color(0xFF7ED321), // right lime
        Color(0xFF34C759), // bottom-right green
        Color(0xFF32ADE6), // bottom cyan
        Color(0xFF007AFF), // bottom-left blue
        Color(0xFFAF52DE), // left purple
        Color(0xFFFF2D55)  // top-left pink-red
    )
    val pw = w * 0.22f
    val ph = w * 0.36f
    for (i in 0 until 8) {
        rotate(i * 45f, pivot = c) {
            drawRoundRect(
                color = petalColors[i].copy(alpha = 0.82f),
                topLeft = Offset(c.x - pw / 2f, c.y - ph - w * 0.03f),
                size = Size(pw, ph),
                cornerRadius = CornerRadius(pw / 2f, pw / 2f)
            )
        }
    }
}

private fun DrawScope.drawCameraIcon(w: Float, h: Float, c: Offset) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFEBEEF2), Color(0xFFB3B9C2))
        )
    )
    val body = Path().apply {
        moveTo(w * 0.18f, h * 0.34f)
        lineTo(w * 0.31f, h * 0.34f)
        lineTo(w * 0.36f, h * 0.25f)
        lineTo(w * 0.64f, h * 0.25f)
        lineTo(w * 0.69f, h * 0.34f)
        lineTo(w * 0.82f, h * 0.34f)
        quadraticTo(w * 0.88f, h * 0.34f, w * 0.88f, h * 0.40f)
        lineTo(w * 0.88f, h * 0.72f)
        quadraticTo(w * 0.88f, h * 0.78f, w * 0.82f, h * 0.78f)
        lineTo(w * 0.18f, h * 0.78f)
        quadraticTo(w * 0.12f, h * 0.78f, w * 0.12f, h * 0.72f)
        lineTo(w * 0.12f, h * 0.40f)
        quadraticTo(w * 0.12f, h * 0.34f, w * 0.18f, h * 0.34f)
        close()
    }
    drawPath(body, color = Color(0xFF1D1E22))
    val lensCenter = Offset(c.x, h * 0.55f)
    drawCircle(color = Color(0xFF434852), radius = w * 0.17f, center = lensCenter)
    drawCircle(color = Color(0xFF0F1116), radius = w * 0.12f, center = lensCenter)
    drawCircle(color = Color(0xFF1E4C7A), radius = w * 0.06f, center = Offset(lensCenter.x - w * 0.02f, lensCenter.y - w * 0.02f))
    drawCircle(color = Color(0xFFFFCC00), radius = w * 0.022f, center = Offset(w * 0.74f, h * 0.42f))
}

private fun DrawScope.drawAppStoreIcon(w: Float, h: Float, c: Offset) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF19C6FF), Color(0xFF0E68F3))
        )
    )
    val stroke = w * 0.088f
    drawLine(
        color = Color.White,
        start = Offset(w * 0.50f, h * 0.24f),
        end = Offset(w * 0.26f, h * 0.72f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White,
        start = Offset(w * 0.47f, h * 0.24f),
        end = Offset(w * 0.74f, h * 0.72f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White,
        start = Offset(w * 0.22f, h * 0.58f),
        end = Offset(w * 0.78f, h * 0.58f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawMusicIcon(w: Float, h: Float, c: Offset) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFC5C70), Color(0xFFFA1F3A))
        )
    )
    val beam = Path().apply {
        moveTo(w * 0.40f, h * 0.26f)
        lineTo(w * 0.74f, h * 0.20f)
        lineTo(w * 0.74f, h * 0.32f)
        lineTo(w * 0.40f, h * 0.38f)
        close()
    }
    drawPath(beam, color = Color.White)
    drawLine(
        color = Color.White,
        start = Offset(w * 0.42f, h * 0.30f),
        end = Offset(w * 0.42f, h * 0.69f),
        strokeWidth = w * 0.068f
    )
    drawLine(
        color = Color.White,
        start = Offset(w * 0.71f, h * 0.24f),
        end = Offset(w * 0.71f, h * 0.62f),
        strokeWidth = w * 0.068f
    )
    drawOval(
        color = Color.White,
        topLeft = Offset(w * 0.24f, h * 0.62f),
        size = Size(w * 0.21f, h * 0.16f)
    )
    drawOval(
        color = Color.White,
        topLeft = Offset(w * 0.53f, h * 0.55f),
        size = Size(w * 0.21f, h * 0.16f)
    )
}

private fun DrawScope.drawWeatherIcon(w: Float, h: Float, c: Offset) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1F6FEB), Color(0xFF56B7FF))
        )
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFEB3B), Color(0xFFFF9800)),
            center = Offset(w * 0.42f, h * 0.42f),
            radius = w * 0.24f
        ),
        radius = w * 0.20f,
        center = Offset(w * 0.41f, h * 0.42f)
    )
    // Cloud
    drawCircle(color = Color.White.copy(alpha = 0.94f), radius = w * 0.18f, center = Offset(w * 0.52f, h * 0.52f))
    drawCircle(color = Color.White.copy(alpha = 0.94f), radius = w * 0.13f, center = Offset(w * 0.35f, h * 0.58f))
    drawCircle(color = Color.White.copy(alpha = 0.94f), radius = w * 0.14f, center = Offset(w * 0.68f, h * 0.57f))
    drawRoundRect(
        color = Color.White.copy(alpha = 0.94f),
        topLeft = Offset(w * 0.23f, h * 0.54f),
        size = Size(w * 0.56f, h * 0.18f),
        cornerRadius = CornerRadius(w * 0.09f, w * 0.09f)
    )
}

private fun DrawScope.drawMapsIcon(
    w: Float,
    h: Float,
    c: Offset,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    drawRect(color = Color(0xFFF3EFE6))
    // Green park quadrant top-left
    drawRect(color = Color(0xFF8CE27B), topLeft = Offset(0f, 0f), size = Size(w * 0.44f, h * 0.44f))
    // Blue river diagonal right
    drawLine(
        color = Color(0xFF66B3FF),
        start = Offset(w * 0.58f, -h * 0.05f),
        end = Offset(w * 1.05f, h * 0.45f),
        strokeWidth = w * 0.18f
    )
    // Pink/purple building block bottom-left
    drawRect(color = Color(0xFFEBD2E8), topLeft = Offset(0f, h * 0.55f), size = Size(w * 0.40f, h * 0.45f))
    // Yellow highway vertical
    drawLine(
        color = Color(0xFFFFCE44),
        start = Offset(w * 0.50f, 0f),
        end = Offset(w * 0.50f, h),
        strokeWidth = w * 0.14f
    )
    // Gray crossroad
    drawLine(
        color = Color.White,
        start = Offset(0f, h * 0.50f),
        end = Offset(w, h * 0.50f),
        strokeWidth = w * 0.11f
    )
    // Navigation circle bottom-right
    drawCircle(color = Color.White, radius = w * 0.21f, center = Offset(w * 0.70f, h * 0.72f))
    drawCircle(color = Color(0xFF007AFF), radius = w * 0.17f, center = Offset(w * 0.70f, h * 0.72f))
    val arrow = Path().apply {
        moveTo(w * 0.70f, h * 0.62f)
        lineTo(w * 0.78f, h * 0.79f)
        lineTo(w * 0.70f, h * 0.75f)
        lineTo(w * 0.62f, h * 0.79f)
        close()
    }
    drawPath(arrow, color = Color.White)

    // Highway shield top-left
    drawRoundRect(
        color = Color(0xFF0066CC),
        topLeft = Offset(w * 0.14f, h * 0.24f),
        size = Size(w * 0.30f, h * 0.22f),
        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
    )
    drawRect(color = Color(0xFFFF3B30), topLeft = Offset(w * 0.14f, h * 0.24f), size = Size(w * 0.30f, h * 0.06f))
    val shieldText = textMeasurer.measure(
        text = "280",
        style = TextStyle(
            fontFamily = SfProText,
            fontWeight = FontWeight.Bold,
            fontSize = (w * 0.105f / density).sp,
            color = Color.White
        )
    )
    drawText(shieldText, topLeft = Offset(w * 0.29f - shieldText.size.width / 2f, h * 0.31f))
}

private fun DrawScope.drawNotesIcon(w: Float, h: Float) {
    drawRect(color = Color.White)
    drawRect(
        brush = Brush.verticalGradient(colors = listOf(Color(0xFFFFDE4A), Color(0xFFF6C400))),
        topLeft = Offset.Zero,
        size = Size(w, h * 0.27f)
    )
    for (yRatio in listOf(0.42f, 0.59f, 0.76f)) {
        drawLine(
            color = Color(0xFFDADAE0),
            start = Offset(0f, h * yRatio),
            end = Offset(w, h * yRatio),
            strokeWidth = w * 0.018f
        )
    }
}

private fun DrawScope.drawRemindersIcon(w: Float, h: Float) {
    drawRect(color = Color.White)
    val items = listOf(
        0.28f to Color(0xFF007AFF),
        0.50f to Color(0xFFFF3B30),
        0.72f to Color(0xFFFF9500)
    )
    items.forEach { (yRatio, dotColor) ->
        drawCircle(color = dotColor, radius = w * 0.068f, center = Offset(w * 0.23f, h * yRatio))
        drawLine(
            color = Color(0xFFC7C7CC),
            start = Offset(w * 0.38f, h * yRatio),
            end = Offset(w * 0.84f, h * yRatio),
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawFilesIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color.White)
    val folderBack = Path().apply {
        moveTo(w * 0.14f, h * 0.28f)
        lineTo(w * 0.40f, h * 0.28f)
        lineTo(w * 0.47f, h * 0.35f)
        lineTo(w * 0.86f, h * 0.35f)
        lineTo(w * 0.86f, h * 0.74f)
        lineTo(w * 0.14f, h * 0.74f)
        close()
    }
    drawPath(folderBack, color = Color(0xFF006EE6))
    drawRoundRect(
        brush = Brush.verticalGradient(colors = listOf(Color(0xFF4CC2FF), Color(0xFF007AFF))),
        topLeft = Offset(w * 0.14f, h * 0.38f),
        size = Size(w * 0.72f, h * 0.38f),
        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
    )
}

private fun DrawScope.drawHealthIcon(w: Float, h: Float) {
    drawRect(color = Color.White)
    val cx = w * 0.66f
    val cy = h * 0.36f
    val s = w * 0.22f
    val heart = Path().apply {
        moveTo(cx, cy + s * 0.75f)
        cubicTo(cx - s * 1.4f, cy - s * 0.1f, cx - s * 0.85f, cy - s * 1.1f, cx, cy - s * 0.40f)
        cubicTo(cx + s * 0.85f, cy - s * 1.1f, cx + s * 1.4f, cy - s * 0.1f, cx, cy + s * 0.75f)
        close()
    }
    drawPath(
        path = heart,
        brush = Brush.verticalGradient(colors = listOf(Color(0xFFFF6188), Color(0xFFFF2647)))
    )
}

private fun DrawScope.drawWalletIcon(w: Float, h: Float) {
    drawRect(color = Color(0xFF1C1C1E))
    // Card stripes peeking out
    drawRoundRect(
        color = Color(0xFF34AADC),
        topLeft = Offset(w * 0.14f, h * 0.18f),
        size = Size(w * 0.72f, h * 0.22f),
        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
    )
    drawRoundRect(
        color = Color(0xFFFF9500),
        topLeft = Offset(w * 0.14f, h * 0.26f),
        size = Size(w * 0.72f, h * 0.22f),
        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
    )
    drawRoundRect(
        color = Color(0xFF34C759),
        topLeft = Offset(w * 0.14f, h * 0.34f),
        size = Size(w * 0.72f, h * 0.22f),
        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
    )
    drawRoundRect(
        color = Color(0xFFFF3B30),
        topLeft = Offset(w * 0.14f, h * 0.42f),
        size = Size(w * 0.72f, h * 0.22f),
        cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
    )
    // Dark leather pocket
    drawRoundRect(
        color = Color(0xFF2C2C30),
        topLeft = Offset(w * 0.12f, h * 0.46f),
        size = Size(w * 0.76f, h * 0.38f),
        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
    )
    drawArc(
        color = Color(0xFF1C1C1E),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * 0.36f, h * 0.40f),
        size = Size(w * 0.28f, h * 0.14f)
    )
}

private fun DrawScope.drawMailIcon(w: Float, h: Float, c: Offset) {
    drawRect(
        brush = Brush.verticalGradient(colors = listOf(Color(0xFF1E65F5), Color(0xFF5DC4FF)))
    )
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(w * 0.15f, h * 0.27f),
        size = Size(w * 0.70f, h * 0.46f),
        cornerRadius = CornerRadius(w * 0.07f, w * 0.07f)
    )
    val flap = Path().apply {
        moveTo(w * 0.16f, h * 0.29f)
        lineTo(w * 0.50f, h * 0.54f)
        lineTo(w * 0.84f, h * 0.29f)
    }
    drawPath(flap, color = Color(0xFF429BF8), style = Stroke(width = w * 0.035f, join = StrokeJoin.Round))
}

private fun DrawScope.drawYouTubeIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color.White)
    drawRoundRect(
        color = Color(0xFFFF0000),
        topLeft = Offset(w * 0.12f, h * 0.24f),
        size = Size(w * 0.76f, h * 0.52f),
        cornerRadius = CornerRadius(w * 0.16f, w * 0.16f)
    )
    val tri = Path().apply {
        moveTo(w * 0.42f, h * 0.37f)
        lineTo(w * 0.65f, h * 0.50f)
        lineTo(w * 0.42f, h * 0.63f)
        close()
    }
    drawPath(tri, color = Color.White)
}

private fun DrawScope.drawYouTubeMusicIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color.White)
    drawCircle(color = Color(0xFFFF0000), radius = w * 0.39f, center = c)
    drawCircle(
        color = Color.White,
        radius = w * 0.24f,
        center = c,
        style = Stroke(width = w * 0.032f)
    )
    val tri = Path().apply {
        moveTo(w * 0.43f, h * 0.38f)
        lineTo(w * 0.63f, h * 0.50f)
        lineTo(w * 0.43f, h * 0.62f)
        close()
    }
    drawPath(tri, color = Color.White)
}

private fun DrawScope.drawTikTokIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color(0xFF0A0A0D))
    fun DrawScope.drawNote(dx: Float, dy: Float, color: Color) {
        val stemX = w * 0.54f + dx
        drawLine(
            color = color,
            start = Offset(stemX, h * 0.22f + dy),
            end = Offset(stemX, h * 0.64f + dy),
            strokeWidth = w * 0.095f,
            cap = StrokeCap.Round
        )
        drawArc(
            color = color,
            startAngle = 90f,
            sweepAngle = 250f,
            useCenter = false,
            topLeft = Offset(w * 0.28f + dx, h * 0.49f + dy),
            size = Size(w * 0.29f, h * 0.28f),
            style = Stroke(width = w * 0.095f, cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = -90f,
            useCenter = false,
            topLeft = Offset(stemX, h * 0.14f + dy),
            size = Size(w * 0.36f, h * 0.28f),
            style = Stroke(width = w * 0.095f, cap = StrokeCap.Round)
        )
    }
    drawNote(-w * 0.025f, -h * 0.025f, Color(0xFF25F4EE))
    drawNote(w * 0.025f, h * 0.025f, Color(0xFFFE2C55))
    drawNote(0f, 0f, Color.White)
}

private fun DrawScope.drawInstagramIcon(w: Float, h: Float, c: Offset) {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF515BD4), Color(0xFF8134AF), Color(0xFFDD2A7B), Color(0xFFF58529), Color(0xFFFEDA77)),
            start = Offset(w * 0.1f, h * 0.1f),
            end = Offset(w * 0.9f, h * 0.9f)
        )
    )
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(w * 0.19f, h * 0.19f),
        size = Size(w * 0.62f, h * 0.62f),
        cornerRadius = CornerRadius(w * 0.16f, w * 0.16f),
        style = Stroke(width = w * 0.065f)
    )
    drawCircle(
        color = Color.White,
        radius = w * 0.155f,
        center = c,
        style = Stroke(width = w * 0.065f)
    )
    drawCircle(color = Color.White, radius = w * 0.042f, center = Offset(w * 0.68f, h * 0.32f))
}

private fun DrawScope.drawWhatsAppIcon(w: Float, h: Float, c: Offset) {
    drawGreenGradientBg()
    drawCircle(
        color = Color.White,
        radius = w * 0.31f,
        center = c,
        style = Stroke(width = w * 0.065f)
    )
    val tail = Path().apply {
        moveTo(w * 0.27f, h * 0.70f)
        lineTo(w * 0.19f, h * 0.81f)
        lineTo(w * 0.34f, h * 0.77f)
        close()
    }
    drawPath(tail, color = Color.White)
    drawPhoneReceiverPath(c.x, c.y, w * 0.56f, Color.White)
}

private fun DrawScope.drawFacebookIcon(
    w: Float,
    h: Float,
    c: Offset,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    drawRect(
        brush = Brush.verticalGradient(colors = listOf(Color(0xFF1A82FF), Color(0xFF0A5CC9)))
    )
    val fText = textMeasurer.measure(
        text = "f",
        style = TextStyle(
            fontFamily = SfProDisplay,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (w * 0.92f / density).sp,
            color = Color.White
        )
    )
    drawText(fText, topLeft = Offset(w * 0.37f, h * 0.06f))
}

private fun DrawScope.drawGmailIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color.White)
    val stroke = w * 0.13f
    drawLine(
        color = Color(0xFF4285F4),
        start = Offset(w * 0.21f, h * 0.34f),
        end = Offset(w * 0.21f, h * 0.70f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF34A853),
        start = Offset(w * 0.79f, h * 0.34f),
        end = Offset(w * 0.79f, h * 0.70f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFC5221F),
        start = Offset(w * 0.21f, h * 0.34f),
        end = Offset(w * 0.50f, h * 0.55f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFEA4335),
        start = Offset(w * 0.50f, h * 0.55f),
        end = Offset(w * 0.79f, h * 0.34f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawSpotifyIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color(0xFF191414))
    drawCircle(color = Color(0xFF1ED760), radius = w * 0.38f, center = c)
    rotate(8f, pivot = c) {
        for ((yOff, widthFactor, strokeF) in listOf(
            Triple(-0.12f, 0.44f, 0.058f),
            Triple(-0.01f, 0.36f, 0.050f),
            Triple(0.09f, 0.28f, 0.044f)
        )) {
            drawArc(
                color = Color(0xFF191414),
                startAngle = 215f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(c.x - w * widthFactor, c.y + h * yOff),
                size = Size(w * widthFactor * 2f, h * 0.24f),
                style = Stroke(width = w * strokeF, cap = StrokeCap.Round)
            )
        }
    }
}

private fun DrawScope.drawTelegramIcon(w: Float, h: Float, c: Offset) {
    drawRect(
        brush = Brush.verticalGradient(colors = listOf(Color(0xFF36B3E8), Color(0xFF1A8EC4)))
    )
    val plane = Path().apply {
        moveTo(w * 0.81f, h * 0.25f)
        lineTo(w * 0.16f, h * 0.50f)
        lineTo(w * 0.38f, h * 0.58f)
        lineTo(w * 0.70f, h * 0.35f)
        lineTo(w * 0.46f, h * 0.62f)
        lineTo(w * 0.68f, h * 0.77f)
        close()
    }
    drawPath(plane, color = Color.White)
}

private fun DrawScope.drawDiscordIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color(0xFF5865F2))
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(w * 0.19f, h * 0.28f),
        size = Size(w * 0.62f, h * 0.42f),
        cornerRadius = CornerRadius(w * 0.18f, w * 0.18f)
    )
    drawCircle(color = Color(0xFF5865F2), radius = w * 0.065f, center = Offset(w * 0.38f, h * 0.50f))
    drawCircle(color = Color(0xFF5865F2), radius = w * 0.065f, center = Offset(w * 0.62f, h * 0.50f))
}

private fun DrawScope.drawAppleTvIcon(
    w: Float,
    h: Float,
    c: Offset,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    drawRect(
        brush = Brush.verticalGradient(colors = listOf(Color(0xFF2B2B2E), Color(0xFF141416)))
    )
    val tv = textMeasurer.measure(
        text = "tv",
        style = TextStyle(
            fontFamily = SfProDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = (w * 0.40f / density).sp,
            color = Color.White,
            letterSpacing = (-1).sp
        )
    )
    drawText(tv, topLeft = Offset((w - tv.size.width) / 2f, (h - tv.size.height) / 2f))
}

private fun DrawScope.drawGamesIcon(w: Float, h: Float, c: Offset) {
    drawRect(
        brush = Brush.verticalGradient(colors = listOf(Color(0xFFFF4D2D), Color(0xFFE02020)))
    )
    rotate(45f, pivot = c) {
        drawOval(
            color = Color.White,
            topLeft = Offset(w * 0.36f, h * 0.18f),
            size = Size(w * 0.28f, h * 0.56f)
        )
        drawCircle(color = Color(0xFFFF4D2D), radius = w * 0.06f, center = Offset(w * 0.50f, h * 0.38f))
    }
}

private fun DrawScope.drawTipsIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color(0xFFFFFCF0))
    drawCircle(
        brush = Brush.radialGradient(colors = listOf(Color(0xFFFFE64D), Color(0xFFFFB800))),
        radius = w * 0.23f,
        center = Offset(c.x, h * 0.43f)
    )
    drawRoundRect(
        color = Color(0xFF8E8E93),
        topLeft = Offset(w * 0.40f, h * 0.68f),
        size = Size(w * 0.20f, h * 0.12f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
    )
}

private fun DrawScope.drawCalculatorIcon(w: Float, h: Float) {
    drawRect(color = Color(0xFF1C1C1E))
    drawRoundRect(
        color = Color(0xFF3A3A3C),
        topLeft = Offset(w * 0.15f, h * 0.14f),
        size = Size(w * 0.70f, h * 0.20f),
        cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
    )
    val btnR = w * 0.095f
    val xs = listOf(0.27f, 0.50f, 0.73f)
    val ys = listOf(0.49f, 0.67f, 0.83f)
    ys.forEachIndexed { r, yr ->
        xs.forEachIndexed { col, xr ->
            val colColor = if (col == 2) Color(0xFFFF9F0A) else Color(0xFF505054)
            drawCircle(color = colColor, radius = btnR, center = Offset(w * xr, h * yr))
        }
    }
}

private fun DrawScope.drawKeepIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color.White)
    drawRoundRect(
        color = Color(0xFFFBBC04),
        topLeft = Offset(w * 0.20f, h * 0.16f),
        size = Size(w * 0.60f, h * 0.68f),
        cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
    )
    drawCircle(color = Color.White, radius = w * 0.12f, center = Offset(c.x, h * 0.46f))
    drawRect(color = Color.White, topLeft = Offset(w * 0.43f, h * 0.60f), size = Size(w * 0.14f, h * 0.08f))
}

private fun DrawScope.drawDriveIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color.White)
    val tri = Path().apply {
        moveTo(w * 0.50f, h * 0.22f)
        lineTo(w * 0.80f, h * 0.74f)
        lineTo(w * 0.20f, h * 0.74f)
        close()
    }
    drawPath(tri, color = Color(0xFF4285F4), style = Stroke(width = w * 0.13f, join = StrokeJoin.Round))
    drawLine(
        color = Color(0xFF34A853),
        start = Offset(w * 0.50f, h * 0.22f),
        end = Offset(w * 0.20f, h * 0.74f),
        strokeWidth = w * 0.13f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFFBBC04),
        start = Offset(w * 0.50f, h * 0.22f),
        end = Offset(w * 0.80f, h * 0.74f),
        strokeWidth = w * 0.13f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawPinterestIcon(
    w: Float,
    h: Float,
    c: Offset,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    drawRect(color = Color.White)
    drawCircle(color = Color(0xFFE60023), radius = w * 0.37f, center = c)
    val p = textMeasurer.measure(
        text = "P",
        style = TextStyle(
            fontFamily = SfProDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = (w * 0.44f / density).sp,
            color = Color.White
        )
    )
    drawText(p, topLeft = Offset((w - p.size.width) / 2f, (h - p.size.height) / 2f))
}

private fun DrawScope.drawChromeIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color.White)
    val r = w * 0.36f
    drawArc(
        color = Color(0xFFEA4335),
        startAngle = 210f,
        sweepAngle = 150f,
        useCenter = true,
        topLeft = Offset(c.x - r, c.y - r),
        size = Size(r * 2, r * 2)
    )
    drawArc(
        color = Color(0xFFFBBC05),
        startAngle = 360f,
        sweepAngle = 120f,
        useCenter = true,
        topLeft = Offset(c.x - r, c.y - r),
        size = Size(r * 2, r * 2)
    )
    drawArc(
        color = Color(0xFF34A853),
        startAngle = 120f,
        sweepAngle = 120f,
        useCenter = true,
        topLeft = Offset(c.x - r, c.y - r),
        size = Size(r * 2, r * 2)
    )
    drawCircle(color = Color.White, radius = w * 0.18f, center = c)
    drawCircle(color = Color(0xFF4285F4), radius = w * 0.13f, center = c)
}

private fun DrawScope.drawGeminiIcon(w: Float, h: Float, c: Offset) {
    drawRect(color = Color(0xFF13131A))
    val star = Path().apply {
        moveTo(c.x, h * 0.17f)
        quadraticTo(c.x, c.y, w * 0.83f, c.y)
        quadraticTo(c.x, c.y, c.x, h * 0.83f)
        quadraticTo(c.x, c.y, w * 0.17f, c.y)
        quadraticTo(c.x, c.y, c.x, h * 0.17f)
        close()
    }
    drawPath(
        star,
        brush = Brush.linearGradient(colors = listOf(Color(0xFF4E86F7), Color(0xFF9E70FF), Color(0xFFFF6D8A)))
    )
}
