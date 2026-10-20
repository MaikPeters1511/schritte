package de.schritte.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class GoalProgressTest {
    @Test fun zeroSteps_isZero() = assertEquals(GoalProgress(0f, 0), goalProgress(0, 10_000))

    @Test fun half_isHalf() = assertEquals(GoalProgress(0.5f, 50), goalProgress(5_000, 10_000))

    @Test fun exactlyGoal_isFull() = assertEquals(GoalProgress(1f, 100), goalProgress(10_000, 10_000))

    @Test fun overGoal_ringStaysFullButPercentKeepsCounting() =
        assertEquals(GoalProgress(1f, 123), goalProgress(12_345, 10_000))

    @Test fun justBelowGoal_neverShowsHundredPercent() =
        assertEquals(99, goalProgress(9_999, 10_000).percent)

    @Test fun invalidGoal_isZero() = assertEquals(GoalProgress(0f, 0), goalProgress(5_000, 0))
}
