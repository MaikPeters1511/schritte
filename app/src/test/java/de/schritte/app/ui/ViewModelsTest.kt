package de.schritte.app.ui

import de.schritte.app.data.Settings
import de.schritte.app.domain.DayState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    }

    @Test fun today_withoutData_isZero() = runBlocking {
        val ui = TodayViewModel(flowOf(null), flowOf(Settings()), clock).ui.first()
        assertEquals(TodayUi(0, 0.0, 0.0), ui)
    }

    @Test fun stats_weekCoversLastSevenDaysIncludingToday() = runBlocking {
        var asked: Pair<LocalDate, LocalDate>? = null
        val vm = StatsViewModel(clock) { f, t -> asked = f to t; flowOf(mapOf(today to 500)) }
        val s = vm.stats.first()
        assertEquals(today.minusDays(6) to today, asked)
        assertEquals(7, s.days.size)
        assertEquals(500, s.total)
    }

    @Test fun stats_monthCoversLastThirtyDays() = runBlocking {
        val vm = StatsViewModel(clock) { _, _ -> flowOf(emptyMap()) }
        vm.range.value = StatsRange.MONTH
        assertEquals(30, vm.stats.first().days.size)
    }

    @Test fun settings_saveReturnsFalseWhenInvalid() = runBlocking {
        val vm = SettingsViewModel(flowOf(Settings()), { _ -> }) { h, _ -> require(h >= 100) }
        assertFalse(vm.save(50, 70))
        assertTrue(vm.save(180, 70))
    }

    @Test fun today_ignoresStepsFromEarlierDay() = runBlocking {
        val yesterday = DayState(today.minusDays(1), 8_000, 0)
        val ui = TodayViewModel(flowOf(yesterday), flowOf(Settings()), clock).ui.first()
        assertEquals(0, ui.steps)
    }

    @Test fun settings_setDarkThemeDelegates() = runBlocking {
        var received: Boolean? = null
        val vm = SettingsViewModel(flowOf(Settings()), { received = it }) { _, _ -> }
        vm.setDarkTheme(true)
        assertEquals(true, received)
    }
}
