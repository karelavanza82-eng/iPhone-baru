package com.example.icons

import java.util.Locale

enum class IosIconKey {
    PHONE,
    MESSAGES,
    SAFARI,
    SETTINGS,
    PHOTOS,
    CAMERA,
    APP_STORE,
    MUSIC,
    CALENDAR,
    CLOCK,
    WEATHER,
    MAPS,
    NOTES,
    REMINDERS,
    FILES,
    HEALTH,
    WALLET,
    MAIL,
    FACETIME,
    YOUTUBE,
    TIKTOK,
    INSTAGRAM,
    WHATSAPP,
    FACEBOOK,
    GMAIL,
    SPOTIFY,
    TELEGRAM,
    DISCORD,
    YOUTUBE_MUSIC,
    APPLE_TV,
    GAMES,
    TIPS,
    CALCULATOR,
    KEEP,
    DRIVE,
    PINTEREST,
    CHROME,
    GEMINI
}

object IosIconMapper {

    private val exactPackageMap: Map<String, IosIconKey> = mapOf(
        "com.google.android.dialer" to IosIconKey.PHONE,
        "com.android.dialer" to IosIconKey.PHONE,
        "com.samsung.android.dialer" to IosIconKey.PHONE,
        "com.google.android.apps.messaging" to IosIconKey.MESSAGES,
        "com.android.mms" to IosIconKey.MESSAGES,
        "com.samsung.android.messaging" to IosIconKey.MESSAGES,
        "com.android.chrome" to IosIconKey.CHROME,
        "org.mozilla.firefox" to IosIconKey.SAFARI,
        "com.sec.android.app.sbrowser" to IosIconKey.SAFARI,
        "com.android.settings" to IosIconKey.SETTINGS,
        "com.google.android.apps.photos" to IosIconKey.PHOTOS,
        "com.sec.android.gallery3d" to IosIconKey.PHOTOS,
        "com.android.gallery3d" to IosIconKey.PHOTOS,
        "com.google.android.GoogleCamera" to IosIconKey.CAMERA,
        "com.android.camera" to IosIconKey.CAMERA,
        "com.android.camera2" to IosIconKey.CAMERA,
        "com.sec.android.app.camera" to IosIconKey.CAMERA,
        "com.android.vending" to IosIconKey.APP_STORE,
        "com.google.android.youtube" to IosIconKey.YOUTUBE,
        "com.google.android.apps.youtube.music" to IosIconKey.YOUTUBE_MUSIC,
        "com.zhiliaoapp.musically" to IosIconKey.TIKTOK,
        "com.ss.android.ugc.trill" to IosIconKey.TIKTOK,
        "com.instagram.android" to IosIconKey.INSTAGRAM,
        "com.whatsapp" to IosIconKey.WHATSAPP,
        "com.whatsapp.w4b" to IosIconKey.WHATSAPP,
        "com.facebook.katana" to IosIconKey.FACEBOOK,
        "com.facebook.lite" to IosIconKey.FACEBOOK,
        "com.google.android.gm" to IosIconKey.GMAIL,
        "com.spotify.music" to IosIconKey.SPOTIFY,
        "org.telegram.messenger" to IosIconKey.TELEGRAM,
        "org.telegram.plus" to IosIconKey.TELEGRAM,
        "com.discord" to IosIconKey.DISCORD,
        "com.google.android.calendar" to IosIconKey.CALENDAR,
        "com.android.calendar" to IosIconKey.CALENDAR,
        "com.samsung.android.calendar" to IosIconKey.CALENDAR,
        "com.google.android.deskclock" to IosIconKey.CLOCK,
        "com.android.deskclock" to IosIconKey.CLOCK,
        "com.sec.android.app.clockpackage" to IosIconKey.CLOCK,
        "com.google.android.apps.maps" to IosIconKey.MAPS,
        "com.google.android.keep" to IosIconKey.KEEP,
        "com.google.android.apps.docs" to IosIconKey.DRIVE,
        "com.pinterest" to IosIconKey.PINTEREST,
        "com.google.android.calculator" to IosIconKey.CALCULATOR,
        "com.android.calculator2" to IosIconKey.CALCULATOR,
        "com.sec.android.app.popupcalculator" to IosIconKey.CALCULATOR,
        "com.google.android.apps.nbu.files" to IosIconKey.FILES,
        "com.android.documentsui" to IosIconKey.FILES,
        "com.google.android.apps.walletnfcrel" to IosIconKey.WALLET,
        "com.google.android.apps.fitness" to IosIconKey.HEALTH,
        "com.google.android.apps.bard" to IosIconKey.GEMINI,
        "com.google.android.apps.tachyon" to IosIconKey.FACETIME,
        "us.zoom.videomeetings" to IosIconKey.FACETIME,
        "com.google.android.apps.tasks" to IosIconKey.REMINDERS,
        "com.google.android.apps.weather" to IosIconKey.WEATHER,
        "com.google.android.videos" to IosIconKey.APPLE_TV,
        "com.google.android.play.games" to IosIconKey.GAMES
    )

