package com.example

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.example.icons.IosIconKey
import com.example.icons.IosIconMapper
import com.example.icons.formatIosBadgeCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches PureOS`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("PureOS", context.getString(R.string.app_name))
    }

    @Test
    fun `main activity is registered as home launcher`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val matches = context.packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        assertTrue(matches.any { it.activityInfo.packageName == context.packageName })
    }

    @Test
    fun `ios icon mapper resolves core and social packages`() {
        assertEquals(IosIconKey.WHATSAPP, IosIconMapper.resolveIconKey("com.whatsapp", "WhatsApp"))
        assertEquals(IosIconKey.INSTAGRAM, IosIconMapper.resolveIconKey("com.instagram.android", "Instagram"))
        assertEquals(IosIconKey.TIKTOK, IosIconMapper.resolveIconKey("com.zhiliaoapp.musically", "TikTok"))
        assertEquals(IosIconKey.YOUTUBE, IosIconMapper.resolveIconKey("com.google.android.youtube", "YouTube"))
        assertEquals(IosIconKey.GMAIL, IosIconMapper.resolveIconKey("com.google.android.gm", "Gmail"))
        assertEquals(IosIconKey.SPOTIFY, IosIconMapper.resolveIconKey("com.spotify.music", "Spotify"))
        assertEquals(IosIconKey.TELEGRAM, IosIconMapper.resolveIconKey("org.telegram.messenger", "Telegram"))
        assertEquals(IosIconKey.SETTINGS, IosIconMapper.resolveIconKey("com.android.settings", "Settings"))
        assertEquals(IosIconKey.APP_STORE, IosIconMapper.resolveIconKey("com.android.vending", "Play Store"))
    }

    @Test
    fun `ios notification badge formatting handles counts cleanly`() {
        assertEquals("", formatIosBadgeCount(0))
        assertEquals("1", formatIosBadgeCount(1))
        assertEquals("9", formatIosBadgeCount(9))
        assertEquals("99", formatIosBadgeCount(99))
        assertEquals("2,920", formatIosBadgeCount(2920))
        assertEquals("999+", formatIosBadgeCount(12000))
    }
}
