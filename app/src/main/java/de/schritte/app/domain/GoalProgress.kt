package de.schritte.app.domain

data class GoalProgress(val fraction: Float, val percent: Int)

fun goalProgress(steps: Int, goal: Int): GoalProgress {
    if (goal <= 0) return GoalProgress(0f, 0)
    return GoalProgress(
        fraction = (steps.toFloat() / goal).coerceIn(0f, 1f),
        percent = (steps * 100L / goal).toInt(),
    )
}
