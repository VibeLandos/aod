package com.antigravity.aod.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.aod.BuildConfig
import com.antigravity.aod.ui.theme.RyAccent
import com.antigravity.aod.ui.theme.RyBackground
import com.antigravity.aod.ui.theme.RyBorder
import com.antigravity.aod.ui.theme.RyGreen
import com.antigravity.aod.ui.theme.RyOrange
import com.antigravity.aod.ui.theme.RyPrimary
import com.antigravity.aod.ui.theme.RyPrimaryContainer
import com.antigravity.aod.ui.theme.RySurface
import com.antigravity.aod.ui.theme.RySurfaceElevated
import com.antigravity.aod.ui.theme.RySurfaceVariant
import com.antigravity.aod.ui.theme.RyTextMuted
import com.antigravity.aod.ui.theme.RyTextPrimary
import com.antigravity.aod.ui.theme.RyTextSecondary
import com.antigravity.aod.ui.viewmodel.AodViewModel
import com.antigravity.aod.updater.GithubRelease
import com.antigravity.aod.updater.UpdateState

@Composable
fun SettingsScreen(
    viewModel: AodViewModel,
    hasOverlayPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RyBackground)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(RySurface)
                .padding(20.dp)
        ) {
            Text(
                text = "Настройки",
                color = RyTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Permission Card 1: Overlay Permission
                item {
                    PermissionStatusCard(
                        icon = Icons.Rounded.Layers,
                        title = "Поверх других приложений",
                        subtitle = if (hasOverlayPermission) "Разрешено ✓ (Экран не гаснет в любых приложениях)" else "Требуется разрешение (Нажмите для перехода в настройки)",
                        isGranted = hasOverlayPermission,
                        onClick = onRequestOverlayPermission
                    )
                }

                // Permission Card 2: Notifications Permission
                item {
                    PermissionStatusCard(
                        icon = Icons.Rounded.Notifications,
                        title = "Уведомление в шторке",
                        subtitle = if (hasNotificationPermission) "Включено ✓ (Кнопки быстрого управления активны)" else "Отключено (Нажмите для запроса разрешения)",
                        isGranted = hasNotificationPermission,
                        onClick = onRequestNotificationPermission
                    )
                }

                // Setting 1: AMOLED True Black
                item {
                    SettingToggleCard(
                        icon = Icons.Rounded.DarkMode,
                        title = "AMOLED True Black",
                        subtitle = "Отключает подсветку черных пикселей экрана для экономии батареи",
                        checked = settings.isAmoledMode,
                        onCheckedChange = { viewModel.setAmoledMode(it) }
                    )
                }

                // Setting 2: Anti-Burn-in Protection
                item {
                    SettingToggleCard(
                        icon = Icons.Rounded.Shield,
                        title = "Защита матрицы OLED",
                        subtitle = "Смещает часы и виджеты каждые 60 секунд, предотвращая выгорание",
                        checked = settings.antiBurnInEnabled,
                        onCheckedChange = { viewModel.setAntiBurnIn(it) }
                    )
                }

                // Setting 3: Charging only
                item {
                    SettingToggleCard(
                        icon = Icons.Rounded.BatteryChargingFull,
                        title = "Только при зарядке",
                        subtitle = "Экран включен исключительно когда телефон подключен к сети",
                        checked = settings.keepOnOnlyWhenCharging,
                        onCheckedChange = { viewModel.setChargingOnly(it) }
                    )
                }

                // Setting 4: Show on Lock Screen (AOD Mode)
                item {
                    SettingToggleCard(
                        icon = Icons.Rounded.Lock,
                        title = "AOD на экране блокировки",
                        subtitle = "Позволяет заставке часов отображаться при заблокированном телефоне",
                        checked = settings.showOnLockScreen,
                        onCheckedChange = { viewModel.setShowOnLockScreen(it) }
                    )
                }

                // Setting 4: Quick Settings Tile Guide Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(RySurfaceVariant)
                            .border(1.dp, RyBorder.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(RyPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Widgets,
                                    contentDescription = null,
                                    tint = RyAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Плитка в шторке быстрых настроек",
                                color = RyTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Опустите шторку Android, нажмите 'Изменить' (карандаш) и перетащите плитку 'Keep Screen On' для включения в один клик из любого приложения.",
                            color = RyTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                // Setting 5: Brightness Control
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(RySurfaceVariant)
                            .border(1.dp, RyBorder.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(RyPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BrightnessMedium,
                                    contentDescription = null,
                                    tint = RyAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Яркость AOD заставки",
                                    color = RyTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${(settings.customBrightness * 100).toInt()}%",
                                    color = RyAccent,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = settings.customBrightness,
                            onValueChange = { viewModel.setBrightness(it) },
                            valueRange = 0.05f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = RyAccent,
                                activeTrackColor = RyPrimary,
                                inactiveTrackColor = RySurfaceElevated
                            )
                        )
                    }
                }

                // Setting 6: GitHub Updates & Auto-Installer
                item {
                    GithubUpdateCard(
                        viewModel = viewModel,
                        currentVersion = BuildConfig.VERSION_NAME
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RySurfaceVariant)
            .border(1.dp, RyBorder.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (checked) RyPrimaryContainer else RySurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (checked) RyAccent else RyTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = RyTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = RyTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = RyPrimary,
                    uncheckedThumbColor = RyTextSecondary,
                    uncheckedTrackColor = RySurfaceElevated
                )
            )
        }
    }
}

