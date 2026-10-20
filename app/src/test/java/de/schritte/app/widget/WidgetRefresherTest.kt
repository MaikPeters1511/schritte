package de.schritte.app.widget

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WidgetRefresherTest {
    private var updates = 0

    private fun TestScope.refresher() =
        WidgetRefresher(this, 30_000, { testScheduler.currentTime }) { updates++ }

    @Test fun firstRequest_updatesImmediately() = runTest {
        refresher().request()
        runCurrent()
        assertEquals(1, updates)
    }

    @Test fun requestWithinInterval_isDeferredToTrailingUpdate() = runTest {
        val r = refresher()
        r.request(); runCurrent()
        advanceTimeBy(10_000)
        r.request(); runCurrent()
        assertEquals(1, updates)
        advanceTimeBy(20_000); runCurrent()
        assertEquals(2, updates)
    }

    @Test fun manyRequestsWithinInterval_giveOneTrailingUpdate() = runTest {
        val r = refresher()
        r.request(); runCurrent()
        repeat(5) { advanceTimeBy(1_000); r.request() }
        advanceTimeBy(60_000); runCurrent()
        assertEquals(2, updates)
    }

    @Test fun requestAfterInterval_updatesImmediately() = runTest {
        val r = refresher()
        r.request(); runCurrent()
        advanceTimeBy(31_000)
        r.request(); runCurrent()
        assertEquals(2, updates)
    }
}
