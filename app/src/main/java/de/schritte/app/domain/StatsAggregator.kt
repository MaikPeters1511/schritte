package de.schritte.app.domain

import java.time.LocalDate

data class DayCount(val date: LocalDate, val steps: Int)

data class Stats(val days: List<DayCount>, val total: Int, val average: Int, val best: DayCount?)

object StatsAggregator {
    fun summarize(steps: Map<LocalDate, Int>, from: LocalDate, to: LocalDate): Stats {
        val days = generateSequence(from) { it.plusDays(1) }
            .takeWhile { !it.isAfter(to) }
            .map { DayCount(it, steps[it] ?: 0) }
            .toList()
        val total = days.sumOf { it.steps }
        val best = days.maxByOrNull { it.steps }?.takeIf { it.steps > 0 }
        return Stats(days, total, if (days.isEmpty()) 0 else total / days.size, best)
    }
}
