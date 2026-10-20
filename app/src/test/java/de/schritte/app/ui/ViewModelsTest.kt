package de.schritte.app.ui

import de.schritte.app.data.Settings
import de.schritte.app.domain.DayState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ViewModelsTest {
    private val zone = ZoneId.of("Europe/Berlin")
    private val today = LocalDate.of(2026, 10, 20)
    private val clock = Clock.fixed(today.atTime(12, 0).atZone(zone).toInstant(), zone)

    @Test fun today_combinesStepsWithSettings() = runBlocking {
        val vm = TodayViewModel(flowOf(DayState(today, 10_000, 0)), flowOf(Settings(180, 80)), clock)
        val ui = vm.ui.first()
        assertEquals(10_000, ui.steps)
        assertEquals(7.47, ui.distanceKm, 1e-9)
        assertEquals(340.632, ui.kcal, 1e-6)
        assertEquals(10_000, ui.goal)
        assertEquals(1f, ui.progress, 0f)
        assertEquals(100, ui.percent)
    }

    @Test fun today_progressUsesConfiguredGoal() = runBlocking {
        val vm = TodayViewModel(flowOf(DayState(today, 5_000, 0)), flowOf(Settings(goalSteps = 20_000)), clock)
        val ui = vm.ui.first()
        assertEquals(20_000, ui.goal)
        assertEquals(0.25f, ui.progress, 0f)
        assertEquals(25, ui.percent)
    }

    @Test fun today_withoutData_isZero() = runBlocking {
        val ui = TodayViewModel(flowOf(null), flowOf(Settings()), clock).ui.first()
        assertEquals(TodayUi(0, 0.0, 0.0, 10_000, 0f, 0), ui)
    }

    @Test fun stats_weekCoversLastSevenDaysIncludingToday() = runBlocking {
        var asked: Pair<LocalDate, LocalDate>? = null
        val vm = StatsViewModel(clock) { f, t -> asked = f to t; flowOf(mapOf(today to 500)) }
        val s = vm.stats.first()
        assertEquals(today.minusDays(13) to today, asked)
        assertEquals(7, s.points.size)
        assertEquals(ChartPoint("20.10.", 500), s.points.last())
        assertEquals(500, s.total)
        assertEquals(BestUi("20.10.2026", 500), s.best)
    }

    @Test fun stats_weekComparesWithPreviousSevenDays() = runBlocking {
        val data = mapOf(today to 500, today.minusDays(7) to 200, today.minusDays(13) to 100)
        val vm = StatsViewModel(clock) { _, _ -> flowOf(data) }
        val c = vm.stats.first().comparison!!
        assertEquals(7, c.previous.size)
        assertEquals(100, c.previous.first())
        assertEquals(200, c.previous.last())
        assertEquals(300, c.previousTotal)
        assertEquals(200, c.diff)
        assertEquals(67, c.percent)
    }

    @Test fun stats_weekWithoutPreviousData_hasNoPercent() = runBlocking {
        val vm = StatsViewModel(clock) { _, _ -> flowOf(mapOf(today to 500)) }
        val c = vm.stats.first().comparison!!
        assertEquals(500, c.diff)
        assertNull(c.percent)
    }

    @Test fun stats_monthAndYearHaveNoComparison() = runBlocking {
        val vm = StatsViewModel(clock) { _, _ -> flowOf(emptyMap()) }
        vm.range.value = StatsRange.MONTH
        assertNull(vm.stats.first().comparison)
        vm.range.value = StatsRange.YEAR
        assertNull(vm.stats.first().comparison)
    }

    @Test fun stats_monthCoversLastThirtyDays() = runBlocking {
        val vm = StatsViewModel(clock) { _, _ -> flowOf(emptyMap()) }
        vm.range.value = StatsRange.MONTH
        assertEquals(30, vm.stats.first().points.size)
    }

    @Test fun stats_yearCoversLastTwelveMonthsAsMonthlySums() = runBlocking {
        var asked: Pair<LocalDate, LocalDate>? = null
        val data = mapOf(today to 500, today.minusDays(5) to 250)
        val vm = StatsViewModel(clock) { f, t -> asked = f to t; flowOf(data) }
        vm.range.value = StatsRange.YEAR
        val s = vm.stats.first()
        assertEquals(LocalDate.of(2025, 11, 1) to today, asked)
        assertEquals(12, s.points.size)
        assertEquals("Nov", s.points.first().label)
        assertEquals(ChartPoint("Okt", 750), s.points.last())
        assertEquals(750, s.total)
        assertEquals(BestUi("Oktober 2026", 750), s.best)
    }

    @Test fun settings_saveReturnsFalseWhenInvalid() = runBlocking {
        val vm = SettingsViewModel(flowOf(Settings()), { _ -> }) { h, _, _ -> require(h >= 100) }
        assertFalse(vm.save(50, 70, 10_000))
        assertTrue(vm.save(180, 70, 10_000))
    }

    @Test fun today_ignoresStepsFromEarlierDay() = runBlocking {
        val yesterday = DayState(today.minusDays(1), 8_000, 0)
        val ui = TodayViewModel(flowOf(yesterday), flowOf(Settings()), clock).ui.first()
        assertEquals(0, ui.steps)
    }

    @Test fun settings_setDarkThemeDelegates() = runBlocking {
        var received: Boolean? = null
        val vm = SettingsViewModel(flowOf(Settings()), { received = it }) { _, _, _ -> }
        vm.setDarkTheme(true)
        assertEquals(true, received)
    }
}
