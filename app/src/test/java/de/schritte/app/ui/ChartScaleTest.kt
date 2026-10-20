package de.schritte.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ChartScaleTest {
    @Test fun niceMax_roundsUpToReadableValue() {
        assertEquals(15_000, ChartScale.niceMax(12_000))
        assertEquals(10_000, ChartScale.niceMax(10_000))
        assertEquals(4_000, ChartScale.niceMax(3_700))
        assertEquals(8_000, ChartScale.niceMax(7_001))
    }

    @Test fun niceMax_hasMinimumForEmptyData() = assertEquals(1_000, ChartScale.niceMax(0))

    @Test fun niceMax_worksForSmallValues() = assertEquals(5, ChartScale.niceMax(5))

    @Test fun yTicks_areZeroHalfAndMax() =
        assertEquals(listOf(0, 7_500, 15_000), ChartScale.yTicks(15_000))

    @Test fun labelIndices_weekLabelsEveryDay() =
        assertEquals((0..6).toList(), ChartScale.labelIndices(7))

    @Test fun labelIndices_monthLabelsEveryFifthDayEndingToday() =
        assertEquals(listOf(4, 9, 14, 19, 24, 29), ChartScale.labelIndices(30))

    @Test fun labelIndices_emptyForNoDays() = assertEquals(emptyList<Int>(), ChartScale.labelIndices(0))
}
