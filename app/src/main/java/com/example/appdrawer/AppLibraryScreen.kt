package com.example.appdrawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.icons.IosAppIconGraphic
import com.example.icons.IosNotificationBadge
import com.example.launcher.AppCategory
import com.example.launcher.LauncherApp
import com.example.theme.SfProDisplay
import com.example.theme.SfProText

private data class CategoryFolder(
    val title: String,
    val apps: List<LauncherApp>
)

@Composable
fun AppLibraryPage(
    allApps: List<LauncherApp>,
    badgeCounts: Map<String, Int>,
    onLaunchApp: (LauncherApp) -> Unit,
    onRegisterBounds: (String, Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var expandedFolder by remember { mutableStateOf<CategoryFolder?>(null) }

    val folders = remember(allApps) {
        val suggestions = allApps.take(8)
        val recentlyAdded = allApps.filter { !it.isBuiltin }
            .sortedByDescending { it.installTimeMillis }
            .take(8)
        val result = mutableListOf<CategoryFolder>()
        if (suggestions.isNotEmpty()) result.add(CategoryFolder("Suggestions", suggestions))
        if (recentlyAdded.isNotEmpty()) result.add(CategoryFolder("Recently Added", recentlyAdded))
        AppCategory.entries
            .filter { it != AppCategory.SUGGESTIONS && it != AppCategory.RECENTLY_ADDED }
            .forEach { cat ->
                val list = allApps.filter { it.category == cat }
                if (list.isNotEmpty()) result.add(CategoryFolder(cat.title, list))
            }
        result
    }

    val filteredApps = remember(allApps, query) {
        if (query.isBlank()) emptyList()
        else allApps.filter { it.label.contains(query.trim(), ignoreCase = true) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            // App Library Search Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x521E2028))
                    .border(0.6.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search App Library",
                    tint = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = "App Library",
                            fontFamily = SfProText,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        cursorBrush = SolidColor(Color.White),
                        textStyle = TextStyle(
                            fontFamily = SfProText,
                            fontSize = 16.sp,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("app_library_search")
                    )
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            if (query.isNotBlank()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0x661C1C22)),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredApps, key = { it.id }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLaunchApp(app) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .onGloballyPositioned { onRegisterBounds(app.id, it.boundsInRoot()) }
                            ) {
                                IosAppIconGraphic(
                                    iconKey = app.iconKey,
                                    fallbackBitmap = app.fallbackBitmap,
                                    fallbackTintColor = app.fallbackTintColor,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.label,
                                    fontFamily = SfProText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = app.category.title,
                                    fontFamily = SfProText,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.55f)
                                )
                            }
                        }
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.08f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 74.dp)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(folders, key = { it.title }) { folder ->
                        CategoryFolderCard(
                            folder = folder,
                            badgeCounts = badgeCounts,
                            onLaunchApp = onLaunchApp,
                            onExpandFolder = { expandedFolder = folder },
                            onRegisterBounds = onRegisterBounds
                        )
                    }
                }
            }
        }

        // Expanded Folder Overlay
        AnimatedVisibility(
            visible = expandedFolder != null,
            enter = fadeIn() + scaleIn(initialScale = 0.88f),
            exit = fadeOut() + scaleOut(targetScale = 0.88f)
        ) {
            expandedFolder?.let { folder ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f))
                        .clickable { expandedFolder = null },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xD924252D))
                            .border(0.6.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(30.dp))
                            .clickable(enabled = false) {}
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = folder.title,
                            fontFamily = SfProDisplay,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                        Spacer(Modifier.height(18.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                        ) {
                            items(folder.apps, key = { it.id }) { app ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        expandedFolder = null
                                        onLaunchApp(app)
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .onGloballyPositioned { onRegisterBounds(app.id, it.boundsInRoot()) }
                                    ) {
                                        IosAppIconGraphic(
                                            iconKey = app.iconKey,
                                            fallbackBitmap = app.fallbackBitmap,
                                            fallbackTintColor = app.fallbackTintColor,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        val count = if (!app.isBuiltin) badgeCounts[app.packageName] ?: 0 else 0
                                        if (count > 0) {
                                            IosNotificationBadge(
                                                count = count,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = 5.dp, y = (-5).dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = app.label,
                                        fontFamily = SfProText,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFolderCard(
    folder: CategoryFolder,
    badgeCounts: Map<String, Int>,
    onLaunchApp: (LauncherApp) -> Unit,
    onExpandFolder: () -> Unit,
    onRegisterBounds: (String, Rect) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0x521F212B))
                .border(0.6.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(26.dp))
                .padding(12.dp)
        ) {
            val top3 = folder.apps.take(3)
            val rest = folder.apps.drop(3).take(4)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FolderAppSlot(top3.getOrNull(0), badgeCounts, onLaunchApp, onRegisterBounds, Modifier.weight(1f))
                    Spacer(Modifier.width(10.dp))
                    FolderAppSlot(top3.getOrNull(1), badgeCounts, onLaunchApp, onRegisterBounds, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FolderAppSlot(top3.getOrNull(2), badgeCounts, onLaunchApp, onRegisterBounds, Modifier.weight(1f))
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = rest.isNotEmpty(), onClick = onExpandFolder)
                            .padding(2.dp)
                    ) {
                        if (rest.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MiniFolderDot(rest.getOrNull(0), Modifier.weight(1f))
                                    Spacer(Modifier.width(4.dp))
                                    MiniFolderDot(rest.getOrNull(1), Modifier.weight(1f))
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MiniFolderDot(rest.getOrNull(2), Modifier.weight(1f))
                                    Spacer(Modifier.width(4.dp))
                                    MiniFolderDot(rest.getOrNull(3), Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(5.dp))
        Text(
            text = folder.title,
            fontFamily = SfProText,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FolderAppSlot(
    app: LauncherApp?,
    badgeCounts: Map<String, Int>,
    onLaunchApp: (LauncherApp) -> Unit,
    onRegisterBounds: (String, Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    if (app == null) {
        Spacer(modifier = modifier.aspectRatio(1f))
        return
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .onGloballyPositioned { onRegisterBounds(app.id, it.boundsInRoot()) }
            .clickable { onLaunchApp(app) }
    ) {
        IosAppIconGraphic(
            iconKey = app.iconKey,
            fallbackBitmap = app.fallbackBitmap,
            fallbackTintColor = app.fallbackTintColor,
            modifier = Modifier.fillMaxSize()
        )
        val count = if (!app.isBuiltin) badgeCounts[app.packageName] ?: 0 else 0
        if (count > 0) {
            IosNotificationBadge(
                count = count,
                badgeHeight = 16.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
            )
        }
    }
}

@Composable
private fun MiniFolderDot(app: LauncherApp?, modifier: Modifier = Modifier) {
    if (app == null) {
        Spacer(modifier = modifier.aspectRatio(1f))
        return
    }
    IosAppIconGraphic(
        iconKey = app.iconKey,
        fallbackBitmap = app.fallbackBitmap,
        fallbackTintColor = app.fallbackTintColor,
        animateClock = false,
        modifier = modifier.aspectRatio(1f)
    )
}
