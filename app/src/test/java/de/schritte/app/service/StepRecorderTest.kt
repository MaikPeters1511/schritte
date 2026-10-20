package de.schritte.app.service

import de.schritte.app.domain.DayState
import de.schritte.app.domain.StepTracker
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class StepRecorderTest {
    private val zone = ZoneId.of("Europe/Berlin")
    private val day = LocalDate.of(2026, 10, 20)
    private val clock = Clock.fixed(day.atTime(10, 0).atZone(zone).toInstant(), zone)
    private var stored: DayState? = null

    private val recorder = StepRecorder(StepTracker(zone), { stored }, { stored = it }, clock)

    @Test fun firstEvent_persistsBase() = runBlocking {
        recorder.record(1000)
        assertEquals(DayState(day, 0, 1000), stored)
    }

    @Test fun secondEvent_accumulates() = runBlocking {
        recorder.record(1000)
        recorder.record(1025)
        assertEquals(DayState(day, 25, 1025), stored)
    }

    @Test fun rebootBetweenEvents_keepsEarlierSteps() = runBlocking {
        stored = DayState(day, 500, 80_000)
        recorder.record(12)
        assertEquals(DayState(day, 512, 12), stored)
    }
}
