package de.schritte.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

@Composable
fun StatsScreen(vm: StatsViewModel, modifier: Modifier = Modifier) {
    val range by vm.range.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle(StatsUi.Empty)
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(range == StatsRange.WEEK, { vm.range.value = StatsRange.WEEK }, { Text("Woche") })
            FilterChip(range == StatsRange.MONTH, { vm.range.value = StatsRange.MONTH }, { Text("Monat") })
            FilterChip(range == StatsRange.YEAR, { vm.range.value = StatsRange.YEAR }, { Text("Jahr") })
        }
        LineChart(stats.points, Modifier.fillMaxWidth().height(220.dp), previous = stats.comparison?.previous)
        stats.comparison?.let { c ->
            val muted = MaterialTheme.colorScheme.onSurfaceVariant
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("━ Diese Woche", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                Text("┅ Vorwoche", color = muted, style = MaterialTheme.typography.labelLarge)
            }
            val change = when {
                c.diff > 0 -> Color(0xFF34A853)
                c.diff < 0 -> Color(0xFFD93025)
                else -> MaterialTheme.colorScheme.onSurface
            }
            Text("Vorwoche: ${String.format(Locale.GERMANY, "%,d", c.previousTotal)}")
            Row {
                Text("Unterschied: ")
                Text(formatDiff(c.diff, c.percent), color = change, fontWeight = FontWeight.Bold)
            }
        }
        val fmt = { n: Int -> String.format(Locale.GERMANY, "%,d", n) }
        Text("Summe: ${fmt(stats.total)}", style = MaterialTheme.typography.titleMedium)
        Text("Durchschnitt pro Tag: ${fmt(stats.average)}")
        stats.best?.let {
            Text("${if (range == StatsRange.YEAR) "Bester Monat" else "Bester Tag"}: ${it.label} (${fmt(it.steps)})")
        }
    }
}