    fun resolveIconKey(packageName: String, label: String): IosIconKey? {
        exactPackageMap[packageName]?.let { return it }
        val pkg = packageName.lowercase(Locale.ROOT)
        val lbl = label.lowercase(Locale.ROOT)
        return when {
            pkg.contains("whatsapp") || lbl == "whatsapp" -> IosIconKey.WHATSAPP
            pkg.contains("instagram") || lbl == "instagram" -> IosIconKey.INSTAGRAM
            pkg.contains("musically") || pkg.contains("tiktok") || lbl == "tiktok" -> IosIconKey.TIKTOK
            pkg.contains("youtube.music") || lbl == "yt music" || lbl == "youtube music" -> IosIconKey.YOUTUBE_MUSIC
            pkg.contains("youtube") || lbl == "youtube" -> IosIconKey.YOUTUBE
            pkg.contains("facebook.katana") || lbl == "facebook" -> IosIconKey.FACEBOOK
            pkg.contains("telegram") || lbl == "telegram" -> IosIconKey.TELEGRAM
            pkg.contains("spotify") || lbl == "spotify" -> IosIconKey.SPOTIFY
            pkg.contains("discord") || lbl == "discord" -> IosIconKey.DISCORD
            pkg.contains("pinterest") || lbl == "pinterest" -> IosIconKey.PINTEREST
            pkg.contains("google.android.gm") || lbl == "gmail" -> IosIconKey.GMAIL
            pkg.contains("email") || pkg.contains("mail") || lbl == "mail" -> IosIconKey.MAIL
            pkg.contains("dialer") || pkg.contains("phone") || lbl == "phone" -> IosIconKey.PHONE
            pkg.contains("messaging") || pkg.contains("mms") || lbl == "messages" -> IosIconKey.MESSAGES
            pkg.contains("chrome") || lbl == "chrome" -> IosIconKey.CHROME
            pkg.contains("browser") || lbl == "browser" || lbl == "safari" -> IosIconKey.SAFARI
            pkg.contains("settings") || lbl == "settings" -> IosIconKey.SETTINGS
            pkg.contains("gallery") || pkg.contains("photos") || lbl == "photos" -> IosIconKey.PHOTOS
            pkg.contains("camera") || lbl == "camera" -> IosIconKey.CAMERA
            pkg.contains("vending") || lbl.contains("play store") || lbl == "app store" -> IosIconKey.APP_STORE
            pkg.contains("calendar") || lbl == "calendar" -> IosIconKey.CALENDAR
            pkg.contains("clock") || pkg.contains("deskclock") || lbl == "clock" -> IosIconKey.CLOCK
            pkg.contains("weather") || lbl == "weather" -> IosIconKey.WEATHER
            pkg.contains("maps") || lbl == "maps" -> IosIconKey.MAPS
            pkg.contains("keep") || lbl == "keep notes" -> IosIconKey.KEEP
            pkg.contains("notes") || lbl == "notes" -> IosIconKey.NOTES
            pkg.contains("tasks") || pkg.contains("reminders") || lbl == "reminders" -> IosIconKey.REMINDERS
            pkg.contains("files") || pkg.contains("documentsui") || lbl == "files" -> IosIconKey.FILES
            pkg.contains("calculator") || lbl == "calculator" -> IosIconKey.CALCULATOR
            pkg.contains("wallet") || lbl == "wallet" -> IosIconKey.WALLET
            pkg.contains("fitness") || pkg.contains("health") || lbl == "health" -> IosIconKey.HEALTH
            pkg.contains("apps.docs") || lbl == "drive" -> IosIconKey.DRIVE
            pkg.contains("music") || lbl == "music" -> IosIconKey.MUSIC
            pkg.contains("gemini") || pkg.contains("bard") || lbl == "gemini" -> IosIconKey.GEMINI
            else -> null
        }
    }
}
