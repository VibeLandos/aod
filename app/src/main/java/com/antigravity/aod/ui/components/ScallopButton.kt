package com.antigravity.aod.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antigravity.aod.ui.theme.RyScallopBadge
import com.antigravity.aod.ui.theme.RyScallopBadgeActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom scalloped badge button matching the signature organic wavy button
 * from ryNet reference screenshot.
 */
@Composable
fun ScallopPowerButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val badgeColor by animateColorAsState(
        targetValue = if (isActive) RyScallopBadgeActive else RyScallopBadge,
        animationSpec = tween(durationMillis = 350),
        label = "badgeColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 250),
        label = "scale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val radius = (this.size.minDimension / 2f) * 0.95f
            val center = this.center
            val points = 14 // 14 gentle wavy lobes like ryNet badge
            val amplitude = radius * 0.08f // Soft organic wave depth
            val path = Path()

            val step = (2 * PI) / (points * 10)
            var angle = 0.0
            var first = true

            while (angle < 2 * PI) {
                // Harmonic wave formula for smooth scalloped rim
                val r = radius + amplitude * cos(points * angle).toFloat()
                val x = center.x + r * cos(angle).toFloat()
                val y = center.y + r * sin(angle).toFloat()

                if (first) {
                    path.moveTo(x, y)
                    first = false
                } else {
                    path.lineTo(x, y)
                }
                angle += step
            }
            path.close()

            drawPath(
                path = path,
                color = badgeColor,
                style = Fill
            )
        }

        // Center Icon
        Icon(
            imageVector = if (isActive) Icons.Rounded.WbSunny else Icons.Rounded.Bedtime,
            contentDescription = if (isActive) "Screen Awake Active" else "Screen Normal Sleep",
            tint = Color.White,
            modifier = Modifier.size(size * 0.42f)
        )
    }
}
