package de.schritte.app.domain

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class MonthCount(val month: YearMonth, val steps: Int)

data class MonthlyStats(
    val months: List<MonthCount>,
    val total: Int,
    val averagePerDay: Int,
    val best: MonthCount?,
)

object MonthlyAggregator {
    fun summarize(steps: Map<LocalDate, Int>, from: LocalDate, to: LocalDate): MonthlyStats {
        val inRange = steps.filterKeys { !it.isBefore(from) && !it.isAfter(to) }
        val sums = inRange.entries.groupBy({ YearMonth.from(it.key) }, { it.value }).mapValues { it.value.sum() }
        val months = generateSequence(YearMonth.from(from)) { it.plusMonths(1) }
            .takeWhile { it <= YearMonth.from(to) }
            .map { MonthCount(it, sums[it] ?: 0) }
            .toList()
        val total = months.sumOf { it.steps }
        val days = ChronoUnit.DAYS.between(from, to) + 1
        val best = months.maxByOrNull { it.steps }?.takeIf { it.steps > 0 }
        return MonthlyStats(months, total, if (days <= 0) 0 else (total / days).toInt(), best)
    }
}
