package de.schritte.app.service

import de.schritte.app.domain.DayState
import de.schritte.app.domain.StepTracker
import java.time.Clock

class StepRecorder(
    private val tracker: StepTracker,
    private val load: suspend () -> DayState?,
    private val save: suspend (DayState) -> Unit,
    private val clock: Clock,
) {
    suspend fun record(raw: Long): DayState {
        val state = tracker.onSensor(load(), raw, clock.instant())
        save(state)
        return state
    }
}
