package com.example.builtinapps

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.icons.IosAppIconGraphic
import com.example.launcher.IosBuiltinAppId
import com.example.launcher.LauncherApp
import com.example.launcher.LauncherUiState
import com.example.launcher.NoteRecord
import com.example.theme.IosColors
import com.example.theme.SfProDisplay
import com.example.theme.SfProText
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun BuiltinIosAppContent(
    builtinId: IosBuiltinAppId,
    state: LauncherUiState,
    onLaunchApp: (LauncherApp, View?) -> Unit,
    onSaveNote: (String, String, String?) -> Unit,
    onDeleteNote: (String) -> Unit,
    onToggleReminder: (String) -> Unit,
    onAddReminder: (String) -> Unit,
    onSetWallpaperPreset: (com.example.launcher.WallpaperPreset) -> Unit
) {
    when (builtinId) {
        IosBuiltinAppId.CLOCK -> IosClockApp()
        IosBuiltinAppId.CALCULATOR -> IosCalculatorApp()
        IosBuiltinAppId.WEATHER -> IosWeatherApp()
        IosBuiltinAppId.NOTES -> IosNotesApp(state.notes, onSaveNote, onDeleteNote)
        IosBuiltinAppId.CALENDAR -> IosCalendarApp()
        IosBuiltinAppId.PHOTOS -> IosPhotosApp(onSetWallpaperPreset)
        IosBuiltinAppId.REMINDERS -> IosRemindersApp(state.reminders, onToggleReminder, onAddReminder)
        IosBuiltinAppId.APP_STORE -> IosAppStoreView(state.allApps, onLaunchApp)
        IosBuiltinAppId.FILES -> IosFilesApp(state)
        IosBuiltinAppId.SETTINGS -> Unit
    }
}

@Composable
private fun IosClockApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var stopwatchRunning by remember { mutableStateOf(false) }
    var stopwatchElapsedMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            if (stopwatchRunning) stopwatchElapsedMs += 100L
            delay(100L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = if (selectedTab == 0) "World Clock" else "Stopwatch",
            fontFamily = SfProDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            color = Color.White
        )
        Spacer(Modifier.height(16.dp))

        if (selectedTab == 0) {
            val cities = listOf("Local Time" to 0, "Cupertino" to -8, "London" to 0, "Tokyo" to 9)
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cities) { (city, _) ->
                    val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(nowMillis))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1C1C1E))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Today", fontFamily = SfProText, fontSize = 12.sp, color = Color.Gray)
                            Text(city, fontFamily = SfProDisplay, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Color.White)
                        }
                        Text(timeText, fontFamily = SfProDisplay, fontWeight = FontWeight.Light, fontSize = 38.sp, color = Color.White)
                    }
                }
            }
        } else {
            val mins = (stopwatchElapsedMs / 60000L)
            val secs = (stopwatchElapsedMs % 60000L) / 1000L
            val tenths = (stopwatchElapsedMs % 1000L) / 100L
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = String.format(Locale.US, "%02d:%02d.%d", mins, secs, tenths),
                    fontFamily = SfProDisplay,
                    fontWeight = FontWeight.Light,
                    fontSize = 68.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF333336))
                            .clickable {
                                stopwatchRunning = false
                                stopwatchElapsedMs = 0L
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Reset", color = Color.White, fontFamily = SfProText)
                    }
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(if (stopwatchRunning) Color(0xFF3B1517) else Color(0xFF10381C))
                            .clickable { stopwatchRunning = !stopwatchRunning },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (stopwatchRunning) "Stop" else "Start",
                            color = if (stopwatchRunning) IosColors.SystemRed else IosColors.SystemGreen,
                            fontFamily = SfProText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("World Clock", "Stopwatch").forEachIndexed { idx, label ->
                Text(
                    text = label,
                    fontFamily = SfProText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (selectedTab == idx) IosColors.SystemOrange else Color.Gray,
                    modifier = Modifier
                        .clickable { selectedTab = idx }
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun IosCalculatorApp() {
    var display by remember { mutableStateOf("0") }
    var storedValue by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var resetOnNextDigit by remember { mutableStateOf(false) }

    fun onTap(key: String) {
        when (key) {
            "AC" -> {
                display = "0"
                storedValue = null
                pendingOp = null
                resetOnNextDigit = false
            }
            "+/-" -> {
                val v = display.toDoubleOrNull() ?: return
                display = formatCalc(-v)
            }
            "%" -> {
                val v = display.toDoubleOrNull() ?: return
                display = formatCalc(v / 100.0)
            }
            "÷", "×", "-", "+" -> {
                storedValue = display.toDoubleOrNull() ?: 0.0
                pendingOp = key
                resetOnNextDigit = true
            }
            "=" -> {
                val a = storedValue ?: return
                val b = display.toDoubleOrNull() ?: return
                val res = when (pendingOp) {
                    "÷" -> if (b == 0.0) 0.0 else a / b
                    "×" -> a * b
                    "-" -> a - b
                    "+" -> a + b
                    else -> b
                }
                display = formatCalc(res)
                storedValue = null
                pendingOp = null
                resetOnNextDigit = true
            }
            "." -> {
                if (resetOnNextDigit) {
                    display = "0."
                    resetOnNextDigit = false
                } else if (!display.contains(".")) {
                    display += "."
                }
            }
            else -> {
                display = if (display == "0" || resetOnNextDigit) {
                    resetOnNextDigit = false
                    key
                } else {
                    (display + key).take(12)
                }
            }
        }
    }

    val rows = listOf(
        listOf("AC", "+/-", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = display,
            fontFamily = SfProDisplay,
            fontWeight = FontWeight.Light,
            fontSize = 68.sp,
            color = Color.White,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { btn ->
                    val isOp = btn in listOf("÷", "×", "-", "+", "=")
                    val isTop = btn in listOf("AC", "+/-", "%")
                    val bg = when {
                        isOp -> IosColors.SystemOrange
                        isTop -> Color(0xFFA5A5A5)
                        else -> Color(0xFF333333)
                    }
                    val fg = if (isTop) Color.Black else Color.White
                    val weight = if (btn == "0") 2.1f else 1f
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .height(74.dp)
                            .clip(CircleShape)
                            .background(bg)
                            .clickable { onTap(btn) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = btn,
                            fontFamily = SfProDisplay,
                            fontWeight = FontWeight.Medium,
                            fontSize = 28.sp,
                            color = fg
                        )
                    }
                }
            }
        }
    }
}

