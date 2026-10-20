package de.schritte.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class StatsAggregatorTest {
    private val d1 = LocalDate.of(2026, 10, 18)
    private val d2 = d1.plusDays(1)
    private val d3 = d1.plusDays(2)

    @Test fun gapsBecomeZero() {
        val s = StatsAggregator.summarize(mapOf(d1 to 100, d3 to 300), d1, d3)
        assertEquals(listOf(DayCount(d1, 100), DayCount(d2, 0), DayCount(d3, 300)), s.days)
    }

    @Test fun totalAndAverageIncludeGaps() {
        val s = StatsAggregator.summarize(mapOf(d1 to 100, d3 to 300), d1, d3)
        assertEquals(400, s.total)
        assertEquals(133, s.average)
    }

    @Test fun bestDay_earlierWinsOnTie() {
        val s = StatsAggregator.summarize(mapOf(d1 to 500, d2 to 500, d3 to 10), d1, d3)
        assertEquals(DayCount(d1, 500), s.best)
    }

    @Test fun noSteps_hasNoBestDay() {
        assertNull(StatsAggregator.summarize(emptyMap(), d1, d3).best)
    }

    @Test fun dataOutsideRangeIsIgnored() {
        val s = StatsAggregator.summarize(mapOf(d1.minusDays(1) to 999, d2 to 5), d1, d3)
        assertEquals(5, s.total)
    }
}
