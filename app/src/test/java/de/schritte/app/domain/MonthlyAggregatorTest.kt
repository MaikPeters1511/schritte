package de.schritte.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class MonthlyAggregatorTest {
    private val from = LocalDate.of(2025, 11, 1)
    private val to = LocalDate.of(2026, 10, 20)

    @Test fun rangeAcrossYearBoundary_hasTwelveMonthsInOrder() {
        val s = MonthlyAggregator.summarize(emptyMap(), from, to)
        assertEquals(12, s.months.size)
        assertEquals(YearMonth.of(2025, 11), s.months.first().month)
        assertEquals(YearMonth.of(2026, 10), s.months.last().month)
    }

    @Test fun daysAreSummedPerMonth() {
        val steps = mapOf(
            LocalDate.of(2025, 12, 3) to 100,
            LocalDate.of(2025, 12, 31) to 50,
            LocalDate.of(2026, 1, 1) to 7,
        )
        val s = MonthlyAggregator.summarize(steps, from, to)
        assertEquals(150, s.months.first { it.month == YearMonth.of(2025, 12) }.steps)
        assertEquals(7, s.months.first { it.month == YearMonth.of(2026, 1) }.steps)
    }

    @Test fun monthsWithoutDataAreZero() {
        val s = MonthlyAggregator.summarize(mapOf(LocalDate.of(2026, 3, 5) to 10), from, to)
        assertEquals(0, s.months.first { it.month == YearMonth.of(2026, 2) }.steps)
    }

    @Test fun dataOutsideRangeIsIgnored() {
        val steps = mapOf(LocalDate.of(2025, 10, 31) to 999, LocalDate.of(2026, 10, 21) to 999, from to 5)
        val s = MonthlyAggregator.summarize(steps, from, to)
        assertEquals(5, s.total)
    }

    @Test fun averagePerDay_countsEveryDayOfRange() {
        // 2025-11-01..2026-10-20 = 354 days
        val s = MonthlyAggregator.summarize(mapOf(to to 3_540), from, to)
        assertEquals(10, s.averagePerDay)
    }

    @Test fun bestMonth_earlierWinsOnTie() {
        val steps = mapOf(LocalDate.of(2026, 1, 1) to 500, LocalDate.of(2026, 2, 1) to 500)
        assertEquals(YearMonth.of(2026, 1), MonthlyAggregator.summarize(steps, from, to).best?.month)
    }

    @Test fun noSteps_hasNoBestMonth() = assertNull(MonthlyAggregator.summarize(emptyMap(), from, to).best)
}