private fun formatCalc(v: Double): String {
    return if (v % 1.0 == 0.0 && v in -1e12..1e12) v.toLong().toString()
    else String.format(Locale.US, "%.6f", v).trimEnd('0').trimEnd('.')
}

@Composable
private fun IosWeatherApp() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A4B8C), Color(0xFF3474B4), Color(0xFF5695CE))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Cupertino", fontFamily = SfProDisplay, fontSize = 34.sp, color = Color.White)
        Text("27°", fontFamily = SfProDisplay, fontWeight = FontWeight.Thin, fontSize = 86.sp, color = Color.White)
        Text("Partly Cloudy", fontFamily = SfProText, fontSize = 20.sp, color = Color.White.copy(alpha = 0.9f))
        Text("H:31°  L:26°", fontFamily = SfProText, fontSize = 18.sp, color = Color.White)

        Spacer(Modifier.height(32.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.14f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("5-DAY FORECAST", fontFamily = SfProText, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            listOf(
                Triple("Today", "26°", "31°"),
                Triple("Sat", "25°", "30°"),
                Triple("Sun", "24°", "29°"),
                Triple("Mon", "25°", "31°"),
                Triple("Tue", "26°", "32°")
            ).forEach { (day, low, high) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(day, fontFamily = SfProText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White, modifier = Modifier.width(64.dp))
                    Icon(Icons.Filled.WbSunny, null, tint = Color(0xFFFFD60A), modifier = Modifier.size(20.dp))
                    Text("$low  —  $high", fontFamily = SfProText, fontSize = 15.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun IosNotesApp(
    notes: List<NoteRecord>,
    onSaveNote: (String, String, String?) -> Unit,
    onDeleteNote: (String) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var bodyInput by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notes", fontFamily = SfProDisplay, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Color.White)
            IconButton(onClick = { isEditing = !isEditing }) {
                Icon(Icons.Filled.Add, contentDescription = "New Note", tint = IosColors.SystemYellow)
            }
        }

        if (isEditing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1C1C1E))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Title", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bodyInput,
                    onValueChange = { bodyInput = it },
                    label = { Text("Note", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (titleInput.isNotBlank() || bodyInput.isNotBlank()) {
                            onSaveNote(titleInput.ifBlank { "Untitled" }, bodyInput, null)
                            titleInput = ""
                            bodyInput = ""
                            isEditing = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IosColors.SystemYellow, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Note", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(notes, key = { it.id }) { note ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1C1C1E))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(note.title, fontFamily = SfProText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                        Spacer(Modifier.height(4.dp))
                        Text(note.body, fontFamily = SfProText, fontSize = 13.5.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = { onDeleteNote(note.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete note", tint = IosColors.SystemRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun IosCalendarApp() {
    val cal = remember { Calendar.getInstance() }
    val monthTitle = remember { SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(cal.time) }
    val today = remember { cal.get(Calendar.DAY_OF_MONTH) }
    val maxDay = remember { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
    ) {
        Text(monthTitle, fontFamily = SfProDisplay, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = IosColors.SystemRed)
        Spacer(Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
                Text(d, fontFamily = SfProText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.Gray)
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items((1..maxDay).toList()) { day ->
                val isToday = day == today
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(if (isToday) IosColors.SystemRed else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        fontFamily = SfProText,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun IosPhotosApp(onSetWallpaperPreset: (com.example.launcher.WallpaperPreset) -> Unit) {
    val items = listOf(
        Triple(R.drawable.img_wallpaper_ios_blue, "iOS 18 Sapphire", com.example.launcher.WallpaperPreset.IOS_BLUE),
        Triple(R.drawable.img_wallpaper_ios_aurora, "Twilight Aurora", com.example.launcher.WallpaperPreset.IOS_AURORA),
        Triple(R.drawable.img_wallpaper_ios_emerald, "Emerald Depth", com.example.launcher.WallpaperPreset.IOS_EMERALD)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
    ) {
        Text("Photos", fontFamily = SfProDisplay, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Color.White)
        Text("Tap any wallpaper to apply", fontFamily = SfProText, fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { (resId, title, preset) ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1C1C1E))
                        .clickable { onSetWallpaperPreset(preset) }
                ) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    )
                    Text(
                        text = title,
                        fontFamily = SfProText,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IosRemindersApp(
    reminders: List<com.example.launcher.ReminderRecord>,
    onToggle: (String) -> Unit,
    onAdd: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
    ) {
        Text("Reminders", fontFamily = SfProDisplay, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = IosColors.SystemBlue)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("New Reminder", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    if (input.isNotBlank()) {
                        onAdd(input)
                        input = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IosColors.SystemBlue)
            ) { Text("Add") }
        }
        Spacer(Modifier.height(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(reminders, key = { it.id }) { rem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1C1C1E))
                        .clickable { onToggle(rem.id) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (rem.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (rem.isCompleted) IosColors.SystemBlue else Color.Gray
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = rem.title,
                        fontFamily = SfProText,
                        fontSize = 16.sp,
                        color = if (rem.isCompleted) Color.Gray else Color.White,
                        textDecoration = if (rem.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
        }
    }
}

@Composable
private fun IosAppStoreView(
    allApps: List<LauncherApp>,
    onLaunchApp: (LauncherApp, View?) -> Unit
) {
    val view = LocalView.current
    val installed = remember(allApps) { allApps.filter { !it.isBuiltin } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text("Apps", fontFamily = SfProDisplay, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Color.White)
        Text("${installed.size} Installed Android Apps", fontFamily = SfProText, fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(installed, key = { it.id }) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1C1C1E))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp)) {
                        IosAppIconGraphic(
                            iconKey = app.iconKey,
                            fallbackBitmap = app.fallbackBitmap,
                            fallbackTintColor = app.fallbackTintColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.label, fontFamily = SfProText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(app.category.title, fontFamily = SfProText, fontSize = 12.sp, color = Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2E))
                            .clickable { onLaunchApp(app, view) }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("OPEN", fontFamily = SfProText, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IosColors.SystemBlue)
                    }
                }
            }
        }
    }
}

@Composable
private fun IosFilesApp(state: LauncherUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Browse", fontFamily = SfProDisplay, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Color.White)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1C1C1E))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Folder, null, tint = IosColors.SystemBlue, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("On My Phone", fontFamily = SfProText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                    Text("${state.storageInfo.availableGb} available of ${state.storageInfo.totalGb}", fontFamily = SfProText, fontSize = 13.sp, color = Color.Gray)
                }
            }
            HorizontalDivider(color = IosColors.DarkSeparator, thickness = 0.5.dp)
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Folder, null, tint = IosColors.SystemOrange, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Saved Notes", fontFamily = SfProText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                    Text("${state.notes.size} notes stored locally", fontFamily = SfProText, fontSize = 13.sp, color = Color.Gray)
                }
            }
        }
    }
}
