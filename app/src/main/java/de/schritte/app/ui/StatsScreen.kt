package de.schritte.app.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.schritte.app.domain.Stats
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StatsScreen(vm: StatsViewModel, modifier: Modifier = Modifier) {
    val range by vm.range.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle(Stats(emptyList(), 0, 0, null))
    val color = MaterialTheme.colorScheme.primary
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(range == StatsRange.WEEK, { vm.range.value = StatsRange.WEEK }, { Text("Woche") })
            FilterChip(range == StatsRange.MONTH, { vm.range.value = StatsRange.MONTH }, { Text("Monat") })
        }
        Canvas(Modifier.fillMaxWidth().height(200.dp)) {
            val max = (stats.days.maxOfOrNull { it.steps } ?: 0).coerceAtLeast(1)
            val slot = size.width / stats.days.size.coerceAtLeast(1)
            stats.days.forEachIndexed { i, d ->
                val h = size.height * d.steps / max
                drawRect(color, Offset(i * slot + slot * 0.1f, size.height - h), Size(slot * 0.8f, h))
            }
        }
        val fmt = { n: Int -> String.format(Locale.GERMANY, "%,d", n) }
        Text("Summe: ${fmt(stats.total)}", style = MaterialTheme.typography.titleMedium)
        Text("Durchschnitt pro Tag: ${fmt(stats.average)}")
        stats.best?.let {
            Text("Bester Tag: ${it.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} (${fmt(it.steps)})")
        }
    }
}
