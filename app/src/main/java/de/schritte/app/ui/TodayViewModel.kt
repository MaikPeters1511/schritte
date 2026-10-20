package de.schritte.app.ui

import androidx.lifecycle.ViewModel
import de.schritte.app.data.Settings
import de.schritte.app.domain.DayState
import de.schritte.app.domain.StepCalculator
import de.schritte.app.domain.goalProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import java.time.LocalDate

data class TodayUi(
    val steps: Int,
    val distanceKm: Double,
    val kcal: Double,
    val goal: Int,
    val progress: Float,
    val percent: Int,
)

class TodayViewModel(steps: Flow<DayState?>, settings: Flow<Settings>, clock: Clock) : ViewModel() {
    val ui: Flow<TodayUi> = combine(steps, settings) { day, s ->
        val n = day?.takeIf { it.date == LocalDate.now(clock) }?.steps ?: 0
        val calc = StepCalculator(s.heightCm, s.weightKg)
        val p = goalProgress(n, s.goalSteps)
        TodayUi(n, calc.distanceM(n) / 1000, calc.kcal(n), s.goalSteps, p.fraction, p.percent)
    }
}
