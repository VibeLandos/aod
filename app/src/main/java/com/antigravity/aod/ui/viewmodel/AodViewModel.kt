package com.antigravity.aod.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aod.data.AodPreferences
import com.antigravity.aod.data.AodSettings
import com.antigravity.aod.service.KeepScreenService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BatteryInfo(
    val level: Int = 100,
    val isCharging: Boolean = false
)

class AodViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = AodPreferences.getInstance(application)
    val settings: StateFlow<AodSettings> = prefs.settingsFlow

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { updateBatteryStatus(it) }
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = application.registerReceiver(batteryReceiver, filter)
        initialIntent?.let { updateBatteryStatus(it) }
    }

    private fun updateBatteryStatus(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val batteryPct = if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            100
        }

        _batteryInfo.value = BatteryInfo(level = batteryPct, isCharging = isCharging)
    }

    fun toggleKeepAwake() {
        val current = settings.value.isKeepScreenOn
        val newTarget = !current
        if (newTarget) {
            KeepScreenService.start(getApplication())
        } else {
            KeepScreenService.stop(getApplication())
        }
        prefs.setKeepScreenOn(newTarget)
    }

    fun setAmoledMode(enabled: Boolean) {
        prefs.setAmoledMode(enabled)
    }

    fun setChargingOnly(enabled: Boolean) {
        prefs.setKeepOnOnlyWhenCharging(enabled)
    }

    fun setTimeoutMinutes(minutes: Int) {
        prefs.setTimeoutMinutes(minutes)
    }

    fun setAntiBurnIn(enabled: Boolean) {
        prefs.setAntiBurnInEnabled(enabled)
    }

    fun setBrightness(brightness: Float) {
        prefs.setCustomBrightness(brightness)
    }

    fun setUseSystemBrightness(useSystem: Boolean) {
        prefs.setUseSystemBrightness(useSystem)
    }

    fun setShowOnLockScreen(enabled: Boolean) {
        prefs.setShowOnLockScreen(enabled)
    }

    override fun onCleared() {
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {
            // Already unregistered
        }
        super.onCleared()
    }
}
