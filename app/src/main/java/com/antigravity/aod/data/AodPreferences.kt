package com.antigravity.aod.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AodSettings(
    val isKeepScreenOn: Boolean = false,
    val isAmoledMode: Boolean = true,
    val keepOnOnlyWhenCharging: Boolean = false,
    val timeoutMinutes: Int = 0, // 0 = Infinite / No Timeout
    val antiBurnInEnabled: Boolean = true,
    val customBrightness: Float = 0.5f,
    val useSystemBrightness: Boolean = true,
    val showOnLockScreen: Boolean = false,
    val githubToken: String = "",
    val githubRepo: String = "VibeLandos/aod"
)

class AodPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("aod_settings_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AodSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): AodSettings {
        return AodSettings(
            isKeepScreenOn = prefs.getBoolean(KEY_KEEP_SCREEN_ON, false),
            isAmoledMode = prefs.getBoolean(KEY_AMOLED_MODE, true),
            keepOnOnlyWhenCharging = prefs.getBoolean(KEY_ONLY_CHARGING, false),
            timeoutMinutes = prefs.getInt(KEY_TIMEOUT_MINUTES, 0),
            antiBurnInEnabled = prefs.getBoolean(KEY_ANTI_BURN_IN, true),
            customBrightness = prefs.getFloat(KEY_BRIGHTNESS, 0.5f),
            useSystemBrightness = prefs.getBoolean(KEY_USE_SYSTEM_BRIGHTNESS, true),
            showOnLockScreen = prefs.getBoolean(KEY_SHOW_ON_LOCK_SCREEN, false),
            githubToken = prefs.getString(KEY_GITHUB_TOKEN, "") ?: "",
            githubRepo = prefs.getString(KEY_GITHUB_REPO, "VibeLandos/aod") ?: "VibeLandos/aod"
        )
    }

    fun setKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(isKeepScreenOn = enabled)
    }

    fun setAmoledMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AMOLED_MODE, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(isAmoledMode = enabled)
    }

    fun setKeepOnOnlyWhenCharging(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ONLY_CHARGING, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(keepOnOnlyWhenCharging = enabled)
    }

    fun setTimeoutMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_TIMEOUT_MINUTES, minutes).apply()
        _settingsFlow.value = _settingsFlow.value.copy(timeoutMinutes = minutes)
    }

    fun setAntiBurnInEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANTI_BURN_IN, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(antiBurnInEnabled = enabled)
    }

    fun setCustomBrightness(brightness: Float) {
        prefs.edit().putFloat(KEY_BRIGHTNESS, brightness).apply()
        _settingsFlow.value = _settingsFlow.value.copy(customBrightness = brightness)
    }

    fun setUseSystemBrightness(useSystem: Boolean) {
        prefs.edit().putBoolean(KEY_USE_SYSTEM_BRIGHTNESS, useSystem).apply()
        _settingsFlow.value = _settingsFlow.value.copy(useSystemBrightness = useSystem)
    }

    fun setShowOnLockScreen(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_ON_LOCK_SCREEN, show).apply()
        _settingsFlow.value = _settingsFlow.value.copy(showOnLockScreen = show)
    }

    fun setGithubToken(token: String) {
        prefs.edit().putString(KEY_GITHUB_TOKEN, token).apply()
        _settingsFlow.value = _settingsFlow.value.copy(githubToken = token)
    }

    fun setGithubRepo(repo: String) {
        prefs.edit().putString(KEY_GITHUB_REPO, repo).apply()
        _settingsFlow.value = _settingsFlow.value.copy(githubRepo = repo)
    }

    companion object {
        private const val KEY_KEEP_SCREEN_ON = "key_keep_screen_on"
        private const val KEY_AMOLED_MODE = "key_amoled_mode"
        private const val KEY_ONLY_CHARGING = "key_only_charging"
        private const val KEY_TIMEOUT_MINUTES = "key_timeout_minutes"
        private const val KEY_ANTI_BURN_IN = "key_anti_burn_in"
        private const val KEY_BRIGHTNESS = "key_brightness"
        private const val KEY_USE_SYSTEM_BRIGHTNESS = "key_use_system_brightness"
        private const val KEY_SHOW_ON_LOCK_SCREEN = "key_show_on_lock_screen"
        private const val KEY_GITHUB_TOKEN = "key_github_token"
        private const val KEY_GITHUB_REPO = "key_github_repo"

        @Volatile
        private var INSTANCE: AodPreferences? = null

        fun getInstance(context: Context): AodPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AodPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
