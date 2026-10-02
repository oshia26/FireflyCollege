package com.fyrefly.fireflycollege.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.sin

/**
 * A few drifting, blinking fireflies. Pure Canvas — no image or animation library,
 * deterministic seeds so it doesn't jump between recompositions.
 */
@Composable
fun FireflyCanvas(
    modifier: Modifier = Modifier,
    fireflyCount: Int = 7
) {
    val tint = MaterialTheme.colorScheme.primary
    val accent = MaterialTheme.colorScheme.tertiary

    val seeds = remember {
        List(fireflyCount) { i ->
            Triple(
                (i * 73 % 97 + 11) / 108f,          // x base 0.1..0.9
                (i * 131 % 89 + 13) / 108f,         // y base
                (i * 27 % 100) / 100f               // phase
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "fireflies")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )
    val blink by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    Canvas(modifier = modifier) {
        val unit = size.minDimension
        seeds.forEachIndexed { index, (xBase, yBase, phase) ->
            val t = drift + phase * (2f * Math.PI).toFloat() * 2f
            val x = (xBase * size.width) + sin(t + index) * unit * 0.06f
            val y = (yBase * size.height) + sin((t + phase) * 1.7f) * unit * 0.05f
            val glow = ((blink + phase) % 1f)
            val alpha = 0.35f + 0.55f * glow
            val color = if (index % 3 == 2) accent else tint

            // halo
            drawCircle(color = color.copy(alpha = alpha * 0.12f), radius = unit * 0.085f, center = Offset(x, y))
            // body
            drawCircle(color = color.copy(alpha = alpha), radius = unit * 0.022f, center = Offset(x, y))
            // bright core
            drawCircle(color = Color.White.copy(alpha = alpha * 0.8f), radius = unit * 0.010f, center = Offset(x, y))
        }
    }
}
