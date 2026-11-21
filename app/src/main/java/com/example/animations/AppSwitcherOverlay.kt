package com.example.animations

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.icons.IosAppIconGraphic
import com.example.launcher.ActiveAppSession
import com.example.launcher.LauncherApp
import com.example.theme.SfProDisplay
import com.example.theme.SfProText

@Composable
fun AppSwitcherOverlay(
    recentApps: List<ActiveAppSession>,
    onSelectApp: (LauncherApp, View?) -> Unit,
    onRemoveRecent: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x880B0C12))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (recentApps.isEmpty()) {
            Text(
                text = "No Recent Apps",
                fontFamily = SfProText,
                fontSize = 17.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                contentPadding = PaddingValues(horizontal = 36.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(recentApps, key = { it.app.id }) { session ->
                    Column(
                        modifier = Modifier
                            .width(248.dp)
                            .fillMaxHeight(0.68f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(28.dp)) {
                                    IosAppIconGraphic(
                                        iconKey = session.app.iconKey,
                                        fallbackBitmap = session.app.fallbackBitmap,
                                        fallbackTintColor = session.app.fallbackTintColor,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = session.app.label,
                                    fontFamily = SfProText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                            IconButton(
                                onClick = { onRemoveRecent(session.app.id) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close app card",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color(0xFF181920))
                                .border(0.8.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(32.dp))
                                .clickable { onSelectApp(session.app, view) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(76.dp)) {
                                    IosAppIconGraphic(
                                        iconKey = session.app.iconKey,
                                        fallbackBitmap = session.app.fallbackBitmap,
                                        fallbackTintColor = session.app.fallbackTintColor,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = session.app.label,
                                    fontFamily = SfProDisplay,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
