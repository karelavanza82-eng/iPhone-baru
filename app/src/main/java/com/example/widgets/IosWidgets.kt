package com.example.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.launcher.BatteryStatus
import com.example.launcher.HomeWidgetItem
import com.example.launcher.MediaPlaybackInfo
import com.example.launcher.ReminderRecord
import com.example.launcher.WidgetSize
import com.example.launcher.WidgetType
import com.example.theme.IosColors
import com.example.theme.SfProDisplay
import com.example.theme.SfProText
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IosWidgetCard(
    widget: HomeWidgetItem,
    batteryStatus: BatteryStatus,
    mediaInfo: MediaPlaybackInfo,
    reminders: List<ReminderRecord>,
    onWidgetClick: () -> Unit,
    onMediaPlayPause: () -> Unit,
    onMediaNext: () -> Unit,
    onCycleSmartStack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeType = if (widget.type == WidgetType.SMART_STACK) {
        when (widget.stackIndex % 4) {
            0 -> WidgetType.BATTERIES
            1 -> WidgetType.WEATHER
            2 -> WidgetType.CALENDAR
            else -> WidgetType.MUSIC
        }
    } else {
        widget.type
    }

    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .border(0.6.dp, Color.White.copy(alpha = 0.22f), shape)
            .clickable {
                if (widget.type == WidgetType.SMART_STACK) onCycleSmartStack()
                else onWidgetClick()
            }
    ) {
        when (activeType) {
            WidgetType.BATTERIES -> BatteriesWidgetContent(batteryStatus, widget.size)
            WidgetType.CALENDAR -> CalendarWidgetContent(reminders, widget.size)
            WidgetType.WEATHER -> WeatherWidgetContent(widget.size)
            WidgetType.CLOCK -> ClockWidgetContent()
            WidgetType.MUSIC -> MusicWidgetContent(mediaInfo, widget.size, onMediaPlayPause, onMediaNext)
            WidgetType.PHOTOS -> PhotosWidgetContent()
            WidgetType.SMART_STACK -> BatteriesWidgetContent(batteryStatus, widget.size)
        }

        if (widget.type == WidgetType.SMART_STACK) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(4) { idx ->
                    Box(
                        modifier = Modifier
                            .size(4.5.dp)
                            .clip(CircleShape)
                            .background(
                                if (idx == widget.stackIndex % 4) Color.White
                                else Color.White.copy(alpha = 0.35f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteriesWidgetContent(battery: BatteryStatus, size: WidgetSize) {
    val ringColor = when {
        battery.isCharging || battery.level > 25 -> IosColors.SystemGreen
        battery.level > 15 -> IosColors.SystemYellow
        else -> IosColors.SystemRed
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x991C1D24))
            .padding(14.dp)
    ) {
        if (size == WidgetSize.SMALL) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 5.5.dp.toPx()
                        drawArc(
                            color = Color.White.copy(alpha = 0.16f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke)
                        )
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = (battery.level / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Icon(
                        imageVector = if (battery.isCharging) Icons.Filled.Bolt else Icons.Filled.PhoneAndroid,
                        contentDescription = "Device battery",
                        tint = if (battery.isCharging) ringColor else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "${battery.level}%",
                    fontFamily = SfProDisplay,
                    fontWeight = FontWeight.Medium,
                    fontSize = 36.sp,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Phone Battery",
                        fontFamily = SfProText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.72f)
                    )
                    Text(
                        text = "${battery.level}%",
                        fontFamily = SfProDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp,
                        color = Color.White
                    )
                    Text(
                        text = if (battery.isCharging) "Charging • ${battery.healthLabel}" else "${battery.healthLabel} • ${battery.temperatureC}°C",
                        fontFamily = SfProText,
                        fontSize = 12.sp,
                        color = ringColor
                    )
                }
                Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 7.dp.toPx()
                        drawArc(
                            color = Color.White.copy(alpha = 0.16f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke)
                        )
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = (battery.level / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Icon(
                        imageVector = if (battery.isCharging) Icons.Filled.Bolt else Icons.Filled.PhoneAndroid,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarWidgetContent(reminders: List<ReminderRecord>, size: WidgetSize) {
    val cal = remember { Calendar.getInstance() }
    val weekday = remember {
        cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)
            ?.uppercase(Locale.ENGLISH) ?: "FRIDAY"
    }
    val dayNum = remember { cal.get(Calendar.DAY_OF_MONTH).toString() }
    val upcoming = reminders.firstOrNull { !it.isCompleted }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAD1E1F26))
            .padding(14.dp)
    ) {
        if (size == WidgetSize.SMALL) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = weekday,
                        fontFamily = SfProText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = IosColors.SystemRed,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        text = dayNum,
                        fontFamily = SfProDisplay,
                        fontWeight = FontWeight.Normal,
                        fontSize = 36.sp,
                        lineHeight = 38.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = upcoming?.title ?: "No Events Today",
                    fontFamily = SfProText,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = if (upcoming != null) 0.90f else 0.55f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text(
                        text = weekday,
                        fontFamily = SfProText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = IosColors.SystemRed
                    )
                    Text(
                        text = dayNum,
                        fontFamily = SfProDisplay,
                        fontWeight = FontWeight.Normal,
                        fontSize = 42.sp,
                        color = Color.White
                    )
                }
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    val activeList = reminders.filter { !it.isCompleted }.take(2)
                    if (activeList.isEmpty()) {
                        Text(
                            text = "No Events Today",
                            fontFamily = SfProText,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    } else {
                        activeList.forEach { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(IosColors.SystemBlue)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = item.title,
                                    fontFamily = SfProText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.5.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherWidgetContent(size: WidgetSize) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E569E), Color(0xFF3E78B8), Color(0xFF5D92CC))
                )
            )
            .padding(14.dp)
    ) {
        if (size == WidgetSize.SMALL) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Cupertino",
                        fontFamily = SfProText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "27°",
                        fontFamily = SfProDisplay,
                        fontWeight = FontWeight.Normal,
                        fontSize = 40.sp,
                        lineHeight = 42.sp,
                        color = Color.White
                    )
                }
                Column {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Weather",
                        tint = Color(0xFFFFD60A),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Partly Cloudy",
                        fontFamily = SfProText,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                    Text(
                        text = "H:31° L:26°",
                        fontFamily = SfProText,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Cupertino",
                            fontFamily = SfProText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "27°",
                            fontFamily = SfProDisplay,
                            fontWeight = FontWeight.Light,
                            fontSize = 44.sp,
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Icon(
                            imageVector = Icons.Filled.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFFFFD60A),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Partly Cloudy",
                            fontFamily = SfProText,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = "H:31° L:26°",
                            fontFamily = SfProText,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Now" to "27°", "1PM" to "29°", "2PM" to "30°", "3PM" to "31°", "4PM" to "28°").forEach { (hr, temp) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(hr, fontFamily = SfProText, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Icon(Icons.Filled.WbSunny, contentDescription = null, tint = Color(0xFFFFD60A), modifier = Modifier.size(14.dp))
                            Text(temp, fontFamily = SfProText, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClockWidgetContent() {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val c = Offset(w / 2f, h / 2f)
            val r = minOf(w, h) * 0.46f
            drawCircle(color = Color.White, radius = r, center = c)

            val numRadius = r * 0.76f
            val fontSizeSp = (r * 0.25f / density).sp
            for (i in 1..12) {
                val rad = Math.toRadians((i * 30 - 90).toDouble())
                val nx = c.x + (numRadius * cos(rad)).toFloat()
                val ny = c.y + (numRadius * sin(rad)).toFloat()
                val layout = textMeasurer.measure(
                    text = i.toString(),
                    style = TextStyle(
                        fontFamily = SfProDisplay,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = fontSizeSp,
                        color = Color(0xFF141414)
                    )
                )
                drawText(layout, topLeft = Offset(nx - layout.size.width / 2f, ny - layout.size.height / 2f))
            }

            val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val sec = cal.get(Calendar.SECOND)
            val min = cal.get(Calendar.MINUTE) + sec / 60f
            val hr = (cal.get(Calendar.HOUR) % 12) + min / 60f

            rotate(hr * 30f, pivot = c) {
                drawLine(
                    color = Color(0xFF141414),
                    start = Offset(c.x, c.y + r * 0.08f),
                    end = Offset(c.x, c.y - r * 0.50f),
                    strokeWidth = r * 0.08f,
                    cap = StrokeCap.Round
                )
            }
            rotate(min * 6f, pivot = c) {
                drawLine(
                    color = Color(0xFF141414),
                    start = Offset(c.x, c.y + r * 0.10f),
                    end = Offset(c.x, c.y - r * 0.78f),
                    strokeWidth = r * 0.055f,
                    cap = StrokeCap.Round
                )
            }
            rotate(sec * 6f, pivot = c) {
                drawLine(
                    color = Color(0xFFFF9500),
                    start = Offset(c.x, c.y + r * 0.16f),
                    end = Offset(c.x, c.y - r * 0.84f),
                    strokeWidth = r * 0.025f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(color = Color(0xFFFF9500), radius = r * 0.06f, center = c)
        }
    }
}

@Composable
private fun MusicWidgetContent(
    mediaInfo: MediaPlaybackInfo,
    size: WidgetSize,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF342B3E), Color(0xFF1D1B26))
                )
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(if (size == WidgetSize.SMALL) 54.dp else 64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFA2D48), Color(0xFF833AB4))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = "Album artwork",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onPlayPause, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = if (mediaInfo.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play or pause",
                            tint = Color.White
                        )
                    }
                    if (size != WidgetSize.SMALL) {
                        IconButton(onClick = onNext, modifier = Modifier.size(34.dp)) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Next track",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Column {
                Text(
                    text = mediaInfo.title,
                    fontFamily = SfProText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = mediaInfo.artist.ifBlank { "Music" },
                    fontFamily = SfProText,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PhotosWidgetContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_wallpaper_ios_aurora),
            contentDescription = "Featured memory photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Text(
                text = "Featured",
                fontFamily = SfProText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "Twilight Gallery",
                fontFamily = SfProDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}
