package de.schritte.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StepCalculatorTest {
    private val calc = StepCalculator(heightCm = 180, weightKg = 80)

    @Test fun stepLength_is0415OfHeight() = assertEquals(0.747, calc.stepLengthM, 1e-9)

    @Test fun distance_isStepsTimesStepLength() = assertEquals(747.0, calc.distanceM(1000), 1e-9)

    @Test fun kcal_usesWeightAndKilometres() = assertEquals(340.632, calc.kcal(10_000), 1e-6)

    @Test fun zeroSteps_giveZero() {
        assertEquals(0.0, calc.distanceM(0), 0.0)
        assertEquals(0.0, calc.kcal(0), 0.0)
    }
}
