package com.example.services

import android.app.role.RoleManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.KeyEvent
import android.view.Window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.launcher.BatteryStatus
import com.example.launcher.ConnectivityStatus
import com.example.launcher.DeviceStorageInfo
import kotlin.math.roundToInt

class DeviceSystemController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    private val _batteryStatus = MutableStateFlow(readBatteryStatus())
    val batteryStatus: StateFlow<BatteryStatus> = _batteryStatus.asStateFlow()

    private val _connectivityStatus = MutableStateFlow(readConnectivityStatus())
    val connectivityStatus: StateFlow<ConnectivityStatus> = _connectivityStatus.asStateFlow()

    private val _flashlightOn = MutableStateFlow(false)
    val flashlightOn: StateFlow<Boolean> = _flashlightOn.asStateFlow()

    private val _volumeFraction = MutableStateFlow(readMusicVolumeFraction())
    val volumeFraction: StateFlow<Float> = _volumeFraction.asStateFlow()

    private val _brightnessFraction = MutableStateFlow(readSystemBrightnessFraction())
    val brightnessFraction: StateFlow<Float> = _brightnessFraction.asStateFlow()

    private var torchCameraId: String? = null
    private var receiversRegistered = false

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == torchCameraId) {
                _flashlightOn.value = enabled
            }
        }
    }

    private val systemReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_CHANGED -> _batteryStatus.value = readBatteryStatus(intent)
                ConnectivityManager.CONNECTIVITY_ACTION,
                WifiManager.WIFI_STATE_CHANGED_ACTION,
                BluetoothAdapter.ACTION_STATE_CHANGED,
                Intent.ACTION_AIRPLANE_MODE_CHANGED -> _connectivityStatus.value = readConnectivityStatus()
                "android.media.VOLUME_CHANGED_ACTION" -> _volumeFraction.value = readMusicVolumeFraction()
            }
        }
    }

    init {
        torchCameraId = findFlashCameraId()
    }

    fun startListening() {
        if (receiversRegistered) return
        receiversRegistered = true
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction("android.media.VOLUME_CHANGED_ACTION")
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(systemReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(systemReceiver, filter)
            }
            cameraManager?.registerTorchCallback(torchCallback, null)
        } catch (_: Throwable) {
        }
        refreshTelemetry()
    }

    fun stopListening() {
        if (!receiversRegistered) return
        receiversRegistered = false
        try {
            context.unregisterReceiver(systemReceiver)
            cameraManager?.unregisterTorchCallback(torchCallback)
        } catch (_: Throwable) {
        }
    }

    fun refreshTelemetry() {
        _batteryStatus.value = readBatteryStatus()
        _connectivityStatus.value = readConnectivityStatus()
        _volumeFraction.value = readMusicVolumeFraction()
        _brightnessFraction.value = readSystemBrightnessFraction()
    }

    fun toggleFlashlight(): Boolean {
        val camId = torchCameraId ?: findFlashCameraId()?.also { torchCameraId = it } ?: return false
        val next = !_flashlightOn.value
        return try {
            cameraManager?.setTorchMode(camId, next)
            _flashlightOn.value = next
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun setVolumeFraction(fraction: Float) {
        val clamped = fraction.coerceIn(0f, 1f)
        _volumeFraction.value = clamped
        val am = audioManager ?: return
        try {
            val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = (clamped * maxVol).roundToInt().coerceIn(0, maxVol)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        } catch (_: Throwable) {
        }
    }

    fun setBrightnessFraction(fraction: Float, window: Window?) {
        val clamped = fraction.coerceIn(0.05f, 1f)
        _brightnessFraction.value = clamped
        try {
            window?.let { win ->
                val lp = win.attributes
                lp.screenBrightness = clamped
                win.attributes = lp
            }
            if (Settings.System.canWrite(context)) {
                val intBrightness = (clamped * 255f).roundToInt().coerceIn(10, 255)
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    intBrightness
                )
            }
        } catch (_: Throwable) {
        }
    }

    fun dispatchMediaKey(keyCode: Int) {
        val am = audioManager ?: return
        try {
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        } catch (_: Throwable) {
        }
    }

    fun isDefaultHomeLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName == context.packageName
    }

    fun openHomeLauncherPicker() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return
                }
            }
            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Throwable) {
            openSystemSettings(Settings.ACTION_SETTINGS)
        }
    }

    fun openCameraApp() {
        try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {
            openSystemSettings(Settings.ACTION_SETTINGS)
        }
    }

    fun openSystemSettings(action: String) {
        try {
            context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Throwable) {
            try {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: Throwable) {
            }
        }
    }

    fun openNotificationListenerSettings() {
        try {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Throwable) {
            openSystemSettings(Settings.ACTION_SETTINGS)
        }
    }

    fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {
        }
    }

    fun requestUninstall(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {
        }
    }

    fun readDeviceStorageInfo(): DeviceStorageInfo {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.blockSizeLong * stat.blockCountLong
            val availBytes = stat.blockSizeLong * stat.availableBlocksLong
            val totalGb = (totalBytes / (1024.0 * 1024.0 * 1024.0)).roundToInt()
            val availGb = availBytes / (1024.0 * 1024.0 * 1024.0)
            DeviceStorageInfo(
                totalGb = "$totalGb GB",
                availableGb = String.format(java.util.Locale.US, "%.2f GB", availGb)
            )
        } catch (_: Throwable) {
            DeviceStorageInfo()
        }
    }

    private fun findFlashCameraId(): String? {
        return try {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val lens = chars.get(CameraCharacteristics.LENS_FACING)
                hasFlash && lens == CameraCharacteristics.LENS_FACING_BACK
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun readBatteryStatus(batteryIntent: Intent? = null): BatteryStatus {
        val intent = batteryIntent ?: try {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (_: Throwable) {
            null
        }
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = if (intent != null) {
            val raw = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
            if (raw >= 0) ((raw * 100f) / scale).roundToInt()
            else bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
        } else {
            bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
        }
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val health = when (intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Degraded"
            else -> "Normal"
        }
        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 310) ?: 310
        return BatteryStatus(
            level = level.coerceIn(0, 100),
            isCharging = isCharging,
            healthLabel = health,
            temperatureC = tempTenths / 10f
        )
    }

    @Suppress("DEPRECATION")
    private fun readConnectivityStatus(): ConnectivityStatus {
        val airplaneOn = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
        } catch (_: Throwable) {
            false
        }
        val caps = try {
            connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        } catch (_: Throwable) {
            null
        }
        val wifiConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val cellularConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        val wifiEnabled = try {
            wifiManager?.isWifiEnabled == true
        } catch (_: Throwable) {
            wifiConnected
        }
        val ssid = if (wifiConnected) {
            try {
                val raw = wifiManager?.connectionInfo?.ssid?.replace("\"", "")
                if (raw.isNullOrBlank() || raw == "<unknown ssid>") "Wi-Fi" else raw
            } catch (_: Throwable) {
                "Wi-Fi"
            }
        } else if (wifiEnabled) {
            "On"
        } else {
            "Off"
        }
        val carrier = try {
            telephonyManager?.networkOperatorName?.takeIf { it.isNotBlank() } ?: "No Service"
        } catch (_: Throwable) {
            "No Service"
        }
        val bluetoothOn = try {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bm?.adapter?.isEnabled == true
        } catch (_: Throwable) {
            false
        }
        return ConnectivityStatus(
            wifiConnected = wifiConnected || wifiEnabled,
            wifiSsid = ssid,
            cellularConnected = cellularConnected,
            carrierName = carrier,
            bluetoothEnabled = bluetoothOn,
            airplaneModeOn = airplaneOn
        )
    }

    private fun readMusicVolumeFraction(): Float {
        val am = audioManager ?: return 0.5f
        return try {
            val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            (cur.toFloat() / max.toFloat()).coerceIn(0f, 1f)
        } catch (_: Throwable) {
            0.5f
        }
    }

    private fun readSystemBrightnessFraction(): Float {
        return try {
            val raw = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 160)
            (raw / 255f).coerceIn(0.05f, 1f)
        } catch (_: Throwable) {
            0.65f
        }
    }
}
