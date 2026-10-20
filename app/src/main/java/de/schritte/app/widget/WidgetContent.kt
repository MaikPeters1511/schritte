package de.schritte.app.widget

import de.schritte.app.data.Settings
import de.schritte.app.domain.DayState
import de.schritte.app.domain.StepCalculator
import java.time.LocalDate
import java.util.Locale

data class WidgetContent(val steps: String, val distance: String, val kcal: String)

fun widgetContent(day: DayState?, settings: Settings, today: LocalDate): WidgetContent {
    val n = day?.takeIf { it.date == today }?.steps ?: 0
    val calc = StepCalculator(settings.heightCm, settings.weightKg)
    return WidgetContent(
        steps = String.format(Locale.GERMANY, "%,d", n),
        distance = String.format(Locale.GERMANY, "%.2f km", calc.distanceM(n) / 1000),
        kcal = String.format(Locale.GERMANY, "%.0f kcal", calc.kcal(n)),
    )
}
