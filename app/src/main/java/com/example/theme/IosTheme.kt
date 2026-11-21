package com.example.theme

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

val SfProDisplay = FontFamily(
    Font(R.font.dm_sans, FontWeight.Normal),
    Font(R.font.dm_sans, FontWeight.Medium),
    Font(R.font.dm_sans, FontWeight.SemiBold),
    Font(R.font.dm_sans, FontWeight.Bold)
)

val SfProText = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold)
)

object IosColors {
    val SystemBlue = Color(0xFF0A84FF)
    val SystemBlueLight = Color(0xFF007AFF)
    val SystemGreen = Color(0xFF30D158)
    val SystemGreenLight = Color(0xFF34C759)
    val SystemRed = Color(0xFFFF3B30)
    val SystemOrange = Color(0xFFFF9F0A)
    val SystemYellow = Color(0xFFFFD60A)
    val SystemPink = Color(0xFFFF375F)
    val SystemIndigo = Color(0xFF5E5CE6)
    val SystemTeal = Color(0xFF64D2FF)

    val DarkBackground = Color(0xFF000000)
    val DarkSecondaryBackground = Color(0xFF1C1C1E)
    val DarkTertiaryBackground = Color(0xFF2C2C2E)
    val DarkSeparator = Color(0xFF38383A)

    val LightBackground = Color(0xFFF2F2F7)
    val LightSecondaryBackground = Color(0xFFFFFFFF)
    val LightTertiaryBackground = Color(0xFFE5E5EA)
    val LightSeparator = Color(0xFFC6C6C8)

    val LabelPrimaryDark = Color(0xFFFFFFFF)
    val LabelSecondaryDark = Color(0x99EBEBF5)
    val LabelTertiaryDark = Color(0x4DEBEBF5)

    val LabelPrimaryLight = Color(0xFF000000)
    val LabelSecondaryLight = Color(0x993C3C43)
    val LabelTertiaryLight = Color(0x4D3C3C43)

    val FrostedGlassDark = Color(0x661E1E24)
    val FrostedGlassCard = Color(0x5225262E)
    val FrostedGlassLight = Color(0x4DFFFFFF)
    val DockGlass = Color(0x421F2330)
    val DockBorder = Color(0x30FFFFFF)
}

object IosTypography {
    val LockScreenClock = TextStyle(
        fontFamily = SfProDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 82.sp,
        lineHeight = 86.sp,
        letterSpacing = (-2.0).sp
    )
    val LockScreenDate = TextStyle(
        fontFamily = SfProText,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    )
    val LargeTitle = TextStyle(
        fontFamily = SfProDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    )
    val Title1 = TextStyle(
        fontFamily = SfProDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.3).sp
    )
    val Title2 = TextStyle(
        fontFamily = SfProDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    )
    val Headline = TextStyle(
        fontFamily = SfProText,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp
    )
    val Body = TextStyle(
        fontFamily = SfProText,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 22.sp
    )
    val Callout = TextStyle(
        fontFamily = SfProText,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 21.sp
    )
    val Subheadline = TextStyle(
        fontFamily = SfProText,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp
    )
    val Footnote = TextStyle(
        fontFamily = SfProText,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
    val IconLabel = TextStyle(
        fontFamily = SfProText,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 14.sp,
        letterSpacing = (-0.1).sp
    )
    val BadgeText = TextStyle(
        fontFamily = SfProText,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 12.sp
    )
}

/**
 * Authentic iOS continuous squircle approximation (~22.5% corner ratio).
 */
fun iosSquircleShape(cornerRatio: Float = 0.225f): Shape = GenericShape { size, _ ->
    val r = (minOf(size.width, size.height) * cornerRatio)
    addRoundRect(
        RoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            cornerRadius = CornerRadius(r, r)
        )
    )
}

fun iosCardShape(radius: Dp = 22.dp): RoundedCornerShape = RoundedCornerShape(radius)
