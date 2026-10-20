package de.schritte.app.widget

import de.schritte.app.data.Settings
import de.schritte.app.domain.DayState
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class WidgetContentTest {
    private val today = LocalDate.of(2026, 10, 20)
    private val settings = Settings(180, 80)

    @Test fun todaySteps_areFormattedInGerman() {
        val c = widgetContent(DayState(today, 10_000, 0), settings, today)
        assertEquals(WidgetContent("10.000", "7,47 km", "341 kcal"), c)
    }

    @Test fun smallNumbers_haveNoSeparator() {
        assertEquals("999", widgetContent(DayState(today, 999, 0), settings, today).steps)
    }

    @Test fun stepsFromEarlierDay_showZero() {
        val c = widgetContent(DayState(today.minusDays(1), 8_000, 0), settings, today)
        assertEquals(WidgetContent("0", "0,00 km", "0 kcal"), c)
    }

    @Test fun noData_showsZero() {
        assertEquals(WidgetContent("0", "0,00 km", "0 kcal"), widgetContent(null, settings, today))
    }
}
