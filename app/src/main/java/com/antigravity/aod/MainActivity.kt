package com.antigravity.aod

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.antigravity.aod.ui.screens.AodDisplayScreen
import com.antigravity.aod.ui.screens.AodProfilesScreen
import com.antigravity.aod.ui.screens.HomeScreen
import com.antigravity.aod.ui.screens.ModesScreen
import com.antigravity.aod.ui.screens.SettingsScreen
import com.antigravity.aod.ui.theme.AodTheme
import com.antigravity.aod.ui.theme.RyAccent
import com.antigravity.aod.ui.theme.RyBackground
import com.antigravity.aod.ui.theme.RyPrimaryContainer
import com.antigravity.aod.ui.theme.RyTextPrimary
import com.antigravity.aod.ui.theme.RyTextSecondary
import com.antigravity.aod.ui.viewmodel.AodViewModel

enum class NavDestination(val label: String, val icon: ImageVector) {
    HOME("Главная", Icons.Rounded.Home),
    MODES("Режимы", Icons.Rounded.Storage),
    PROFILES("Профили", Icons.Rounded.Link),
    SETTINGS("Настройки", Icons.Rounded.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: AodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AodTheme {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val settings by viewModel.settings.collectAsState()

                // Keep screen on at window level when enabled
                LaunchedEffect(settings.isKeepScreenOn) {
                    if (settings.isKeepScreenOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                // Check overlay permission
                var hasOverlayPermission by remember {
                    mutableStateOf(Settings.canDrawOverlays(context))
                }

                // Check notification permission (Android 13+)
                var hasNotificationPermission by remember {
                    mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else {
                            true
                        }
                    )
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        hasNotificationPermission = isGranted
                    }
                )

                // Refresh permission status on resume
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            hasOverlayPermission = Settings.canDrawOverlays(context)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                hasNotificationPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // Request notification permission on first launch
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                val onRequestOverlay = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }

                val onRequestNotification = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                var currentScreen by remember { mutableStateOf(NavDestination.HOME) }
                var isAodDisplayActive by remember { mutableStateOf(false) }

                // Lock screen safety: never show app over lock screen unless explicitly enabled for AOD screensaver
                LaunchedEffect(settings.showOnLockScreen, isAodDisplayActive) {
                    val allowOnLock = settings.showOnLockScreen && isAodDisplayActive
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        setShowWhenLocked(allowOnLock)
                        setTurnScreenOn(allowOnLock)
                    } else {
                        @Suppress("DEPRECATION")
                        if (allowOnLock) {
                            window.addFlags(
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            )
                        } else {
                            window.clearFlags(
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            )
                        }
                    }
                }

                if (isAodDisplayActive) {
                    AodDisplayScreen(
                        viewModel = viewModel,
                        onExit = { isAodDisplayActive = false }
                    )
                } else {
                    MainAppScaffold(
                        currentScreen = currentScreen,
                        hasOverlayPermission = hasOverlayPermission,
                        hasNotificationPermission = hasNotificationPermission,
                        onRequestOverlayPermission = onRequestOverlay,
                        onRequestNotificationPermission = onRequestNotification,
                        onScreenSelect = { currentScreen = it },
                        onLaunchAod = { isAodDisplayActive = true },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    currentScreen: NavDestination,
    hasOverlayPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onScreenSelect: (NavDestination) -> Unit,
    onLaunchAod: () -> Unit,
    viewModel: AodViewModel
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = RyBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentScreen) {
                    NavDestination.HOME -> HomeScreen(
                        viewModel = viewModel,
                        hasOverlayPermission = hasOverlayPermission,
                        hasNotificationPermission = hasNotificationPermission,
                        onRequestOverlayPermission = onRequestOverlayPermission,
                        onRequestNotificationPermission = onRequestNotificationPermission,
                        onNavigateToAod = onLaunchAod,
                        onNavigateToModes = { onScreenSelect(NavDestination.MODES) }
                    )
                    NavDestination.MODES -> ModesScreen(
                        viewModel = viewModel
                    )
                    NavDestination.PROFILES -> AodProfilesScreen(
                        viewModel = viewModel,
                        onLaunchAod = onLaunchAod
                    )
                    NavDestination.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        hasOverlayPermission = hasOverlayPermission,
                        hasNotificationPermission = hasNotificationPermission,
                        onRequestOverlayPermission = onRequestOverlayPermission,
                        onRequestNotificationPermission = onRequestNotificationPermission
                    )
                }
            }

            // Bottom Navigation Pill Bar (matches ryNet active pill navigation)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RyBackground)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavDestination.entries.forEach { destination ->
                    val isSelected = currentScreen == destination
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (isSelected) RyPrimaryContainer else RyBackground)
                            .clickable { onScreenSelect(destination) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                tint = if (isSelected) RyAccent else RyTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = destination.label,
                                    color = RyTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
