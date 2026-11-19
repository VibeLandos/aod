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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.aod.ui.components.ScallopPowerButton
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Shield
import com.antigravity.aod.ui.theme.RyAccent
import com.antigravity.aod.ui.theme.RyBackground
import com.antigravity.aod.ui.theme.RyBorder
import com.antigravity.aod.ui.theme.RyGreen
import com.antigravity.aod.ui.theme.RyOrange
import com.antigravity.aod.ui.theme.RyPrimaryContainer
import com.antigravity.aod.ui.theme.RySurface
import com.antigravity.aod.ui.theme.RySurfaceElevated
import com.antigravity.aod.ui.theme.RySurfaceVariant
import com.antigravity.aod.ui.theme.RyTextMuted
import com.antigravity.aod.ui.theme.RyTextPrimary
import com.antigravity.aod.ui.theme.RyTextSecondary
import com.antigravity.aod.ui.viewmodel.AodViewModel

@Composable
fun HomeScreen(
    viewModel: AodViewModel,
    hasOverlayPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onNavigateToAod: () -> Unit,
    onNavigateToModes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RyBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Main Container Card matching ryNet design
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(RySurface)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Top Bar inside card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AOD Awake",
                    color = RyTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Battery status chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(RySurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (batteryInfo.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = if (batteryInfo.isCharging) RyGreen else RyAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${batteryInfo.level}%",
                        color = RyTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Hero Section (Scallop Button + Status)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // The signature wavy scallop button
                ScallopPowerButton(
                    isActive = settings.isKeepScreenOn,
                    onClick = { viewModel.toggleKeepAwake() },
                    size = 190.dp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Status text
                Text(
                    text = if (settings.isKeepScreenOn) "Экран включен" else "Не активно",
                    color = RyTextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Small pill badge: Mode info (matches ryNet "через Германия (БЫСТРЫЙ) ⚡")
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(RySurfaceElevated)
                        .clickable { onNavigateToModes() }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (settings.keepOnOnlyWhenCharging) "⚡" else "🌟",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (settings.keepOnOnlyWhenCharging) "При зарядке (АВТО)" else "Постоянный экран (MD3)",
                        color = RyTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = RyOrange,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Overlay Permission Banner if not granted
                if (!hasOverlayPermission) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(RySurfaceElevated)
                            .border(1.dp, RyOrange.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { onRequestOverlayPermission() }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Layers,
                            contentDescription = null,
                            tint = RyOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Разрешить «Поверх других окон»",
                                color = RyTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Удерживает экран включенным вне приложения",
                                color = RyTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "Вкл",
                            color = RyAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Notification Permission Banner if not granted on Android 13+
                if (!hasNotificationPermission) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(RySurfaceElevated)
                            .border(1.dp, RyAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { onRequestNotificationPermission() }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = RyAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Включить уведомление службы",
                                color = RyTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Управление экраном прямо из шторки",
                                color = RyTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "Разрешить",
                            color = RyAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom Inset Card (matches ryNet okak69-Chain item)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(RySurfaceVariant)
                    .border(1.dp, RyBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { onNavigateToAod() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
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
                        // Globe / Display circle icon
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(RyPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Language,
                                contentDescription = null,
                                tint = RyAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "AOD Режим заставки",
                                color = RyTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (settings.isAmoledMode) "AMOLED Deep Black · Без выгорания" else "Стандартный режим дисплея",
                                color = RyTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Right trailing indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (settings.isKeepScreenOn) "ON" else "${batteryInfo.level}",
                            color = if (settings.isKeepScreenOn) RyGreen else RyTextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = RyTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
