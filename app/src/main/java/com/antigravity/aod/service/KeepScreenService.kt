package com.antigravity.aod.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.antigravity.aod.MainActivity
import com.antigravity.aod.R
import com.antigravity.aod.data.AodPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class KeepScreenService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var screenWakeLock: PowerManager.WakeLock? = null
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private lateinit var prefs: AodPreferences
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private var powerReceiver: BroadcastReceiver? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        prefs = AodPreferences.getInstance(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        registerPowerReceiver()

        // Real-time synchronization: observe settings changes
        serviceScope.launch {
            prefs.settingsFlow.collect { settings ->
                if (settings.isKeepScreenOn) {
                    applyKeepScreenState()
                    setupTimeout()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopKeepScreenService()
            return START_NOT_STICKY
        }

        applyKeepScreenState()
        val notification = buildForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)
        prefs.setKeepScreenOn(true)

        setupTimeout()

        return START_STICKY
    }

    override fun onDestroy() {
        cleanup()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Applies screen keep-awake via:
     * 1. Invisible 1x1 transparent overlay with FLAG_KEEP_SCREEN_ON (requires SYSTEM_ALERT_WINDOW).
     * 2. Screen wake lock (fallback).
     * 3. CPU Partial WakeLock (keeps background active).
     */
    private fun applyKeepScreenState() {
        val settings = prefs.settingsFlow.value
        val isChargingOnly = settings.keepOnOnlyWhenCharging

        if (isChargingOnly && !isDeviceCharging()) {
            // Charging-only mode active, but device is unplugged: Pause keep awake
            removeOverlay()
            releaseScreenWakeLock()
            updateNotification("Ожидание зарядки 🔌", "Экран включится при подключении кабеля")
            return
        }

        // Active state
        addOverlay()
        acquireWakeLocks()

        val subtitle = if (isChargingOnly) {
            "Режим: Только при зарядке ⚡"
        } else if (settings.timeoutMinutes > 0) {
            "Таймер: ${settings.timeoutMinutes} мин"
        } else {
            "Постоянный активный экран"
        }

        updateNotification("Экран включен", subtitle)
    }

    private fun addOverlay() {
        if (overlayView != null) return
        if (!Settings.canDrawOverlays(this)) return

        try {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                1, 1,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.START or Gravity.TOP
            }

            val view = View(this)
            windowManager?.addView(view, params)
            overlayView = view
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeOverlay() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayView = null
    }

    @Suppress("DEPRECATION")
    private fun acquireWakeLocks() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return

        // 1. CPU Partial WakeLock
        if (wakeLock == null) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "AOD::CpuWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(24 * 60 * 60 * 1000L)
            }
        }

        // 2. Screen Bright WakeLock as safety fallback alongside overlay
        if (screenWakeLock == null) {
            try {
                screenWakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                    "AOD::ScreenWakeLock"
                ).apply {
                    setReferenceCounted(false)
                    acquire(24 * 60 * 60 * 1000L)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun releaseScreenWakeLock() {
        screenWakeLock?.let {
            if (it.isHeld) it.release()
        }
        screenWakeLock = null
    }

    private fun releaseWakeLocks() {
        releaseScreenWakeLock()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    private fun isDeviceCharging(): Boolean {
        val batteryStatus: Intent? = registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun registerPowerReceiver() {
        powerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_POWER_CONNECTED,
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        applyKeepScreenState()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(powerReceiver, filter)
    }

    private fun setupTimeout() {
        timeoutRunnable?.let { handler.removeCallbacks(it) }
        val timeoutMinutes = prefs.settingsFlow.value.timeoutMinutes
        if (timeoutMinutes > 0) {
            val delayMs = timeoutMinutes * 60 * 1000L
            timeoutRunnable = Runnable {
                stopKeepScreenService()
            }
            handler.postDelayed(timeoutRunnable!!, delayMs)
        }
    }

    private fun stopKeepScreenService() {
        cleanup()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun cleanup() {
        serviceScope.cancel()
        timeoutRunnable?.let { handler.removeCallbacks(it) }
        removeOverlay()
        releaseWakeLocks()
        prefs.setKeepScreenOn(false)
        powerReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) {}
            powerReceiver = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(title: String, text: String) {
        val notification = buildForegroundNotification(title, text)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }

    private fun buildForegroundNotification(
        title: String = getString(R.string.notification_title),
        text: String = getString(R.string.notification_text)
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, KeepScreenService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_tile)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Выключить",
                stopPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "channel_aod_keep_screen_v2"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.antigravity.aod.action.START"
        const val ACTION_STOP = "com.antigravity.aod.action.STOP"

        fun start(context: Context) {
            val intent = Intent(context, KeepScreenService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, KeepScreenService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
