package de.schritte.app.service

import de.schritte.app.domain.DayState
import java.time.LocalDate

/** Sends at most one "goal reached" notification per day. */
class GoalNotifier(
    private val goal: suspend () -> Int,
    private val lastNotified: suspend () -> LocalDate?,
    private val markNotified: suspend (LocalDate) -> Unit,
    private val notify: suspend (steps: Int, goal: Int) -> Unit,
) {
    suspend fun onSteps(state: DayState) {
        val target = goal()
        if (state.steps < target) return
        if (lastNotified() == state.date) return
        markNotified(state.date)
        notify(state.steps, target)
    }
}
