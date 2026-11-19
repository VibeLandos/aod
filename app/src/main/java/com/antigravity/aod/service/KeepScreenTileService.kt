package com.antigravity.aod.service

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.antigravity.aod.R
import com.antigravity.aod.data.AodPreferences

@RequiresApi(Build.VERSION_CODES.N)
class KeepScreenTileService : TileService() {

    private lateinit var prefs: AodPreferences

    override fun onCreate() {
        super.onCreate()
        prefs = AodPreferences.getInstance(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val currentState = qsTile?.state ?: Tile.STATE_INACTIVE
        if (currentState == Tile.STATE_ACTIVE) {
            KeepScreenService.stop(this)
            updateTile(Tile.STATE_INACTIVE, "Screen Awake Off")
        } else {
            KeepScreenService.start(this)
            updateTile(Tile.STATE_ACTIVE, "Screen Awake On")
        }
    }

    private fun updateTileState() {
        val isEnabled = prefs.settingsFlow.value.isKeepScreenOn
        val targetState = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        val subtitle = if (isEnabled) "Screen Awake On" else "Screen Awake Off"
        updateTile(targetState, subtitle)
    }

    private fun updateTile(state: Int, subtitle: String) {
        val tile = qsTile ?: return
        tile.state = state
        tile.icon = Icon.createWithResource(this, R.drawable.ic_tile)
        tile.label = getString(R.string.tile_label)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = subtitle
        }
        tile.updateTile()
    }
}
