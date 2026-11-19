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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.antigravity.aod.ui.theme.RyAccent
import com.antigravity.aod.ui.theme.RyBackground
import com.antigravity.aod.ui.theme.RyBorder
import com.antigravity.aod.ui.theme.RyGreen
import com.antigravity.aod.ui.theme.RyOrange
import com.antigravity.aod.ui.theme.RyPrimary
import com.antigravity.aod.ui.theme.RyPrimaryContainer
import com.antigravity.aod.ui.theme.RyScallopBadge
import com.antigravity.aod.ui.theme.RySurface
import com.antigravity.aod.ui.theme.RySurfaceElevated
import com.antigravity.aod.ui.theme.RySurfaceVariant
import com.antigravity.aod.ui.theme.RyTextPrimary
import com.antigravity.aod.ui.theme.RyTextSecondary
import com.antigravity.aod.ui.viewmodel.AodViewModel

data class ScreenModeItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeoutMinutes: Int,
    val onlyCharging: Boolean = false,
    val icon: ImageVector
)

@Composable
fun ModesScreen(
    viewModel: AodViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    var selectedFilter by remember { mutableStateOf("Все") }

    val modeItems = listOf(
        ScreenModeItem(
            id = "infinite",
            title = "Бесконечно активен",
            subtitle = "Экран никогда не гаснет · Ручное управление",
            timeoutMinutes = 0,
            icon = Icons.Rounded.Language
        ),
        ScreenModeItem(
            id = "charging",
            title = "Только при зарядке",
            subtitle = "Включается при подключении кабеля или беспроводной зарядки",
            timeoutMinutes = 0,
            onlyCharging = true,
            icon = Icons.Rounded.BatteryChargingFull
        ),
        ScreenModeItem(
            id = "15m",
            title = "Таймер: 15 минут",
            subtitle = "Идеально для чтения статей и рецептов",
            timeoutMinutes = 15,
            icon = Icons.Rounded.Timer
        ),
        ScreenModeItem(
            id = "30m",
            title = "Таймер: 30 минут",
            subtitle = "Для работы с кодом, навигации или учебы",
            timeoutMinutes = 30,
            icon = Icons.Rounded.Timer
        ),
        ScreenModeItem(
            id = "60m",
            title = "Таймер: 1 час",
            subtitle = "Презентации, трансляции и мониторинг",
            timeoutMinutes = 60,
            icon = Icons.Rounded.HourglassEmpty
        )
    )

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
            // Header: Title + Filter Pills (matches ryNet "Серверы" + "Как есть / Пинг / А-Я")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Режимы",
                    color = RyTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                // Filter Pill Group
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(RySurfaceElevated)
                        .padding(3.dp)
                ) {
                    listOf("Все", "Таймер", "Зарядка").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isSelected) RyPrimaryContainer else RySurfaceElevated)
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) RyAccent else RyTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search / Filter Input Bar (matches ryNet search pill)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(RySurfaceElevated)
                    .border(1.dp, RyBorder.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = RyTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Поиск режима или таймера",
                    color = RyTextSecondary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chips Row (matches ryNet: Invisible Shrimp / Свои серверы / VPN)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chip 1: AMOLED
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (settings.isAmoledMode) RyPrimaryContainer else RySurfaceElevated)
                        .clickable { viewModel.setAmoledMode(!settings.isAmoledMode) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🖤", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AMOLED Black",
                        color = if (settings.isAmoledMode) RyAccent else RyTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Chip 2: Charging Only
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (settings.keepOnOnlyWhenCharging) RyPrimaryContainer else RySurfaceElevated)
                        .clickable { viewModel.setChargingOnly(!settings.keepOnOnlyWhenCharging) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (settings.keepOnOnlyWhenCharging) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = RyAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "Только зарядка",
                        color = if (settings.keepOnOnlyWhenCharging) RyAccent else RyTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Modes List (matches ryNet server item cards)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(modeItems) { item ->
                    val isSelected = if (item.onlyCharging) {
                        settings.keepOnOnlyWhenCharging
                    } else {
                        !settings.keepOnOnlyWhenCharging && settings.timeoutMinutes == item.timeoutMinutes
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) RySurfaceElevated else RySurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) RyAccent.copy(alpha = 0.5f) else RyBorder.copy(alpha = 0.3f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                if (item.onlyCharging) {
                                    viewModel.setChargingOnly(true)
                                } else {
                                    viewModel.setChargingOnly(false)
                                    viewModel.setTimeoutMinutes(item.timeoutMinutes)
                                }
                            }
                            .padding(14.dp)
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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) RyPrimaryContainer else RySurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) RyAccent else RyTextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = item.title,
                                        color = RyTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.subtitle,
                                        color = RyTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = RyAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button in bottom right corner (matches ryNet blue lightning FAB in Image 2)
        FloatingActionButton(
            onClick = { viewModel.toggleKeepAwake() },
            containerColor = RyScallopBadge,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = "Toggle Screen Awake",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
