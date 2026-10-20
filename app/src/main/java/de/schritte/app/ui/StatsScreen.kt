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
        LineChart(stats.points, Modifier.fillMaxWidth().height(220.dp))
        val fmt = { n: Int -> String.format(Locale.GERMANY, "%,d", n) }
        Text("Summe: ${fmt(stats.total)}", style = MaterialTheme.typography.titleMedium)
        Text("Durchschnitt pro Tag: ${fmt(stats.average)}")
        stats.best?.let {
            Text("${if (range == StatsRange.YEAR) "Bester Monat" else "Bester Tag"}: ${it.label} (${fmt(it.steps)})")
        }
    }
}
