package de.schritte.app.ui

import androidx.lifecycle.ViewModel
import de.schritte.app.domain.Stats
import de.schritte.app.domain.StatsAggregator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate

enum class StatsRange(val days: Long) { WEEK(7), MONTH(30) }

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    private val clock: Clock,
    private val stepsInRange: (LocalDate, LocalDate) -> Flow<Map<LocalDate, Int>>,
) : ViewModel() {
    val range = MutableStateFlow(StatsRange.WEEK)

    val stats: Flow<Stats> = range.flatMapLatest { r ->
        val to = LocalDate.now(clock)
        val from = to.minusDays(r.days - 1)
        stepsInRange(from, to).map { StatsAggregator.summarize(it, from, to) }
    }
}
