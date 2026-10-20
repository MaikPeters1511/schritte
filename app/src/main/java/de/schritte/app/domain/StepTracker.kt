package de.schritte.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DayState(val date: LocalDate, val steps: Int, val lastRaw: Long)

class StepTracker(private val zone: ZoneId) {
    fun onSensor(prev: DayState?, raw: Long, now: Instant): DayState {
        val today = now.atZone(zone).toLocalDate()
        if (prev == null || prev.date != today) return DayState(today, 0, raw)
        val delta = if (raw < prev.lastRaw) raw else raw - prev.lastRaw
        return prev.copy(steps = prev.steps + delta.toInt(), lastRaw = raw)
    }
}
