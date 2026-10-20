package de.schritte.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ComparisonFormatTest {
    @Test fun increase_hasPlusSignAndGermanThousands() = assertEquals("+7.548 (+14 %)", formatDiff(7_548, 14))

    @Test fun decrease_hasMinusSign() = assertEquals("-2.000 (-67 %)", formatDiff(-2_000, -67))

    @Test fun withoutPercent_showsOnlyDifference() = assertEquals("+500", formatDiff(500, null))

    @Test fun noChange_isSaidInWords() = assertEquals("unverändert", formatDiff(0, 0))
}
