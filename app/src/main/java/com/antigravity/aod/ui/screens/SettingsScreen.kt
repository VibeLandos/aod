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
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