@Composable
private fun PermissionStatusCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RySurfaceVariant)
            .border(
                1.dp,
                if (isGranted) RyGreen.copy(alpha = 0.4f) else RyOrange.copy(alpha = 0.5f),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) RyPrimaryContainer else RySurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) RyGreen else RyOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = RyTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = if (isGranted) RyGreen else RyTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (isGranted) "ВКЛ" else "НАСТРОИТЬ",
                color = if (isGranted) RyGreen else RyAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GithubUpdateCard(
    viewModel: AodViewModel,
    currentVersion: String
) {
    val settings by viewModel.settings.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var showAdvancedSettings by remember { mutableStateOf(false) }
    var tokenInput by remember { mutableStateOf(settings.githubToken) }
    var repoInput by remember { mutableStateOf(settings.githubRepo) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RySurfaceVariant)
            .border(1.dp, RyBorder.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(RyPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudDownload,
                        contentDescription = null,
                        tint = RyAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Обновления GitHub",
                        color = RyTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Версия v$currentVersion • ${settings.githubRepo}",
                        color = RyTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Check button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(RySurfaceElevated)
                    .clickable(enabled = updateState !is UpdateState.Checking && updateState !is UpdateState.Downloading) {
                        viewModel.checkForUpdates(currentVersion)
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (updateState is UpdateState.Checking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = RyAccent,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = RyAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ПРОВЕРИТЬ",
                            color = RyAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // State displays
        when (val state = updateState) {
            is UpdateState.UpToDate -> {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RyGreen.copy(alpha = 0.12f))
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = RyGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "У вас установлена самая актуальная версия v${state.currentVersion}",
                        color = RyGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            is UpdateState.UpdateAvailable -> {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(RyPrimaryContainer.copy(alpha = 0.4f))
                        .border(1.dp, RyAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🚀 Доступно обновление: ${state.release.tagName}",
                            color = RyAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.release.size > 0) {
                            Text(
                                text = "%.1f МБ".format(state.release.size / (1024f * 1024f)),
                                color = RyTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (state.release.body.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.release.body.take(200),
                            color = RyTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.downloadAndInstall(state.release) },
                        colors = ButtonDefaults.buttonColors(containerColor = RyAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Скачать и установить",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            is UpdateState.Downloading -> {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RySurfaceElevated)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Загрузка обновления: ${(state.progress * 100).toInt()}%",
                            color = RyTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (state.totalBytes > 0) {
                            Text(
                                text = "%.1f / %.1f МБ".format(
                                    state.downloadedBytes / (1024f * 1024f),
                                    state.totalBytes / (1024f * 1024f)
                                ),
                                color = RyAccent,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = RyAccent,
                        trackColor = RySurfaceVariant
                    )
                }
            }

            is UpdateState.ReadyToInstall -> {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.installApk(state.apkFile) },
                    colors = ButtonDefaults.buttonColors(containerColor = RyGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Установить ${state.release.tagName} (APK готов)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            is UpdateState.Error -> {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RyOrange.copy(alpha = 0.12f))
                        .border(1.dp, RyOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = state.message,
                        color = RyOrange,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            else -> {}
        }

        // Toggle advanced options (Token & Repo)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAdvancedSettings = !showAdvancedSettings }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = null,
                tint = RyTextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (showAdvancedSettings) "Скрыть настройки GitHub" else "Настройки репозитория и токена",
                color = RyTextMuted,
                fontSize = 11.sp
            )
        }

        if (showAdvancedSettings) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RySurfaceElevated)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = repoInput,
                    onValueChange = {
                        repoInput = it
                        viewModel.setGithubRepo(it)
                    },
                    label = { Text("GitHub репозиторий", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RyTextPrimary,
                        unfocusedTextColor = RyTextSecondary,
                        focusedBorderColor = RyAccent,
                        unfocusedBorderColor = RyBorder
                    )
                )

                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = {
                        tokenInput = it
                        viewModel.setGithubToken(it)
                    },
                    label = { Text("GitHub Token (для приватного репо)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RyTextPrimary,
                        unfocusedTextColor = RyTextSecondary,
                        focusedBorderColor = RyAccent,
                        unfocusedBorderColor = RyBorder
                    )
                )

                Text(
                    text = "Если репозиторий публичный, токен указывать не нужно — обновления скачиваются напрямую.",
                    color = RyTextMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
