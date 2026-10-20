package de.schritte.app.ui

import androidx.lifecycle.ViewModel
import de.schritte.app.domain.MonthlyAggregator
import de.schritte.app.domain.StatsAggregator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class StatsRange { WEEK, MONTH, YEAR }

data class ChartPoint(val label: String, val value: Int)

data class BestUi(val label: String, val steps: Int)

data class StatsUi(val points: List<ChartPoint>, val total: Int, val average: Int, val best: BestUi?) {
    companion object { val Empty = StatsUi(emptyList(), 0, 0, null) }
}

private val shortMonths = listOf("Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez")
private val longMonths = listOf(
    "Januar", "Februar", "März", "April", "Mai", "Juni",
    "Juli", "August", "September", "Oktober", "November", "Dezember",
)
private val dayLabel = DateTimeFormatter.ofPattern("d.M.")
private val dayFull = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    private val clock: Clock,
    private val stepsInRange: (LocalDate, LocalDate) -> Flow<Map<LocalDate, Int>>,
) : ViewModel() {
    val range = MutableStateFlow(StatsRange.WEEK)

    val stats: Flow<StatsUi> = range.flatMapLatest { r ->
        val to = LocalDate.now(clock)
        when (r) {
            StatsRange.WEEK -> daily(to.minusDays(6), to)
            StatsRange.MONTH -> daily(to.minusDays(29), to)
            StatsRange.YEAR -> monthly(to.withDayOfMonth(1).minusMonths(11), to)
        }
    }

    private fun daily(from: LocalDate, to: LocalDate) = stepsInRange(from, to).map { data ->
        val s = StatsAggregator.summarize(data, from, to)
        StatsUi(
            s.days.map { ChartPoint(it.date.format(dayLabel), it.steps) },
            s.total,
            s.average,
            s.best?.let { BestUi(it.date.format(dayFull), it.steps) },
        )
    }

    private fun monthly(from: LocalDate, to: LocalDate) = stepsInRange(from, to).map { data ->
        val s = MonthlyAggregator.summarize(data, from, to)
        StatsUi(
            s.months.map { ChartPoint(shortMonths[it.month.monthValue - 1], it.steps) },
            s.total,
            s.averagePerDay,
            s.best?.let { BestUi(monthName(it.month), it.steps) },
        )
    }

    private fun monthName(m: YearMonth) = "${longMonths[m.monthValue - 1]} ${m.year}"
}
