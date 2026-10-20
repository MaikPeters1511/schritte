package de.schritte.app.domain

import kotlin.math.roundToInt

data class WeekComparison(val diff: Int, val percent: Int?)

fun compareWeeks(current: Int, previous: Int): WeekComparison {
    val diff = current - previous
    val percent = if (previous > 0) (diff * 100.0 / previous).roundToInt() else null
    return WeekComparison(diff, percent)
}
