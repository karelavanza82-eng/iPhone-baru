package com.example.appdrawer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.icons.IosAppIconGraphic
import com.example.launcher.ActiveAppSession
import com.example.launcher.LauncherApp
import com.example.theme.IosColors
import com.example.theme.SfProText

@Composable
fun SpotlightSearchOverlay(
    query: String,
    allApps: List<LauncherApp>,
    recentApps: List<ActiveAppSession>,
    onQueryChange: (String) -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Throwable) {
        }
    }

    val suggestions = remember(allApps, recentApps) {
        val recents = recentApps.map { it.app }
        (recents + allApps).distinctBy { it.id }.take(8)
    }

    val filtered = remember(allApps, query) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim()
            allApps.filter {
                it.label.contains(q, ignoreCase = true) || it.packageName.contains(q, ignoreCase = true)
            }.take(24)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA0C0D12))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {},
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x992C2C32))
                    .border(0.5.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Spotlight Search",
                    tint = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search",
                            fontFamily = SfProText,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        cursorBrush = SolidColor(IosColors.SystemBlue),
                        textStyle = TextStyle(
                            fontFamily = SfProText,
                            fontSize = 16.sp,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("spotlight_input")
                    )
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    fontFamily = SfProText,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (query.isBlank()) {
            // Siri / App Suggestions card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0x9922232B))
                    .border(0.5.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(22.dp))
                    .clickable(enabled = false) {}
                    .padding(16.dp)
            ) {
                Text(
                    text = "Suggestions",
                    fontFamily = SfProText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(14.dp))
                val rows = suggestions.chunked(4)
                rows.forEachIndexed { rowIdx, rowApps ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowApps.forEach { app ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onLaunchApp(app) }
                            ) {
                                Box(modifier = Modifier.size(56.dp)) {
                                    IosAppIconGraphic(
                                        iconKey = app.iconKey,
                                        fallbackBitmap = app.fallbackBitmap,
                                        fallbackTintColor = app.fallbackTintColor,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    text = app.label,
                                    fontFamily = SfProText,
                                    fontSize = 11.5.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        repeat(4 - rowApps.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    if (rowIdx == 0 && rows.size > 1) {
                        Spacer(Modifier.height(14.dp))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xAD22232B))
                    .border(0.5.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
                    .clickable(enabled = false) {}
            ) {
                items(filtered, key = { it.id }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLaunchApp(app) }
                            .padding(horizontal = 16.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp)) {
                            IosAppIconGraphic(
                                iconKey = app.iconKey,
                                fallbackBitmap = app.fallbackBitmap,
                                fallbackTintColor = app.fallbackTintColor,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.label,
                                fontFamily = SfProText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (app.isBuiltin) "Built-in App" else app.category.title,
                                fontFamily = SfProText,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                runCatching {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.google.com/search?q=${Uri.encode(query.trim())}")
                                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                }
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Public,
                            contentDescription = "Search Web",
                            tint = IosColors.SystemBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Search Web for \"${query.trim()}\"",
                            fontFamily = SfProText,
                            fontSize = 15.sp,
                            color = IosColors.SystemBlue
                        )
                    }
                }
            }
        }
    }
}
