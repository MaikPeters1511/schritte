package de.schritte.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class WeekComparisonTest {
    @Test fun increase_isPositive() = assertEquals(WeekComparison(10, 10), compareWeeks(110, 100))

    @Test fun decrease_isNegative() = assertEquals(WeekComparison(-10, -10), compareWeeks(90, 100))

    @Test fun same_isZero() = assertEquals(WeekComparison(0, 0), compareWeeks(100, 100))

    @Test fun previousZero_hasNoPercent() = assertEquals(WeekComparison(500, null), compareWeeks(500, 0))

    @Test fun percentIsRounded() {
        assertEquals(WeekComparison(7_548, 14), compareWeeks(59_648, 52_100))
        assertEquals(WeekComparison(-2, -67), compareWeeks(1, 3))
    }
}
