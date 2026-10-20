package de.schritte.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private val ReachedColor = Color(0xFF34A853)

@Composable
fun GoalRing(
    progress: Float,
    reached: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(progress) { animated.animateTo(progress, tween(900)) }
    val track = MaterialTheme.colorScheme.surfaceVariant
    val color = if (reached) ReachedColor else MaterialTheme.colorScheme.primary

    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val stroke = 18.dp.toPx()
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arc = Size(size.width - stroke, size.height - stroke)
            drawArc(track, 0f, 360f, false, topLeft, arc, style = Stroke(stroke))
            if (animated.value > 0f) {
                drawArc(color, -90f, 360f * animated.value, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round))
            }
        }
        content()
    }
}
