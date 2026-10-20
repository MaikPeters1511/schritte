package de.schritte.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.schritte.app.domain.DayCount
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayFormat = DateTimeFormatter.ofPattern("d.M.")

@Composable
fun LineChart(days: List<DayCount>, modifier: Modifier = Modifier) {
    val line = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val measurer = rememberTextMeasurer()
    val label = TextStyle(fontSize = 11.sp, color = textColor)

    Canvas(modifier) {
        val left = 44.dp.toPx()
        val bottom = 24.dp.toPx()
        val top = 24.dp.toPx()
        val right = 20.dp.toPx()
        val plotW = size.width - left - right
        val plotH = size.height - bottom - top
        val max = ChartScale.niceMax(days.maxOfOrNull { it.steps } ?: 0)

        fun y(v: Int) = top + plotH * (1f - v.toFloat() / max)
        fun x(i: Int) = left + if (days.size <= 1) plotW / 2 else plotW * i / (days.size - 1)

        ChartScale.yTicks(max).forEach { tick ->
            drawLine(grid, Offset(left, y(tick)), Offset(size.width - right, y(tick)), 1.dp.toPx())
            val text = String.format(Locale.GERMANY, "%,d", tick)
            val m = measurer.measure(text, label)
            drawText(m, topLeft = Offset(left - m.size.width - 6.dp.toPx(), y(tick) - m.size.height / 2f))
        }

        ChartScale.labelIndices(days.size).forEach { i ->
            val m = measurer.measure(days[i].date.format(dayFormat), label)
            drawText(m, topLeft = Offset(x(i) - m.size.width / 2f, size.height - bottom + 6.dp.toPx()))
        }

        val path = Path()
        days.forEachIndexed { i, d ->
            if (i == 0) path.moveTo(x(i), y(d.steps)) else path.lineTo(x(i), y(d.steps))
        }
        drawPath(path, line, style = Stroke(width = 2.5.dp.toPx()))

        val radius = if (days.size > 10) 2.5.dp.toPx() else 4.dp.toPx()
        days.forEachIndexed { i, d -> drawCircle(line, radius, Offset(x(i), y(d.steps))) }

        val bestIndex = days.indices.maxByOrNull { days[it].steps }
        if (bestIndex != null && days[bestIndex].steps > 0) {
            val center = Offset(x(bestIndex), y(days[bestIndex].steps))
            drawCircle(line, 6.dp.toPx(), center, style = Stroke(2.dp.toPx()))
            val m = measurer.measure(
                String.format(Locale.GERMANY, "%,d", days[bestIndex].steps),
                label.copy(color = line),
            )
            val tx = (center.x - m.size.width / 2f).coerceIn(left, size.width - m.size.width.toFloat())
            drawText(m, topLeft = Offset(tx, center.y - m.size.height - 8.dp.toPx()))
        }
    }
}
