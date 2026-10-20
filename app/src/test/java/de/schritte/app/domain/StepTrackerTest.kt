package de.schritte.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class StepTrackerTest {
    private val zone = ZoneId.of("Europe/Berlin")
    private val tracker = StepTracker(zone)
    private val day = LocalDate.of(2026, 10, 20)

    private fun at(d: String) = LocalDateTime.parse(d).atZone(zone).toInstant()

    @Test
    fun firstEvent_startsAtZeroWithRawAsBase() {
        val s = tracker.onSensor(null, 5000, at("2026-10-20T08:00:00"))
        assertEquals(DayState(day, 0, 5000), s)
    }

    @Test
    fun sameDay_addsRawDelta() {
        val s = tracker.onSensor(DayState(day, 10, 5000), 5040, at("2026-10-20T09:00:00"))
        assertEquals(DayState(day, 50, 5040), s)
    }

    @Test
    fun newDay_resetsStepsAndRebasesOnRaw() {
        val s = tracker.onSensor(DayState(day, 900, 5000), 5100, at("2026-10-21T00:05:00"))
        assertEquals(DayState(LocalDate.of(2026, 10, 21), 0, 5100), s)
    }

    @Test
    fun reboot_countsNewRawAsDeltaAndKeepsSteps() {
        val s = tracker.onSensor(DayState(day, 100, 90000), 30, at("2026-10-20T10:00:00"))
        assertEquals(DayState(day, 130, 30), s)
    }

    @Test
    fun rebootAcrossMidnight_startsNewDayAtZero() {
        val s = tracker.onSensor(DayState(day, 100, 90000), 30, at("2026-10-21T07:00:00"))
        assertEquals(DayState(LocalDate.of(2026, 10, 21), 0, 30), s)
    }

    @Test
    fun unchangedRaw_keepsSteps() {
        val s = tracker.onSensor(DayState(day, 42, 5000), 5000, at("2026-10-20T11:00:00"))
        assertEquals(DayState(day, 42, 5000), s)
    }
}
